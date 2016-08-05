package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.*;
import com.ccreanga.webserver.http.representation.FileResourceRepresentation;
import com.ccreanga.webserver.http.representation.RepresentationManager;
import com.ccreanga.webserver.ioutil.ChunkedOutputStream;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.repository.FileManager;
import com.ccreanga.webserver.repository.ForbiddenException;
import com.ccreanga.webserver.repository.NotFoundException;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HTTPHeaders.*;
import static com.ccreanga.webserver.http.HttpMessageWriter.*;

public class GetHandler implements HttpMethodHandler {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    private boolean writeBody;


    public GetHandler(boolean writeBody) {
        this.writeBody = writeBody;
    }

    public void handleGetResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {

        HTTPHeaders responseHeaders = new HTTPHeaders();
        HTTPStatus responseStatus;
        String value;
        //ignore body for a GET request and skip the data in order to be able to read the next request
        //see http://tech.groups.yahoo.com/group/rest-discuss/message/9962
        //if the body is larger than the declared header the following request will be broken and the persistent connection will be closed
        if (request.getLength() != 0)
            request.getBody().skip(request.getLength());

        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");

        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((request.getHeader(HOST) == null) && (request.isHTTP1_1())) {//host is mandatory
            writeErrorResponse(request.getHeader(ACCEPT),responseHeaders, HTTPStatus.BAD_REQUEST, "missing host header", out);
            return;
        }

        //check if resource exists
        //decode the resource and remove any possible the parameters (as we only deliver static files)
        String resource = request.getUri();
        int index = resource.indexOf('?');
        if (index != -1)
            resource = resource.substring(0, index);
        resource = URLDecoder.decode(resource, "UTF-8");
        File file;
        try {
            file = FileManager.getInstance().getFile(configuration.getServerRootFolder() + resource);
        } catch (ForbiddenException e) {
            writeErrorResponse(request.getHeader(ACCEPT),responseHeaders, HTTPStatus.FORBIDDEN, "", out);
            return;
        } catch (NotFoundException e) {
            writeErrorResponse(request.getHeader(ACCEPT),responseHeaders, HTTPStatus.NOT_FOUND, "", out);
            return;
        }

        if (file.isFile()) {
            deliverFile(request, responseHeaders, file, configuration, writeBody, out);
        } else {
            deliverFolder(request, responseHeaders, file, configuration, writeBody, out);
        }
    }

    private void deliverFile(HttpRequestMessage request, HTTPHeaders responseHeaders, File file, Configuration configuration, boolean writeBody, OutputStream out) throws IOException {

        String etag = null;
        String mime = Mime.getType(Files.getFileExtension((file.getName())));
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        responseHeaders.putHeader(CONTENT_TYPE, mime);
        responseHeaders.putHeader(LAST_MODIFIED, DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_RFC822));

        if (request.isHTTP1_1()) {

            if (configuration.getRequestEtag().equals(Configuration.ETAG_WEAK)) {
                etag = EtagManager.getInstance().getFileEtag(file, true);//todo - etag should depend on the context encoding too!
                responseHeaders.putHeader(ETAG, etag);
            }
            HTTPStatus statusAfterConditionals = HttpConditionals.evaluateConditional(request, etag, modifiedDate);
            if (!statusAfterConditionals.equals(HTTPStatus.OK)) {
                writeResponseLine(statusAfterConditionals, out);
                writeHeaders(responseHeaders, out);
                return;
            }

            ContextHolder.get().setContentLength("chunked");
            responseHeaders.putHeader(TRANSFER_ENCODING, "chunked");

            if (request.headerContains(ACCEPT_ENCODING, "gzip") && shouldCompress(mime)) {
                responseHeaders.putHeader(CONTENT_ENCODING, "gzip");
            } else if (request.headerContains(ACCEPT_ENCODING, "deflate") && shouldCompress(mime)) {
                responseHeaders.putHeader(CONTENT_ENCODING, "deflate");
            }
            writeResponseLine(HTTPStatus.OK, out);
            writeHeaders(responseHeaders, out);
            //the chunks will have the length equal with ByteStreams.BUF_SIZE (or less)
            OutputStream enclosed = new ChunkedOutputStream(out);
            if (request.headerContains(ACCEPT_ENCODING, "gzip") && shouldCompress(mime)) {
                enclosed = new GZIPOutputStream(enclosed);
            } else if (request.headerContains(ACCEPT_ENCODING, "deflate") && shouldCompress(mime)) {
                enclosed = new DeflaterOutputStream(enclosed);
            }
            if (writeBody) {
                try {
                    ByteStreams.copy(new FileInputStream(file), enclosed);
                } catch (IOException e) {
                    throw new IOException(e.getMessage() + "( file name was " + file.getAbsolutePath() + ")");
                }
            }
            enclosed.close();//!this will not close the response stream - check ChunkedOutputStream.close
        } else {
            //http 1.0 does not support chunk => we will not support gzip - it will be too expensive because we will
            //have to compute the length for the gzipped data (so we'll have to write the the gzipped somewhere in ram/disk)
            String length = String.valueOf(file.length());
            ContextHolder.get().setContentLength(length);
            responseHeaders.putHeader(CONTENT_LENGTH, writeBody ? length : "0");
            writeResponseLine(HTTPStatus.OK, out);
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                ByteStreams.copy(new FileInputStream(file), out);
            }
        }

    }

    private void deliverFolder(HttpRequestMessage request, HTTPHeaders responseHeaders, File file, Configuration configuration, boolean writeBody, OutputStream out) throws IOException {
        //todo - it should not return html unless the client accepts that

        responseHeaders.putHeader(CONTENT_TYPE, Mime.getType("html"));

        FileResourceRepresentation representation =
                RepresentationManager.getInstance().getRepresentation(request.getHeader(HTTPHeaders.ACCEPT));
        responseHeaders.putHeader(CONTENT_TYPE, representation.getContentType());

        String folderRepresentation = representation.folderRepresentation(file,new File(configuration.getServerRootFolder()));

        writeResponseLine(HTTPStatus.OK, out);
        if (request.isHTTP1_1()) {

            responseHeaders.putHeader(TRANSFER_ENCODING, "chunked");
            ContextHolder.get().setContentLength("chunked");

            if (request.headerContains(ACCEPT_ENCODING, "gzip")) {
                responseHeaders.putHeader(CONTENT_ENCODING, "gzip");
            } else if (request.headerContains(ACCEPT_ENCODING, "deflate")) {
                responseHeaders.putHeader(CONTENT_ENCODING, "deflate");
            }

            writeHeaders(responseHeaders, out);

            OutputStream enclosed = new ChunkedOutputStream(out);
            if (request.headerContains(ACCEPT_ENCODING, "gzip")) {
                enclosed = new GZIPOutputStream(enclosed);
            } else if (request.headerContains(ACCEPT_ENCODING, "deflate")) {
                enclosed = new DeflaterOutputStream(enclosed);
            }
            if (writeBody) {
                enclosed.write(folderRepresentation.getBytes(Charsets.UTF_8));
            }
            enclosed.close();
        } else {
            ContextHolder.get().setContentLength("" + folderRepresentation.length());
            responseHeaders.putHeader(CONTENT_LENGTH, writeBody ? "" + folderRepresentation.length() : "0");
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                out.write(folderRepresentation.getBytes(Charsets.UTF_8));
            }
        }

    }

    private boolean shouldCompress(String mimetype) {
        //todo - refine that
        if (mimetype.startsWith("text") ||
                mimetype.contains("xml") ||
                mimetype.contains("json") ||
                mimetype.contains("javascript") ||
                mimetype.contains("html")
                )
            return true;
        return false;

    }

}


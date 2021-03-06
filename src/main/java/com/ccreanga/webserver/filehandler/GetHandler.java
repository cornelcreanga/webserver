package com.ccreanga.webserver.filehandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.filehandler.representation.FileResourceRepresentation;
import com.ccreanga.webserver.filehandler.representation.RepresentationManager;
import com.ccreanga.webserver.http.*;
import com.ccreanga.webserver.http.chunked.ChunkedOutputStream;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.repository.FileManager;
import com.ccreanga.webserver.repository.ForbiddenException;
import com.ccreanga.webserver.repository.NotFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpMessageWriter.*;
import static com.ccreanga.webserver.http.HttpStatus.*;

/**
 * Get handler. Right now the RFC's are not entirely implemented (todo - add details)
 */
public class GetHandler implements HttpMethodHandler {

    private boolean writeBody;


    public GetHandler(boolean writeBody) {
        this.writeBody = writeBody;
    }

    public void handleResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {

        HttpHeaders responseHeaders = new HttpHeaders();
        //ignore body for a GET request and skip the data in order to be able to read the next request
        //see http://tech.groups.yahoo.com/group/rest-discuss/message/9962
        //if the body is larger than the declared header write an response error and the persistent connection will be closed
        if (request.getLength() > 0) {
            long skipped = request.getBody().skip(request.getLength());
            if (request.getLength() != skipped) {//invalid http request
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, BAD_REQUEST, "body longer than the content lenght header", out);
                return;
            }
        } else if (request.isChunked()) {//chunkedstream
            //consume all the body
            while (request.getBody().read() > 0) ;
        }


        Common.addMandatoryHeaders(responseHeaders);

        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((!request.hasHeader(HOST)) && (request.isHTTP1_1())) {//host is mandatory
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, BAD_REQUEST, "missing host header", out);
            return;
        }

        //check if resource exists
        String resource = request.getUri();
        File file;
        try {
            file = FileManager.getInstance().getFile(configuration.getServerRootFolder() + resource);
        } catch (ForbiddenException e) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, FORBIDDEN, "", out);
            return;
        } catch (NotFoundException e) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, NOT_FOUND, "", out);
            return;
        }

        if (file.isFile()) {
            //deliver file content
            if ("true".equals(request.getSingleParam("info"))) {
                deliverFileInfo(request, responseHeaders, file, configuration, writeBody, false, out);
            } else if ("true".equals(request.getSingleParam("extended-info"))) {
                deliverFileInfo(request, responseHeaders, file, configuration, writeBody, true, out);
            } else
                deliverFile(request, responseHeaders, file, configuration, writeBody, out);
        } else {
            //for a folder deliver a representation generated taking into account the content-type
            //if html is accepted it will generate an html page similar with the one generated by Apache
            //if html is not accepted but json is it will generate an json representation
            //the json representation makes more sense if the webserver will not be used from a browser)
            deliverFolder(request, responseHeaders, file, configuration, writeBody, out);
        }
    }

    private void deliverFileInfo(HttpRequestMessage request,
                                 HttpHeaders responseHeaders,
                                 File file,
                                 Configuration cfg,
                                 boolean writeBody,
                                 boolean extendedInfo,
                                 OutputStream out) throws IOException {

        FileResourceRepresentation representation =
                RepresentationManager.getInstance().getRepresentation(request.getHeader(ACCEPT));
        responseHeaders.putHeader(CONTENT_TYPE, representation.getContentType());

        String fileInfo = representation.getFileInfo(file, cfg, extendedInfo);

        writeResponseLine(OK, out);

        ContextHolder.get().setContentLength("" + fileInfo.length());
        responseHeaders.putHeader(CONTENT_LENGTH, writeBody ? "" + fileInfo.length() : "0");
        writeHeaders(responseHeaders, out);
        if (writeBody) {
            out.write(fileInfo.getBytes(StandardCharsets.UTF_8));
        }

    }

    private void deliverFile(HttpRequestMessage request,
                             HttpHeaders responseHeaders,
                             File file,
                             Configuration configuration,
                             boolean writeBody,
                             OutputStream out) throws IOException {

        String etag = null;
        String mime = Mime.getType(IOUtil.getFileExtension((file.getName())));
        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        responseHeaders.putHeader(CONTENT_TYPE, mime);
        responseHeaders.putHeader(LAST_MODIFIED, DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_RFC822));

        if (request.isHTTP1_1()) {

            //should we compress?
            boolean shouldGzip = false, shouldDeflate = false;
            String etagExtension = "";
            if (request.headerContainsValue(ACCEPT_ENCODING, "gzip") && shouldCompress(mime)) {
                shouldGzip = true;
                responseHeaders.putHeader(CONTENT_ENCODING, "gzip");
                etagExtension = EtagManager.GZIP_EXT;
            } else if (request.headerContainsValue(ACCEPT_ENCODING, "deflate") && shouldCompress(mime)) {
                shouldDeflate = true;
                responseHeaders.putHeader(CONTENT_ENCODING, "deflate");
                etagExtension = EtagManager.DF_EXT;
            }

            //should we generate an etag?
            if (configuration.getRequestEtag().equals(Configuration.ETAG_WEAK)) {
                etag = EtagManager.getInstance().getFileEtag(file, etagExtension, true);
                responseHeaders.putHeader(ETAG, etag);
            }

            HttpStatus ifRangeConditional = null;
            if ((request.hasHeader(IF_RANGE)) && (request.hasHeader(RANGE))) {//ignore any other possible conditionals
                ifRangeConditional = HttpConditionals.evaluateIfRange(request, etag, modifiedDate);
            } else {
                //evaluate the conditionals.
                HttpStatus statusAfterConditionals = HttpConditionals.evaluateConditional(request, etag, modifiedDate);
                if (!statusAfterConditionals.equals(OK)) {
                    responseHeaders.removeHeader(CONTENT_ENCODING);

                    ContextHolder.get().setContentLength("0");
                    writeResponseLine(statusAfterConditionals, out);
                    writeHeaders(responseHeaders, out);
                    return;
                }
            }

            boolean shouldSendRange = false;
            long[] range = null;
            try {
                if (((request.hasHeader(RANGE)) && (!request.hasHeader(IF_RANGE))) || (PARTIAL_CONTENT.equals(ifRangeConditional))) {
                    shouldSendRange = true;
                    range = RangeManager.getInstance().obtainRange(request.getHeader(RANGE), file.length());
                }
            } catch (RangeException e) {
                //RFC 7233 3.1 the specified range(s) are invalid or unsatisfiable, the server SHOULD send a 416 (Range Not Satisfiable) response.
                //RFC 7233 4.4 specifies that 200 OK can be also returned but I think it's better to send 416
                writeResponseLine(REQUESTED_RANGE_NOT_SATISFIABLE, out);
                ContextHolder.get().setContentLength("0");
                responseHeaders.removeHeader(CONTENT_ENCODING);
                responseHeaders.putHeader(CONTENT_RANGE, "bytes */" + file.length());
                writeHeaders(responseHeaders, out);
                return;
            }

            //for HTTP 1.1 we'll use chunked encoding
            ContextHolder.get().setContentLength("chunked");
            responseHeaders.putHeader(TRANSFER_ENCODING, "chunked");


            long start = 0, end = file.length();
            if (shouldSendRange) {
                start = range[0];
                end = range[1];
                responseHeaders.putHeader(CONTENT_RANGE, "bytes " + start + "-" + end + "/" + file.length());
            }


            //write status+headers
            writeResponseLine(shouldSendRange ? PARTIAL_CONTENT : OK, out);
            writeHeaders(responseHeaders, out);
            //the chunks will have the length equal with ByteStreams.BUF_SIZE (or less)
            OutputStream enclosed = new ChunkedOutputStream(out);
            //if compressing is accepted we'll enclose the chunked stream with a compressing stream
            if (shouldGzip) {
                enclosed = new GZIPOutputStream(enclosed);
            } else if (shouldDeflate) {
                enclosed = new DeflaterOutputStream(enclosed);
            }
            //for HEAD requests will actually skip the body
            if (writeBody) {
                try {
                    InputStream in = new FileInputStream(file);
                    IOUtil.copy(in, enclosed, start, end - start + 1);
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
            writeResponseLine(OK, out);
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                IOUtil.copy(new FileInputStream(file), out);
            }
        }

    }

    private void deliverFolder(HttpRequestMessage request, HttpHeaders responseHeaders, File file, Configuration configuration, boolean writeBody, OutputStream out) throws IOException {

        //obtain the resource representation taking into account the content type
        FileResourceRepresentation representation =
                RepresentationManager.getInstance().getRepresentation(request.getHeader(ACCEPT));
        responseHeaders.putHeader(CONTENT_TYPE, representation.getContentType());


        //todo - do some tests in order to see if we can generate an weak etag also for folders
        //theoretically the folder modification is updated each time when it's content it's updated so we can use the folder lastmodified date
        //however more tests should be done (windows etc)

        String folderRepresentation = representation.folderRepresentation(file, new File(configuration.getServerRootFolder()));

        writeResponseLine(OK, out);
        if (request.isHTTP1_1()) {
            //for http1 write chunked
            responseHeaders.putHeader(TRANSFER_ENCODING, "chunked");
            ContextHolder.get().setContentLength("chunked");
            //if compression is accepted than use it
            if (request.headerContainsValue(ACCEPT_ENCODING, "gzip")) {
                responseHeaders.putHeader(CONTENT_ENCODING, "gzip");
            } else if (request.headerContainsValue(ACCEPT_ENCODING, "deflate")) {
                responseHeaders.putHeader(CONTENT_ENCODING, "deflate");
            }

            writeHeaders(responseHeaders, out);

            OutputStream enclosed = new ChunkedOutputStream(out);

            boolean shouldGzip = request.headerContainsValue(ACCEPT_ENCODING, "gzip");
            boolean shouldDeflate = request.headerContainsValue(ACCEPT_ENCODING, "deflate");

            //if compression is accepted than use it
            if (shouldGzip) {
                enclosed = new GZIPOutputStream(enclosed);
            } else if (shouldDeflate) {
                enclosed = new DeflaterOutputStream(enclosed);
            }
            if (writeBody) {
                enclosed.write(folderRepresentation.getBytes(StandardCharsets.UTF_8));
            }
            enclosed.close();
        } else {
            ///http 1.0 does not support chunked streams
            ContextHolder.get().setContentLength("" + folderRepresentation.length());
            responseHeaders.putHeader(CONTENT_LENGTH, writeBody ? "" + folderRepresentation.length() : "0");
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                out.write(folderRepresentation.getBytes(StandardCharsets.UTF_8));
            }
        }

    }

    /**
     * Returns true is the resource mimetype satisfies some conditions.
     * Right now it will only compress the most common text files but a lot of improvements can be bone
     *
     * @param mimetype - mimetype
     * @return - should we compress?
     */
    private boolean shouldCompress(String mimetype) {
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


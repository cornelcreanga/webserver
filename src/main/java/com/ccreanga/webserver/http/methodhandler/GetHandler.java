package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.*;
import com.ccreanga.webserver.http.representation.FileResourceRepresentation;
import com.ccreanga.webserver.http.representation.RepresentationManager;
import com.ccreanga.webserver.http.chunked.ChunkedOutputStream;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.repository.FileManager;
import com.ccreanga.webserver.repository.ForbiddenException;
import com.ccreanga.webserver.repository.NotFoundException;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpStatus.PARTIAL_CONTENT;
import static com.ccreanga.webserver.http.HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
import static com.google.common.net.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpMessageWriter.*;

/**
 * Get handler. Right now the RFC's are not entirely implemented (todo - add details)
 */
public class GetHandler implements HttpMethodHandler {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    private boolean writeBody;


    public GetHandler(boolean writeBody) {
        this.writeBody = writeBody;
    }

    public void handleGetResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {

        HttpHeaders responseHeaders = new HttpHeaders();
        //ignore body for a GET request and skip the data in order to be able to read the next request
        //see http://tech.groups.yahoo.com/group/rest-discuss/message/9962
        //if the body is larger than the declared header write an response error and the persistent connection will be closed
        if (request.getLength() > 0){
            long skipped = request.getBody().skip(request.getLength());
            if (request.getLength()!=skipped){//invalid http request
                writeErrorResponse(request.getHeader(ACCEPT),responseHeaders, HttpStatus.BAD_REQUEST, "body longer than the content lenght header", out);
                return;
            }
        }else if (request.isChunked()) {//chunkedstream
            //consume all the body
            while(request.getBody().read()>0);
        }


        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");

        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((request.getHeader(HOST) == null) && (request.isHTTP1_1())) {//host is mandatory
            writeErrorResponse(request.getHeader(ACCEPT),responseHeaders, HttpStatus.BAD_REQUEST, "missing host header", out);
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
            writeErrorResponse(request.getHeader(ACCEPT),responseHeaders, HttpStatus.FORBIDDEN, "", out);
            return;
        } catch (NotFoundException e) {
            writeErrorResponse(request.getHeader(ACCEPT),responseHeaders, HttpStatus.NOT_FOUND, "", out);
            return;
        }

        if (file.isFile()) {
            //deliver file content
            deliverFile(request, responseHeaders, file, configuration, writeBody, out);
        } else {
            //for a folder deliver a representation generated taking into account the content-type
            //if html is accepted it will generate an html page similar with the one generated by Apache
            //if html is not accepted but json is it will generate an json representation (todo - this is planned not done)
            //the json representation makes more sense if the webserver will not be used from a browser)
            deliverFolder(request, responseHeaders, file, configuration, writeBody, out);
        }
    }

    private void deliverFile(HttpRequestMessage request, HttpHeaders responseHeaders, File file, Configuration configuration, boolean writeBody, OutputStream out) throws IOException {

        String etag = null;
        String mime = Mime.getType(Files.getFileExtension((file.getName())));
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        responseHeaders.putHeader(CONTENT_TYPE, mime);
        responseHeaders.putHeader(LAST_MODIFIED, DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_RFC822));

        if (request.isHTTP1_1()) {

            //should we compress?
            boolean shouldGzip = false,shouldDeflate=false;
            String etagExtension = "";
            if (request.headerContains(ACCEPT_ENCODING, "gzip") && shouldCompress(mime)){
                shouldGzip = true;
                responseHeaders.putHeader(CONTENT_ENCODING, "gzip");
                etagExtension = "gz";
            }else if (request.headerContains(ACCEPT_ENCODING, "deflate") && shouldCompress(mime)){
                shouldDeflate = true;
                responseHeaders.putHeader(CONTENT_ENCODING, "deflate");
                etagExtension = "df";
            }

            //should we generate an etag?
            if (configuration.getRequestEtag().equals(Configuration.ETAG_WEAK)) {
                etag = EtagManager.getInstance().getFileEtag(file,etagExtension, true);
                responseHeaders.putHeader(ETAG, etag);
            }

            HttpStatus ifRangeConditional = null;
            if ((request.getHeader(IF_RANGE)!=null) && (request.getHeader(RANGE)!=null)){//ignore any other possible conditionals
                ifRangeConditional = HttpConditionals.evaluateIfRange(request, etag, modifiedDate);
            }else {
                //evaluate the conditionals.
                HttpStatus statusAfterConditionals = HttpConditionals.evaluateConditional(request, etag, modifiedDate);
                if (!statusAfterConditionals.equals(HttpStatus.OK)) {
                    ContextHolder.get().setContentLength("0");
                    responseHeaders.removeHeader(CONTENT_ENCODING);
                    writeResponseLine(statusAfterConditionals, out);
                    writeHeaders(responseHeaders, out);
                    return;
                }
            }

            boolean shouldSendRange = false;
            long[] range = null;
            try {
                if (((request.getHeader(RANGE) != null) && (request.getHeader(IF_RANGE) == null)) || (PARTIAL_CONTENT.equals(ifRangeConditional))) {
                    shouldSendRange = true;
                    range = RangeManager.getInstance().obtainRange(request.getHeader(RANGE), file.length());
                }
            }catch (RangeException e){
                writeResponseLine(REQUESTED_RANGE_NOT_SATISFIABLE, out);
                responseHeaders.putHeader(CONTENT_RANGE,"bytes */"+file.length());
                writeHeaders(responseHeaders, out);
                return;
            }

            //for HTTP 1.1 we'll use chunked encoding
            ContextHolder.get().setContentLength("chunked");
            responseHeaders.putHeader(TRANSFER_ENCODING, "chunked");


            long start = 0, end=file.length();
            if (shouldSendRange){
                start = range[0];
                end = range[1];
                responseHeaders.putHeader(CONTENT_RANGE,"bytes * "+start+"-"+end+"/"+file.length());
            }


            //write status+headers
            writeResponseLine(HttpStatus.OK, out);
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
                    IOUtil.copy(in, enclosed,start,end-start);
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
            writeResponseLine(HttpStatus.OK, out);
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                ByteStreams.copy(new FileInputStream(file), out);
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

        String folderRepresentation = representation.folderRepresentation(file,new File(configuration.getServerRootFolder()));

        writeResponseLine(HttpStatus.OK, out);
        if (request.isHTTP1_1()) {
            //for http1 write chunked
            responseHeaders.putHeader(TRANSFER_ENCODING, "chunked");
            ContextHolder.get().setContentLength("chunked");
            //if compression is accepted than use it
            if (request.headerContains(ACCEPT_ENCODING, "gzip")) {
                responseHeaders.putHeader(CONTENT_ENCODING, "gzip");
            } else if (request.headerContains(ACCEPT_ENCODING, "deflate")) {
                responseHeaders.putHeader(CONTENT_ENCODING, "deflate");
            }

            writeHeaders(responseHeaders, out);

            OutputStream enclosed = new ChunkedOutputStream(out);

            boolean shouldGzip = request.headerContains(ACCEPT_ENCODING, "gzip");
            boolean shouldDeflate = request.headerContains(ACCEPT_ENCODING, "deflate");

            //if compression is accepted than use it
            if (shouldGzip) {
                enclosed = new GZIPOutputStream(enclosed);
            } else if (shouldDeflate) {
                enclosed = new DeflaterOutputStream(enclosed);
            }
            if (writeBody) {
                enclosed.write(folderRepresentation.getBytes(Charsets.UTF_8));
            }
            enclosed.close();
        } else {
            ///http 1.0 does not support chunked streams
            ContextHolder.get().setContentLength("" + folderRepresentation.length());
            responseHeaders.putHeader(CONTENT_LENGTH, writeBody ? "" + folderRepresentation.length() : "0");
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                out.write(folderRepresentation.getBytes(Charsets.UTF_8));
            }
        }

    }

    /**
     * Returns true is the resource mimetype satisfies some conditions.
     * Right now it will only compress the most common text files but a lot of improvements can be bone
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


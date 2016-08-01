package com.ccreanga.webserver;

import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.HTTPVersion;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.ioutil.ChunkedOutputStream;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.repository.FileManager;
import com.ccreanga.webserver.repository.ForbiddenException;
import com.ccreanga.webserver.repository.NotFoundException;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.*;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.ccreanga.webserver.MessageWriter.*;
import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_LOG;
import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_RFC822;

public class GetHandler {

    public void handleGetResponse(RequestMessage request, boolean writeBody, Configuration configuration, OutputStream out) throws IOException {

        HTTPHeaders responseHeaders = new HTTPHeaders();
        HTTPStatus responseStatus;
        String value;
        //ignore body for a GET request and skip the data in order to be able to read the next request
        //see http://tech.groups.yahoo.com/group/rest-discuss/message/9962
        //if the body is larger than the declared header the following request will be broken and the persistent connection will be closed
        if (request.getLength() != 0)
            request.getBody().skip(request.getLength());

        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        ContextHolder.get().setDate(DateUtil.currentDate(FORMATTER_LOG));
        responseHeaders.putHeader(HTTPHeaders.DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(HTTPHeaders.CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(HTTPHeaders.VARY, "Accept-Encoding");

        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((request.getHeader(HTTPHeaders.HOST) == null) && (request.getVersion().equals(HTTPVersion.HTTP_1_1))) {//host is mandatory
            writeErrorResponse(responseHeaders, HTTPStatus.BAD_REQUEST, "missing host header", out);
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
            writeErrorResponse(responseHeaders, HTTPStatus.FORBIDDEN, "", out);
            return;
        } catch (NotFoundException e) {
            writeErrorResponse(responseHeaders, HTTPStatus.NOT_FOUND, "", out);
            return;
        }




        /**
         * Check for conditionals. If-Range is not supported for the moment
         * If conditionals are used improperly return badrequest instead of ignoring them
         */
        LocalDateTime fileLastModifiedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC"));
//        value = request.getHeader(HTTPHeaders.IF_NONE_MATCH);
//        if (value != null) {
//            String etag = EtagManager.getDateBasedEtag(file);//weak etag for the moment
//            if ((value.contains(etag))) {
//                response = new ResponseMessage(HTTPStatus.NOT_MODIFIED);
//                response.setHeader(HTTPHeaders.ETAG, etag);
//                return response;
//            }
//        }
//
//        value = request.getHeader(HTTPHeaders.IF_MODIFIED_SINCE);
//        if ((value != null) && (request.getHeader(HTTPHeaders.IF_NONE_MATCH) != null)) {
//            LocalDateTime date = DateUtil.parseDate(value);
//            if (date == null)
//                return new ResponseMessage(HTTPStatus.BAD_REQUEST);
//            if (date.isAfter(fileLastModifiedDate))
//                return new ResponseMessage(HTTPStatus.NOT_MODIFIED);
//        }
//
//        value = request.getHeader(HTTPHeaders.IF_MATCH);
//        if (value != null) {
//            String etag = EtagManager.getDateBasedEtag(file);//weak etag for the moment
//            if (etag.equals(value))
//                return new ResponseMessage(HTTPStatus.PRECONDITION_FAILED);
//        }
//
//        value = request.getHeader(HTTPHeaders.IF_UNMODIFIED_SINCE);
//        if (value != null) {
//            LocalDateTime date = DateUtil.parseDate(value);
//            if (date == null)
//                return new ResponseMessage(HTTPStatus.BAD_REQUEST);
//            if (date.isBefore(fileLastModifiedDate))
//                return new ResponseMessage(HTTPStatus.PRECONDITION_FAILED);
//        }

        //todo - not ok, it can crash (eg internal server error)
        writeResponseLine(HTTPStatus.OK, out);


        if (file.isFile()) {
            deliverFile(request,responseHeaders,file,configuration,writeBody,out);
        } else {
            deliverFolder(request,responseHeaders,file,configuration,writeBody,out);
        }
    }

    private HTTPStatus evaluateConditional(RequestMessage request,HTTPHeaders responseHeaders,String etag){
        //https://tools.ietf.org/html/rfc7232#section-6
        HTTPStatus response = HTTPStatus.OK;
        String ifMatch = request.getHeader(HTTPHeaders.IF_MATCH);
        String IfUnmodifiedSince =  request.getHeader(HTTPHeaders.IF_UNMODIFIED_SINCE);
        String ifNoneMatch  =  request.getHeader(HTTPHeaders.IF_NONE_MATCH);
        String ifModifiedSince  =  request.getHeader(HTTPHeaders.IF_MODIFIED_SINCE);

        return response;
//        String header = headers.get("If-Match");
//        if (header != null && !match(true, splitElements(header, false), etag))
//            return 412;
//        // If-Unmodified-Since
//        Date date = headers.getDate("If-Unmodified-Since");
//        if (date != null && lastModified > date.getTime())
//            return 412;
//        // If-Modified-Since
//        int status = 200;
//        boolean force = false;
//        date = headers.getDate("If-Modified-Since");
//        if (date != null && date.getTime() <= System.currentTimeMillis()) {
//            if (lastModified > date.getTime())
//                force = true;
//            else
//                status = 304;
//        }
//        // If-None-Match
//        header = headers.get("If-None-Match");
//        if (header != null) {
//            if (match(false, splitElements(header, false), etag)) // RFC7232#3.2: use weak matching
//                status = req.getMethod().equals("GET")
//                        || req.getMethod().equals("HEAD") ? 304 : 412;
//            else
//                force = true;
//        }
//        return force ? 200 : status;

//        if (ifMatch != null) {
//            if (!etag.equals(ifMatch)){
//                return HTTPStatus.PRECONDITION_FAILED;
//            }
//            if (ifNoneMatch!=null){
//                (!etag.equals(ifMatch)){
//            }
//
//        }
//        if (etag.equals(value))
//        if (value != null) {
//            String etag = EtagManager.getDateBasedEtag(file);//weak etag for the moment
//            if (etag.equals(value))
//                return new ResponseMessage(HTTPStatus.PRECONDITION_FAILED);
//        }
    }

    private void deliverFile(RequestMessage request,HTTPHeaders responseHeaders,File file,Configuration configuration,boolean writeBody,OutputStream out) throws IOException{

        String etag = null;
        String mime = Mime.getType(Files.getFileExtension((file.getName())));

        responseHeaders.putHeader(HTTPHeaders.CONTENT_TYPE,mime);
        responseHeaders.putHeader(HTTPHeaders.LAST_MODIFIED, DateUtil.formatDate(Instant.ofEpochMilli(file.lastModified()),DateUtil.FORMATTER_RFC822));

        if (configuration.getRequestEtag().equals(Configuration.ETAG_WEAK) && request.getVersion().equals(HTTPVersion.HTTP_1_1)) {
            etag = EtagManager.getInstance().getFileEtag(file, true);
            responseHeaders.putHeader(HTTPHeaders.ETAG, etag);
            HTTPStatus statusAfterConditionals = evaluateConditional(request,responseHeaders,etag);
            if (!statusAfterConditionals.equals(HTTPStatus.OK)){

            }
        }


        if (request.getVersion().equals(HTTPVersion.HTTP_1_1)){
            responseHeaders.putHeader(HTTPHeaders.TRANSFER_ENCODING,"chunked");

            if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"gzip") && shouldCompress(mime)){
                responseHeaders.putHeader(HTTPHeaders.CONTENT_ENCODING,"gzip");
            }else if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"deflate")  && shouldCompress(mime)){
                responseHeaders.putHeader(HTTPHeaders.CONTENT_ENCODING,"deflate");
            }

            writeHeaders(responseHeaders, out);

            OutputStream enclosed = new ChunkedOutputStream(out);
            if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"gzip") && shouldCompress(mime)){
                enclosed = new GZIPOutputStream(enclosed);
            }else if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"deflate") && shouldCompress(mime)){
                enclosed = new DeflaterOutputStream(enclosed);
            }
            if (writeBody) {
                ByteStreams.copy(new FileInputStream(file), enclosed);
            }
            enclosed.close();//this will not close the response stream - the chunkedoutputstream does not propagate the close method
        }else {
            //http 1.0 does not support chunk => we will not support gzip - it will be too expensive
            String length = String.valueOf(file.length());
            ContextHolder.get().setContentLength(length);
            responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, writeBody?length:"0");
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                ByteStreams.copy(new FileInputStream(file), out);
            }
        }

    }

    private void deliverFolder(RequestMessage request,HTTPHeaders responseHeaders,File file,Configuration configuration,boolean writeBody,OutputStream out) throws IOException{
        //todo - it should not return html unless the client accepts that

        responseHeaders.putHeader(HTTPHeaders.CONTENT_TYPE, Mime.getType("html"));
        String indexPage = TemplateRepository.instance().buildIndex(file, configuration.getServerRootFolder());

        if (request.getVersion().equals(HTTPVersion.HTTP_1_1)){
            responseHeaders.putHeader(HTTPHeaders.TRANSFER_ENCODING,"chunked");

            if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"gzip")){
                responseHeaders.putHeader(HTTPHeaders.CONTENT_ENCODING,"gzip");
            }else if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"deflate")){
                responseHeaders.putHeader(HTTPHeaders.CONTENT_ENCODING,"deflate");
            }

            writeHeaders(responseHeaders, out);

            OutputStream enclosed = new ChunkedOutputStream(out);
            if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"gzip") ){
                enclosed = new GZIPOutputStream(enclosed);
            }else if (request.headerContains(HTTPHeaders.ACCEPT_ENCODING,"deflate") ){
                enclosed = new DeflaterOutputStream(enclosed);
            }
            if (writeBody) {
                enclosed.write(indexPage.getBytes(Charsets.UTF_8));
            }
            enclosed.close();
        }else{
            ContextHolder.get().setContentLength("" + indexPage.length());
            responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, writeBody?"" + indexPage.length():"0");
            writeHeaders(responseHeaders, out);
            if (writeBody) {
                out.write(indexPage.getBytes(Charsets.UTF_8));
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


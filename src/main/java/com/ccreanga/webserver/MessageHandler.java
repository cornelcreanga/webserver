package com.ccreanga.webserver;


import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.repository.FileManager;
import com.ccreanga.webserver.repository.ForbiddenException;
import com.ccreanga.webserver.repository.NotFoundException;
import com.ccreanga.webserver.formatters.DateUtil;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.*;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.ccreanga.webserver.MessageWriter.*;
import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_LOG;
import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_RFC822;

public class MessageHandler {


    public void handleMessage(RequestMessage request, Configuration configuration, OutputStream out) throws IOException{
        //EXPECT header is not yet handled
        if ((request==null) || (request.getMethod()==null))
            System.out.println("request:"+request);//todo - check the back button
        switch (request.getMethod()) {
            case GET:
                handleGetResponse(request,configuration,out);
                return;
            case HEAD:
                handleGetResponse(request, false,configuration,out);
                return;
            case POST:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED,out);
                return;
            case PUT:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED,out);
                return;
            case DELETE:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED,out);
                return;
            case CONNECT:
                return;
            case PATCH:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED,out);
                return;
            case TRACE:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED,out);
                return;
            case OPTIONS:
                HTTPHeaders responseHeaders = new HTTPHeaders();
                responseHeaders.putHeader(HTTPHeaders.ALLOW, "GET, HEAD, OPTIONS");
                responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, "0");
                writeResponseLine(HTTPStatus.OK,out);
                writeHeaders(responseHeaders,out);
                return;
        }
        throw new InternalException("invalid method "+request.getMethod()+". this should never happen(internal error)");
    }

    private void handleGetResponse(RequestMessage request,Configuration configuration,OutputStream out) throws IOException{
        handleGetResponse(request, true,configuration,out);
    }

    private void handleGetResponse(RequestMessage request, boolean hasBody,Configuration configuration,OutputStream out) throws IOException {
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
        responseHeaders.putHeader(HTTPHeaders.DATE, currentDate.replace("UTC","GMT"));
        responseHeaders.putHeader(HTTPHeaders.CONNECTION,"Keep-Alive");
        responseHeaders.putHeader(HTTPHeaders.VARY,"Accept-Encoding");
        responseHeaders.putHeader(HTTPHeaders.SERVER,"WEBSERVER/0.0.1");

        if (request.getHeader(HTTPHeaders.HOST) == null){//host is mandatory
            writeErrorResponse(responseHeaders, HTTPStatus.BAD_REQUEST,"missing host header",out);
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
            file = FileManager.getInstance().getFile(configuration.getRootFolder() + resource);
        } catch (ForbiddenException e) {
            writeErrorResponse(responseHeaders, HTTPStatus.FORBIDDEN,"",out);
            return;
        } catch (NotFoundException e){
            writeErrorResponse(responseHeaders, HTTPStatus.NOT_FOUND,"",out);
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



        //Everything is ok, we will build the headers
        if (!hasBody){
            writeNoBodyResponse(responseHeaders, HTTPStatus.OK,out);
            return;
        }

//        responseHeaders.putHeader(HTTPHeaders.LAST_MODIFIED, DateUtil.formatDate(fileLastModifiedDate.toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));
//        responseHeaders.putHeader(HTTPHeaders.ETAG, EtagManager.getDateBasedEtag(file));

        String indexPage = null;

        writeResponseLine(HTTPStatus.OK,out);


        if (file.isFile()){
            responseHeaders.putHeader(HTTPHeaders.CONTENT_TYPE, Mime.getType(Files.getFileExtension((resource))));
            String length = String.valueOf(file.length());
            ContextHolder.get().setContentLength(length);
            responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, length);
            writeHeaders(responseHeaders,out);
            ByteStreams.copy(new FileInputStream(file), out);
        }else{
            //todo - it should not return html unless the client accepts that
            indexPage = TemplateRepository.instance().buildIndex(file,configuration.getRootFolder());
            responseHeaders.putHeader(HTTPHeaders.CONTENT_TYPE, Mime.getType("html"));
            ContextHolder.get().setContentLength(""+indexPage.length());
            responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, ""+indexPage.length());
            writeHeaders(responseHeaders,out);
            out.write(indexPage.getBytes(Charsets.UTF_8));
        }
        out.flush();
    }



}

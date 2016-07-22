package com.ccreanga.webserver;


import com.adobe.webserver.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MessageHandler {


    public ResponseMessage handleMessage(RequestMessage request) throws IOException{
        //we do not handle yet EXPECT
        switch (request.getMethod()) {
            case GET:
                return handleGetResponse(request);
            case HEAD:
                return handleGetResponse(request, false);
            case POST:
                return new ResponseMessage(HttpStatus.NOT_IMPLEMENTED);
            case PUT:
                return new ResponseMessage(HttpStatus.NOT_IMPLEMENTED);
            case DELETE:
                return new ResponseMessage(HttpStatus.NOT_IMPLEMENTED);
            case CONNECT:
                break;
            case PATCH:
                return new ResponseMessage(HttpStatus.NOT_IMPLEMENTED);
            case TRACE:
                return new ResponseMessage(HttpStatus.NOT_IMPLEMENTED);
            case OPTIONS:
                ResponseMessage response = new ResponseMessage(HttpStatus.OK);
                response.setHeader(Headers.allow, "GET, HEAD, OPTIONS");
                response.setHeader(Headers.contentLength, "0");
                return response;
        }
        throw new FatalException("invalid method, this should never happen");
    }

    private ResponseMessage handleGetResponse(RequestMessage request) throws IOException{
        return handleGetResponse(request, true);
    }

    private ResponseMessage handleGetResponse(RequestMessage request, boolean hasBody) throws IOException {
        DateUtil dateUtil = new DateUtil();
        ResponseMessage response;
        String value;

        //ignore body and skip the data in order to be able to read the next request
        //see http://tech.groups.yahoo.com/group/rest-discuss/message/9962
        //if the body is larger than the declared header the following request will be broken and the persistent connection will be closed
        if (request.getLength()!=0)
            request.getBody().skip(request.getLength());

        if (request.getHeader(Headers.host) == null)//host is mandatory
            return new ResponseMessage(Status.badRequest);

        //check if resource exists
        //remove the parameters, as we only deliver static files
        String resource = request.getResource();
        int index = resource.indexOf('?');
        if (index!=-1)
            resource = resource.substring(0,index);
        File file = FileManager.getInstance().getFile(Config.getConfig().getRootFolder() + resource);
        if ((!file.exists()) || (!file.isFile()))
            return new ResponseMessage(Status.notFound);
        //check if the requested file is not too large
        if (file.length() > Config.getConfig().getMaxEntitySize())
            return new ResponseMessage(Status.entityTooLarge);

        /**
         * Check for conditionals. If-Range is not supported for the moment
         * If conditionals are used improperly return badrequest instead of ignoring them
         */
        value = request.getHeader(Headers.ifNoneMatch);
        if (value != null) {
            String etag = EtagGenerator.getDateBasedEtag(file);//weak etag for the moment
            if ((value.contains(etag))) {
                response = new ResponseMessage(Status.notModified);
                response.setHeader(Headers.etag, etag);
                return response;
            }
        }

        value = request.getHeader(Headers.ifModifiedSince);
        if ((value != null) && (request.getHeader(Headers.ifNoneMatch) != null)) {
            Date date = dateUtil.getDate(value);
            if (date == null)
                return new ResponseMessage(Status.badRequest);
            if ((file.lastModified()) <= date.getTime())
                return new ResponseMessage(Status.notModified);
        }

        value = request.getHeader(Headers.ifMatch);
        if (value != null) {
            String etag = EtagGenerator.getDateBasedEtag(file);//weak etag for the moment
            if (etag.equals(value))
                return new ResponseMessage(Status.precondFailed);
        }

        value = request.getHeader(Headers.ifUnmodifiedSince);
        if (value != null) {
            Date date = dateUtil.getDate(value);
            if (date == null)
                return new ResponseMessage(Status.badRequest);
            if ((file.lastModified()) > date.getTime())
                return new ResponseMessage(Status.precondFailed);
        }

        //Everything is ok, we will build the headers
        response = new ResponseMessage(Status.ok);
        if (!hasBody)
            response.setIgnoreBody(true);
        response.setResourceFullPath(Config.getConfig().getRootFolder() + File.separator + resource);
        response.setHeader(Headers.lastModified, dateUtil.formatDate(file.lastModified()));
        response.setHeader(Headers.etag, EtagGenerator.getDateBasedEtag(file));
        response.setResourceLength(file.length());


        return response;
    }


}

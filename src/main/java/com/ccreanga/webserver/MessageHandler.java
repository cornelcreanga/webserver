package com.ccreanga.webserver;


import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.repository.FileManager;
import com.ccreanga.webserver.repository.ForbiddenException;
import com.ccreanga.webserver.repository.NotFoundException;
import com.ccreanga.webserver.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class MessageHandler {


    public ResponseMessage handleMessage(RequestMessage request,Configuration configuration) throws IOException{
        //EXPECT header is not yet handled
        switch (request.getMethod()) {
            case GET:
                return handleGetResponse(request,configuration);
            case HEAD:
                return handleGetResponse(request, false,configuration);
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
                response.setHeader(HTTPHeaders.ALLOW, "GET, HEAD, OPTIONS");
                response.setHeader(HTTPHeaders.CONTENT_LENGTH, "0");
                return response;
        }
        throw new InternalException("invalid method "+request.getMethod()+". this should never happen(internal error)");
    }

    private ResponseMessage handleGetResponse(RequestMessage request,Configuration configuration) throws IOException{
        return handleGetResponse(request, true,configuration);
    }

    private ResponseMessage handleGetResponse(RequestMessage request, boolean hasBody,Configuration configuration) throws IOException {
        ResponseMessage response;
        String value;
        //ignore body and skip the data in order to be able to read the next request
        //see http://tech.groups.yahoo.com/group/rest-discuss/message/9962
        //if the body is larger than the declared header the following request will be broken and the persistent connection will be closed
        if (request.getLength() != 0)
            request.getBody().skip(request.getLength());

        if (request.getHeader(HTTPHeaders.HOST) == null)//host is mandatory
            return new ResponseMessage(HttpStatus.BAD_REQUEST);

        //check if resource exists
        //decode the resource and remove any possible the parameters (as we only deliver static files)
        String resource = request.getUri();
        int index = resource.indexOf('?');
        if (index != -1)
            resource = resource.substring(0, index);
        resource = URLDecoder.decode(resource, configuration.getRequestGetEncoding());
        File file;
        try {
            file = FileManager.getInstance().getFile(configuration.getRootFolder() + resource);
        } catch (ForbiddenException e) {
            return new ResponseMessage(HttpStatus.FORBIDDEN);
        } catch (NotFoundException e){
            return new ResponseMessage(HttpStatus.NOT_FOUND);
        }

        /**
         * Check for conditionals. If-Range is not supported for the moment
         * If conditionals are used improperly return badrequest instead of ignoring them
         */
        LocalDateTime fileLastModifiedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC"));
        value = request.getHeader(HTTPHeaders.IF_NONE_MATCH);
        if (value != null) {
            String etag = EtagGenerator.getDateBasedEtag(file);//weak etag for the moment
            if ((value.contains(etag))) {
                response = new ResponseMessage(HttpStatus.NOT_MODIFIED);
                response.setHeader(HTTPHeaders.ETAG, etag);
                return response;
            }
        }

        value = request.getHeader(HTTPHeaders.IF_MODIFIED_SINCE);
        if ((value != null) && (request.getHeader(HTTPHeaders.IF_NONE_MATCH) != null)) {
            LocalDateTime date = DateUtil.parseDate(value);
            if (date == null)
                return new ResponseMessage(HttpStatus.BAD_REQUEST);
            if (date.isAfter(fileLastModifiedDate))
                return new ResponseMessage(HttpStatus.NOT_MODIFIED);
        }

        value = request.getHeader(HTTPHeaders.IF_MATCH);
        if (value != null) {
            String etag = EtagGenerator.getDateBasedEtag(file);//weak etag for the moment
            if (etag.equals(value))
                return new ResponseMessage(HttpStatus.PRECONDITION_FAILED);
        }

        value = request.getHeader(HTTPHeaders.IF_UNMODIFIED_SINCE);
        if (value != null) {
            LocalDateTime date = DateUtil.parseDate(value);
            if (date == null)
                return new ResponseMessage(HttpStatus.BAD_REQUEST);
            if (date.isBefore(fileLastModifiedDate))
                return new ResponseMessage(HttpStatus.PRECONDITION_FAILED);
        }

        //Everything is ok, we will build the headers
        response = new ResponseMessage(HttpStatus.OK);
        if (!hasBody)
            response.setIgnoreBody(true);
        response.setResourceFullPath(configuration.getRootFolder() + File.separator + resource);
        response.setHeader(HTTPHeaders.LAST_MODIFIED, DateUtil.formatDate(fileLastModifiedDate.toInstant(ZoneOffset.UTC)));
        response.setHeader(HTTPHeaders.ETAG, EtagGenerator.getDateBasedEtag(file));
        response.setResourceLength(file.length());


        return response;
    }


}

package com.ccreanga.webserver;

import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.HTTPVersion;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.repository.FileManager;
import com.ccreanga.webserver.repository.ForbiddenException;
import com.ccreanga.webserver.repository.NotFoundException;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.ccreanga.webserver.MessageWriter.*;
import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_LOG;
import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_RFC822;

public class GetHandler {

    public void handleGetResponse(RequestMessage request, boolean hasBody, Configuration configuration, OutputStream out) throws IOException {

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


        //Everything is ok, we will build the headers
        if (!hasBody) {
            writeNoBodyResponse(responseHeaders, HTTPStatus.OK, out);
            return;
        }

//        responseHeaders.putHeader(HTTPHeaders.LAST_MODIFIED, DateUtil.formatDate(fileLastModifiedDate.toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));
//        responseHeaders.putHeader(HTTPHeaders.ETAG, EtagManager.getDateBasedEtag(file));

        String indexPage = null;

        writeResponseLine(HTTPStatus.OK, out);


        if (file.isFile()) {
            responseHeaders.putHeader(HTTPHeaders.CONTENT_TYPE, Mime.getType(Files.getFileExtension((resource))));
            String length = String.valueOf(file.length());
            ContextHolder.get().setContentLength(length);
            responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, length);
            writeHeaders(responseHeaders, out);
            ByteStreams.copy(new FileInputStream(file), out);
        } else {
            //todo - it should not return html unless the client accepts that
            indexPage = TemplateRepository.instance().buildIndex(file, configuration.getServerRootFolder());
            responseHeaders.putHeader(HTTPHeaders.CONTENT_TYPE, Mime.getType("html"));
            ContextHolder.get().setContentLength("" + indexPage.length());
            responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, "" + indexPage.length());
            writeHeaders(responseHeaders, out);
            out.write(indexPage.getBytes(Charsets.UTF_8));
        }
    }


}


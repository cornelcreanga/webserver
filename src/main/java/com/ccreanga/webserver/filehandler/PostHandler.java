package com.ccreanga.webserver.filehandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.*;
import com.ccreanga.webserver.ioutil.FileUtil;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.filehandler.HandlerUtils.*;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpMessageWriter.*;

public class PostHandler implements HttpMethodHandler {

    public static final Logger serverLog = Logger.getLogger("serverLog");

    @Override
    public void handleResponse(HttpRequestMessage request, Configuration cfg, OutputStream out) throws IOException {

        HttpHeaders responseHeaders = new HttpHeaders();
        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");

        if (!hostHeaderIsPresent(request, out, responseHeaders)) return;

        if (uriContainsIllegalPath(request, out, responseHeaders)) return;

        if (!rootFolderIsWritable(request, cfg, out, responseHeaders)) return;

        String contentType = request.getHeader(CONTENT_TYPE);

        if (contentTypeIsFormRelated(request, out, responseHeaders, contentType)) return;

        if ((contentType == null) || contentType.length() == 0) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "content type is mandatory for POST", out);
            return;
        }


        String extension = Mime.getExtension(contentType);
        if (extension == null) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "content type " + contentType + " is not known", out);
            return;
        }
        String uri = request.getUri();
        if (!uri.endsWith("/"))
            uri = uri + "/";

        try {
            Path path = Paths.get(cfg.getServerRootFolder() + uri + UUID.randomUUID().toString() + "." + extension);
            if (!createFolderHierarchy(request, out, responseHeaders, path)) return;

            MessageDigest md = instantiateMD5(request, out, responseHeaders);
            if (md == null)
                return;

            File file = path.toFile();
            if (!copyRequestBody(request, out, responseHeaders, md, true, file)) return;

            FileUtil.createMD5file(file, md);
            writeResponseLine(HttpStatus.CREATED, out);

            responseHeaders.putHeader(LOCATION, new String(Base64.getEncoder().encode((uri + file.getName()).getBytes("UTF-8"))));
            writeHeaders(responseHeaders, out);
            ContextHolder.get().setContentLength("-");

        } catch (InvalidPathException e) {
            serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "invalid characters", out);
            return;
        }

    }
}

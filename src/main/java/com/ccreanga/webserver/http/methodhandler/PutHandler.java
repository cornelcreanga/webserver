package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.ioutil.FileUtil;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.logging.Logger;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeErrorResponse;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;
import static com.ccreanga.webserver.http.methodhandler.HandlerUtils.*;

public class PutHandler implements HttpMethodHandler {

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

        String uri = request.getUri();

        try {
            Path path = Paths.get(cfg.getServerRootFolder() + uri);
            File file = path.toFile();
            if ((file.exists()) && (file.isDirectory())){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "cannot overwrite folder", out);
                return;
            }
            if (uri.endsWith("/")){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "missing file name", out);
                return;
            }

            if (!createFolderHierarchy(request, out, responseHeaders, path)) return;

            MessageDigest md = instantiateMD5(request,out,responseHeaders);
            if (md==null)
                return;

            String tempFileName = file.getPath()+"-temp";
            File tempFile = new File(tempFileName);

            if (!copyRequestBody(request, out, responseHeaders, md,false, tempFile)) return;

            if (!renameTemporaryToMainFile(request, out, responseHeaders, file, tempFile)) return;

            FileUtil.createMD5file(file,md);
            writeResponseLine(HttpStatus.NO_CONTENT, out);

            writeHeaders(responseHeaders, out);
            ContextHolder.get().setContentLength("-");

        } catch (InvalidPathException e) {
            serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "invalid characters", out);
            return;
        }

    }



}


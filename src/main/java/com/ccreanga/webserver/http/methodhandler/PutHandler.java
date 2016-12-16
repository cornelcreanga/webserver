package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.common.StringUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.ioutil.FileUtil;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeErrorResponse;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;

public class PutHandler implements HttpMethodHandler {

    public static final Logger serverLog = Logger.getLogger("serverLog");

    @Override
    public void handleResponse(HttpRequestMessage request, Configuration cfg, OutputStream out) throws IOException {

        HttpHeaders responseHeaders = new HttpHeaders();
        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");

        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((!request.hasHeader(HOST)) && (request.isHTTP1_1())) {//host is mandatory
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "missing host header", out);
            return;
        }

        if (request.getUri().contains("..")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.FORBIDDEN, ".. is not allowed", out);
            return;
        }


        if (!cfg.isRootFolderWritable()) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "the root folder is configured read only", out);
            return;
        }

        String contentType = request.getHeader(CONTENT_TYPE);

        if (contentType.contains("application/x-www-form-urlencoded")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
            return;
        }

        if (contentType.contains("multipart/form-data")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
            return;
        }

        String extension = Mime.getExtension(contentType);
        if (extension == null) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "content type " + contentType + " is not known", out);
            return;
        }
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

            try {
                if (!path.getParent().toFile().exists()) {
                    Files.createDirectories(path.getParent());
                }
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", cannot mkdirs for " + uri);
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot create resource", out);
                return;
            }

            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                serverLog.severe("Connection " + ContextHolder.get().getUuid() + ", message is " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR, "cannot create resource", out);
                return;
            }
            try (FileOutputStream outputStream = new FileOutputStream(file);) {
                if (!request.isChunked())
                    IOUtil.copy(request.getBody(), outputStream, 0, request.getLength(),8192,md);
                else
                    IOUtil.copy(request.getBody(), outputStream,-1,-1,8192,md);
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                boolean removed = file.delete();
                if (!removed)
                    serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", can't remove " + file.getPath());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot create resource", out);//todo - refine
                return;
            }
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


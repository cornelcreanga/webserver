package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeErrorResponse;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;

public class DeleteHandler implements HttpMethodHandler {

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

        String uri = request.getUri();

        try {
            Path path = Paths.get(cfg.getServerRootFolder() + uri);
            if (!path.toFile().exists()){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.NOT_FOUND, "not found", out);
                return;

            }
            final List<File> notDeleted = new ArrayList<>();
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(f->{
                        boolean delete = f.delete();
                        if (!delete)
                            notDeleted.add(f);
                    });

            //todo - add list of not deleted
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

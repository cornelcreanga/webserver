package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.representation.FileResourceRepresentation;
import com.ccreanga.webserver.http.representation.RepresentationManager;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
import static com.ccreanga.webserver.http.methodhandler.HandlerUtils.hostHeaderIsPresent;
import static com.ccreanga.webserver.http.methodhandler.HandlerUtils.rootFolderIsWritable;
import static com.ccreanga.webserver.http.methodhandler.HandlerUtils.uriContainsIllegalPath;

public class DeleteHandler implements HttpMethodHandler {

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

            writeResponseLine(HttpStatus.OK, out);
            byte[] body = null;
            String len = "0";
            if (!notDeleted.isEmpty()) {
                FileResourceRepresentation representation =
                        RepresentationManager.getInstance().getRepresentation(request.getHeader(ACCEPT));

                String list = representation.nonDeletedFiles(notDeleted);
                body = list.getBytes(StandardCharsets.UTF_8);
                len = String.valueOf(body.length);
                responseHeaders.putHeader(CONTENT_TYPE, representation.getContentType());
                ContextHolder.get().setContentLength(len);
            }
            responseHeaders.putHeader(CONTENT_LENGTH, len);
            writeHeaders(responseHeaders, out);
            if (!notDeleted.isEmpty())
                out.write(body);

            ContextHolder.get().setContentLength(len);

        } catch (InvalidPathException e) {
            serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "invalid characters", out);
            return;
        }

    }
}

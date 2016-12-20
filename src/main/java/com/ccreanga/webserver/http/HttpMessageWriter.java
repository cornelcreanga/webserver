package com.ccreanga.webserver.http;

import com.ccreanga.webserver.filehandler.representation.FileResourceRepresentation;
import com.ccreanga.webserver.filehandler.representation.RepresentationManager;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.ccreanga.webserver.common.Constants.*;
import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_LENGTH;
import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;

public class HttpMessageWriter {


    public static void writeErrorResponse(String acceptHeader, HttpHeaders responseHeaders, HttpStatus status, String extendedStatus, OutputStream out) throws IOException {
        ContextHolder.get().setStatusCode(status.toString());
        FileResourceRepresentation representation =
                RepresentationManager.getInstance().getRepresentation(acceptHeader);

        String errorHtml = representation.errorRepresentation(status, extendedStatus);
        byte[] body = errorHtml.getBytes(StandardCharsets.UTF_8);
        ContextHolder.get().setContentLength(String.valueOf(body.length));
        responseHeaders.putHeader(CONTENT_LENGTH, String.valueOf(body.length));
        responseHeaders.putHeader(CONTENT_TYPE, representation.getContentType());
        writeResponseLine(status, out);
        writeHeaders(responseHeaders, out);
        out.write(body);
    }

    public static void writeNoBodyResponse(HttpHeaders responseHeaders, HttpStatus status, OutputStream out) throws IOException {
        if (ContextHolder.get() != null)
            ContextHolder.get().setStatusCode(status.toString());
        responseHeaders.putHeader(CONTENT_LENGTH, "0");
        writeResponseLine(status, out);
        writeHeaders(responseHeaders, out);
    }


    public static void writeHeaders(HttpHeaders headers, OutputStream out) throws IOException {
        Map<String, String> headerMap = headers.getAllHeadersMap();
        for (Map.Entry<String, String> e : headerMap.entrySet()) {
            out.write(e.getKey().getBytes(StandardCharsets.ISO_8859_1));
            out.write(HEADER_SEP);
            out.write(e.getValue().getBytes(StandardCharsets.ISO_8859_1));
            out.write(CRLF);
        }
        out.write(CRLF);
    }

    public static void writeResponseLine(HttpStatus status, OutputStream out) throws IOException {
        //we might not even have an context here in case of rejected execution exception
        if (ContextHolder.get() != null)
            ContextHolder.get().setStatusCode(status.toString());
        out.write("HTTP/1.1".getBytes(StandardCharsets.ISO_8859_1));
        out.write(SP);
        out.write(status.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.write(SP);
        out.write((status.getReasonPhrase().getBytes(StandardCharsets.ISO_8859_1)));
        out.write(CRLF);
    }


}

package com.ccreanga.webserver.http;

import com.ccreanga.webserver.http.representation.FileResourceRepresentation;
import com.ccreanga.webserver.http.representation.RepresentationManager;
import com.ccreanga.webserver.logging.ContextHolder;
import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import static com.google.common.net.HttpHeaders.*;

public class HttpMessageWriter {
    private static final byte[] CRLF = {0x0d, 0x0a};
    private static final byte[] SP = {0x20};
    private static final byte[] HEADER_SEP = {0x3A};

    public static void writeErrorResponse(String acceptHeader,HTTPHeaders responseHeaders, HTTPStatus status, String extendedStatus, OutputStream out) throws IOException {
        ContextHolder.get().setStatusCode(status.toString());
        FileResourceRepresentation representation =
                RepresentationManager.getInstance().getRepresentation(acceptHeader);

        String errorHtml = representation.errorRepresentation(status, extendedStatus);
        byte[] body = errorHtml.getBytes(Charsets.UTF_8);
        ContextHolder.get().setContentLength(String.valueOf(body.length));
        responseHeaders.putHeader(CONTENT_LENGTH, String.valueOf(body.length));
        writeResponseLine(status, out);
        writeHeaders(responseHeaders, out);
        out.write(body);
    }

    public static void writeNoBodyResponse(HTTPHeaders responseHeaders, HTTPStatus status, OutputStream out) throws IOException {
        if (ContextHolder.get()!=null)
            ContextHolder.get().setStatusCode(status.toString());
        responseHeaders.putHeader(CONTENT_LENGTH, "0");
        writeResponseLine(status, out);
        writeHeaders(responseHeaders, out);
    }


    public static void writeHeaders(HTTPHeaders headers, OutputStream out) throws IOException {
        Map<String, String> headerMap = headers.getAllHeadersMap();
        for (Map.Entry<String, String> e : headerMap.entrySet()) {
            out.write(e.getKey().getBytes(Charsets.ISO_8859_1));
            out.write(HEADER_SEP);
            out.write(e.getValue().getBytes(Charsets.ISO_8859_1));
            out.write(CRLF);
        }
        out.write(CRLF);
    }

    public static void writeResponseLine(HTTPStatus status, OutputStream out) throws IOException {
        //we might not even have an context here in case of rejected execution exception
        if (ContextHolder.get()!=null)
            ContextHolder.get().setStatusCode(status.toString());
        out.write("HTTP/1.1".getBytes(Charsets.ISO_8859_1));
        out.write(SP);
        out.write(status.toString().getBytes(Charsets.ISO_8859_1));
        out.write(SP);
        out.write((status.getReasonPhrase().getBytes(Charsets.ISO_8859_1)));
        out.write(CRLF);
    }


}

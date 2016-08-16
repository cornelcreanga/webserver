package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.IOException;
import java.io.OutputStream;

import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;
import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;

/**
 * Not supported handler (http status 501)
 */
public class NotSupportedHandler implements HttpMethodHandler {
    @Override
    public void handleGetResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {
        writeResponseLine(HttpStatus.NOT_IMPLEMENTED, out);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putHeader(CONTENT_LENGTH, "0");
        writeHeaders(responseHeaders, out);
        ContextHolder.get().setContentLength("-");
    }
}

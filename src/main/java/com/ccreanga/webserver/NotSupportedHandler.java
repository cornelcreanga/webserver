package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;

import java.io.IOException;
import java.io.OutputStream;

import static com.ccreanga.webserver.MessageWriter.writeHeaders;
import static com.ccreanga.webserver.MessageWriter.writeResponseLine;

public class NotSupportedHandler implements HttpMethodHandler  {
    @Override
    public void handleGetResponse(RequestMessage request, Configuration configuration, OutputStream out) throws IOException {
        writeResponseLine(HTTPStatus.NOT_IMPLEMENTED, out);
        HTTPHeaders responseHeaders = new HTTPHeaders();
        responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, "0");
        writeHeaders(responseHeaders, out);
    }
}

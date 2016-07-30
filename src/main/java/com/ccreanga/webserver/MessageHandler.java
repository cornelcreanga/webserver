package com.ccreanga.webserver;


import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;

import java.io.IOException;
import java.io.OutputStream;

import static com.ccreanga.webserver.MessageWriter.writeHeaders;
import static com.ccreanga.webserver.MessageWriter.writeResponseLine;


public class MessageHandler {


    public void handleMessage(RequestMessage request, Configuration configuration, OutputStream out) throws IOException {
        //EXPECT header is not yet handled
        switch (request.getMethod()) {
            case GET:
                new GetHandler().handleGetResponse(request, true, configuration, out);
                return;
            case HEAD:
                new GetHandler().handleGetResponse(request, false, configuration, out);
                return;
            case POST:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED, out);
                return;
            case PUT:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED, out);
                return;
            case DELETE:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED, out);
                return;
            case CONNECT:
                return;
            case PATCH:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED, out);
                return;
            case TRACE:
                writeResponseLine(HTTPStatus.NOT_IMPLEMENTED, out);
                return;
            case OPTIONS:
                HTTPHeaders responseHeaders = new HTTPHeaders();
                responseHeaders.putHeader(HTTPHeaders.ALLOW, "GET, HEAD, OPTIONS");
                responseHeaders.putHeader(HTTPHeaders.CONTENT_LENGTH, "0");
                writeResponseLine(HTTPStatus.OK, out);
                writeHeaders(responseHeaders, out);
                return;
        }
        throw new InternalException("invalid method " + request.getMethod() + ". this should never happen(internal error)");
    }

}

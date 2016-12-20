package com.ccreanga.webserver.javahandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.http.HttpMessageHandler;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

import static com.ccreanga.webserver.http.HttpHeaders.EXPECT;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;

public class MethodInvokationHandler implements HttpMessageHandler {
    @Override
    public void handleMessage(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {
        if (request.isHTTP1_1() && (request.headerIsEqualWithValue(EXPECT, "100-continue"))) {//in future the result will depend on authentication
            writeResponseLine(HttpStatus.CONTINUE, out);
            out.flush();
        }

        switch (request.getMethod()) {
            case GET:
                new MethodExecutionHandler().handleResponse(request, configuration, out);
                return;
            case HEAD:
            case POST:
            case PUT:
            case DELETE:
            case CONNECT:
                return;
            case PATCH:
            case TRACE:
            case OPTIONS:
        }
        //this should never happen unles we have a bug :)
        throw new InternalException("invalid method " + request.getMethod() + ". this should never happen(internal error)");

    }
}

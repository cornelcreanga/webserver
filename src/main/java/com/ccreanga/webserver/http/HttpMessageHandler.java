package com.ccreanga.webserver.http;


import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.http.methodhandler.GetHandler;
import com.ccreanga.webserver.http.methodhandler.NotSupportedHandler;
import com.ccreanga.webserver.http.methodhandler.OptionsHandler;

import java.io.IOException;
import java.io.OutputStream;


public class HttpMessageHandler {


    public void handleMessage(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {
        //EXPECT header is not yet handled
        switch (request.getMethod()) {
            case GET:
                new GetHandler(true).handleGetResponse(request, configuration, out);
                return;
            case HEAD:
                new GetHandler(false).handleGetResponse(request, configuration, out);
                return;
            case POST:
                new NotSupportedHandler().handleGetResponse(request, configuration, out);
                return;
            case PUT:
                new NotSupportedHandler().handleGetResponse(request, configuration, out);
                return;
            case DELETE:
                new NotSupportedHandler().handleGetResponse(request, configuration, out);
                return;
            case CONNECT:
                return;
            case PATCH:
                new NotSupportedHandler().handleGetResponse(request, configuration, out);
                return;
            case TRACE:
                new NotSupportedHandler().handleGetResponse(request, configuration, out);
                return;
            case OPTIONS:
                new OptionsHandler().handleGetResponse(request, configuration, out);
                return;
        }
        throw new InternalException("invalid method " + request.getMethod() + ". this should never happen(internal error)");
    }

}

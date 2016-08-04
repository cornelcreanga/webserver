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

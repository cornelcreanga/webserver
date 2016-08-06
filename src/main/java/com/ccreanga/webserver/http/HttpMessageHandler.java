package com.ccreanga.webserver.http;


import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.http.methodhandler.GetHandler;
import com.ccreanga.webserver.http.methodhandler.NotSupportedHandler;
import com.ccreanga.webserver.http.methodhandler.OptionsHandler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Finds the proper message handler taking into account the HTTP method.
 */
public class HttpMessageHandler {

    /**
     * Handles the message (by writing the response into the output stream) taking into account the HTTP method (GET/POST/PUT etc).
     * Not all the methods are implemented right now - for them the status will be 501
     * @param request - valid HttpRequestMessage
     * @param configuration - configuration
     * @param out - outputstrem
     * @throws IOException - in case of I/O error
     */
    public void handleMessage(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {
        //EXPECT header is not yet handled
        switch (request.getMethod()) {
            case GET:
                new GetHandler(true).handleGetResponse(request, configuration, out);
                return;
            case HEAD:
                //head is GET without the body
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
        //this should never happen unles we have a bug :)
        throw new InternalException("invalid method " + request.getMethod() + ". this should never happen(internal error)");
    }

}
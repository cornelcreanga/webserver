package com.ccreanga.webserver.filehandler;


import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.http.HttpMessageHandler;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

import static com.ccreanga.webserver.http.HttpHeaders.EXPECT;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;

/**
 * Finds the proper message handler taking into account the HTTP method.
 */
public class FileMessageHandler implements HttpMessageHandler {

    /**
     * Handles the message (by writing the response into the output stream) taking into account the HTTP method (GET/POST/PUT etc).
     * Not all the methods are implemented right now - for them the status will be 501
     *
     * @param request       - valid HttpRequestMessage
     * @param configuration - configuration
     * @param out           - outputstrem
     * @throws IOException - in case of I/O error
     */
    @Override
    public void handleMessage(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {

        if (request.isHTTP1_1() && (request.headerIsEqualWithValue(EXPECT, "100-continue"))) {//in future the result will depend on authentication
            writeResponseLine(HttpStatus.CONTINUE, out);
            out.flush();
        }

        switch (request.getMethod()) {
            case GET:
                new GetHandler(true).handleResponse(request, configuration, out);
                return;
            case HEAD:
                //head is GET without the body
                new GetHandler(false).handleResponse(request, configuration, out);
                return;
            case POST:
                new PostHandler().handleResponse(request, configuration, out);
                return;
            case PUT:
                new PutHandler().handleResponse(request, configuration, out);
                return;
            case DELETE:
                new DeleteHandler().handleResponse(request, configuration, out);
                return;
            case CONNECT:
                return;
            case PATCH:
                new PatchHandler().handleResponse(request, configuration, out);
                return;
            case TRACE:
                new TraceHandler().handleResponse(request, configuration, out);
                return;
            case OPTIONS:
                new OptionsHandler().handleResponse(request, configuration, out);
                return;
        }
        //this should never happen unles we have a bug :)
        throw new InternalException("invalid method " + request.getMethod() + ". this should never happen(internal error)");
    }

}

package com.ccreanga.webserver.javahandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.http.HttpMethodHandler;
import com.ccreanga.webserver.http.HttpRequestMessage;

import java.io.IOException;
import java.io.OutputStream;

public class MethodExecutionHandler implements HttpMethodHandler {
    @Override
    public void handleResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {

    }
}

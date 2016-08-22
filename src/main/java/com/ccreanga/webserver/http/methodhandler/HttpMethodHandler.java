package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.http.HttpRequestMessage;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpMethodHandler {
    void handleResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException;
}

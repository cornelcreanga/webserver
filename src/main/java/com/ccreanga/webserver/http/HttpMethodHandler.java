package com.ccreanga.webserver.http;

import com.ccreanga.webserver.Configuration;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpMethodHandler {
    void handleResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException;
}

package com.ccreanga.webserver.http;

import com.ccreanga.webserver.Configuration;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpMessageHandler {
    void handleMessage(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException;
}

package com.ccreanga.webserver;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpMethodHandler {
    void handleGetResponse(RequestMessage request, Configuration configuration, OutputStream out) throws IOException;
}

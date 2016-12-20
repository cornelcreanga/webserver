package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HttpMessageHandler;

import java.net.Socket;

public interface ConnectionProcessor {

    void handleConnection(Socket socket, HttpMessageHandler httpMessageHandler, Configuration configuration);

}

package com.ccreanga.webserver;

import java.net.Socket;

public interface ConnectionProcessor {

    void handleConnection(Socket socket, Configuration configuration);

}

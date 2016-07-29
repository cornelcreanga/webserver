package com.ccreanga.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerShutDownHook implements Runnable{
    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");

    private Server server;

    public ServerShutDownHook(Server server) {
        this.server = server;
    }

    public void run() {
        server.stop();
        serverLog.info("server is shutting down.");
    }
}

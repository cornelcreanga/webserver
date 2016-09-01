package com.ccreanga.webserver;


import static com.ccreanga.webserver.Server.serverLog;

public class ServerShutDownHook implements Runnable {

    private Server server;

    public ServerShutDownHook(Server server) {
        this.server = server;
    }

    public void run() {
        server.stop();
        serverLog.info("Server is stopped.");
    }
}

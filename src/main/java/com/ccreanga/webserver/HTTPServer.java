package com.ccreanga.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class HTTPServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HTTPServer.class);

    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Config config;
    private ExecutorService threadPool;

    public HTTPServer(Config config) {
        this.config = config;
    }

    public void run() {
        log.info("Server started.");
        try {
            serverSocket = new ServerSocket(config.getServerPort());
            while (!isStopped()) {
                Socket socket = serverSocket.accept();
                socket.setSoLinger(false, -1);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(config.getTimeoutSeconds()*1000);
                try {
                    threadPool.execute(new PersistentConnectionProcessor(socket));
                } catch (RejectedExecutionException e) {
                    new ResponseMessageWriter().writeRequestError(socket.getOutputStream(),HttpStatus.SERVICE_UNAVAILABLE);
                }
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("Fatal error.");
        }
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stop() {
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    public static void main(String[] args) {
        Config config = (args.length>0)?new Config(args[0]):new Config();
        HTTPServer httpServer = new HTTPServer(config);
        System.out.println("running");
        System.out.println(Mime.getType("ec"));
    }

}

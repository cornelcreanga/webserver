package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    private ExecutorService threadPool;


    public Server() {
        ServerConfiguration serverConfiguration = ServerConfiguration.instance();
        threadPool = new ThreadPoolExecutor(
                serverConfiguration.getInitialThreads(),
                serverConfiguration.getMaxThreads(),
                60,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(serverConfiguration.getWaitQueue()));
    }

    public Server(String configurationPath) {
        super();


    }

    public void run() {
        log.info("Server started.");
        ServerConfiguration serverConfiguration = ServerConfiguration.instance();
        try {
            serverSocket = new ServerSocket(serverConfiguration.getServerPort());
            while (!isStopped()) {
                Socket socket = serverSocket.accept();
                //socket.setSoLinger(false, -1); todo - thing again
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(serverConfiguration.getTimeoutSeconds()*1000);
                try {
                    threadPool.execute(new PersistentConnectionProcessor(socket));
                } catch (RejectedExecutionException e) {
                    new ResponseMessageWriter().writeRequestError(socket.getOutputStream(), HttpStatus.SERVICE_UNAVAILABLE);
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

        if (args.length>0)
            ServerConfiguration.reload(args[1]);

        Server server = new Server();

        try {
            new Thread(server).start();
        } catch (InternalException e) {
            e.printStackTrace();//todo
            server.stop();
        }

    }

}

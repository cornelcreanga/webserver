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
    private Configuration configuration = new Configuration();


    public Server() {
    }

    public Server(Configuration configuration) {
        super();
        this.configuration = configuration;


    }

    public void run() {
        log.info("Starting server...");
        threadPool = new ThreadPoolExecutor(
                configuration.getInitialThreads(),
                configuration.getMaxThreads(),
                60,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(configuration.getWaitQueue()));

        try {

//            serverSocket = ServerSocketFactory.getDefault().createServerSocket(configuration.getServerPort());
            serverSocket = new ServerSocket(configuration.getServerPort());
            log.info("Server is started,listening on "+configuration.getServerPort());
            while (!isStopped()) {
                Socket socket = serverSocket.accept();
                //socket.setSoLinger(false, -1); todo - thing again
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(configuration.getTimeoutSeconds()*1000);
                try {
                    threadPool.execute(new PersistentConnectionProcessor(socket,configuration));
                } catch (RejectedExecutionException e) {
                    new ResponseMessageWriter().writeRequestError(socket.getOutputStream(), HttpStatus.SERVICE_UNAVAILABLE);
                }
            }
            threadPool.shutdown();
        } catch (IOException e) {
            //todo
            if (!isStopped) {
                e.printStackTrace();
                log.error("Fatal error.");
            }
        }
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stop() {
        isStopped = true;
        try {
            if (serverSocket!=null)
                serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    public static void main(String[] args) {

        Configuration configuration = new Configuration();
        if (args.length>0)
            configuration.loadFromFile(args[1]);

        Server server = new Server(configuration);

        try {
            new Thread(server).start();
        } catch (InternalException e) {
            e.printStackTrace();//todo
            server.stop();
        }

    }

}

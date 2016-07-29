package com.ccreanga.webserver;

import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server implements Runnable {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");

    private boolean isStopped = false;
    private ExecutorService threadPool;
    private Configuration configuration = new Configuration();


    public Server() {
    }

    public Server(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    public void run() {
        serverLog.info("Starting the server...");
        threadPool = new ThreadPoolExecutor(
                configuration.getInitialThreads(),
                configuration.getMaxThreads(),
                60,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(configuration.getWaitQueue()));


        try(ServerSocket serverSocket = new ServerSocket(configuration.getServerPort())) {
            serverLog.info("Server started,listening on "+configuration.getServerPort()+".");
            while (!isStopped()) {
                try {
                    final Socket socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    socket.setSoTimeout(configuration.getTimeoutSeconds() * 1000);
                    try {
                        threadPool.execute(() -> {
                            try {
                                //always create persistent connections (support http 1.1)
                                ConnectionProcessor connectionProcessor = new PersistentConnectionProcessor();
                                connectionProcessor.handleConnection(socket, configuration);
                            }finally{
                                try {
                                    Closeables.close(socket,true);
                                } catch (IOException e) {/**ignore**/}
                            }
                        });

                    } catch (RejectedExecutionException e) {
                        //todo - new ResponseMessageWriter().writeRequestError(socket.getOutputStream(), HTTPStatus.SERVICE_UNAVAILABLE);
                    }
                }catch (IOException e){
                    serverLog.info(e.getMessage());
                }
            }

        } catch (IOException e) {
            serverLog.info("Fatal error: "+e.getMessage());
        } finally {
            threadPool.shutdown();
        }

        serverLog.info("Server stopped.");
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stop() {
        serverLog.info("Stopping the server...");
        isStopped = true;
    }

    public static void main(String[] args) {

        Configuration configuration = new Configuration();
        if (args.length>0)
            configuration.loadFromFile(args[1]);

        Server server = new Server(configuration);
        Runtime.getRuntime().addShutdownHook(new Thread(new ServerShutDownHook(server)));

        try {
            new Thread(server).start();
        } catch (InternalException e) {
            e.printStackTrace();//todo
            server.stop();
        }

    }

}

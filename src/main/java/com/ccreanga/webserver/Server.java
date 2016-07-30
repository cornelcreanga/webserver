package com.ccreanga.webserver;

import ch.qos.logback.classic.Level;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.logging.Context;
import com.ccreanga.webserver.logging.ContextHolder;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.*;

public class Server implements Runnable {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");

    private boolean isStopped = false;
    private ExecutorService threadPool;
    private Configuration configuration;


    public Server() {
        this(new Configuration());
    }

    public Server(Configuration configuration) {
        this.configuration = configuration;
        ch.qos.logback.classic.Logger serverLog = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("serverLog");
        serverLog.setLevel(configuration.isVerbose() ? Level.TRACE : Level.INFO);
    }

    public void run() {
        serverLog.info("Starting the server...");
        threadPool = new ThreadPoolExecutor(
                configuration.getServerInitialThreads(),
                configuration.getServerMaxThreads(),
                60,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(configuration.getRequestWaitingQueueSize()));


        try (ServerSocket serverSocket = new ServerSocket(configuration.getServerPort())) {
            serverLog.info("Server started,listening on " + configuration.getServerPort() + ".");
            while (!isStopped()) {
                try {
                    final Socket socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    socket.setSoTimeout(configuration.getRequestTimeoutSeconds() * 1000);
                    try {
                        threadPool.execute(() -> {
                            try {
                                ContextHolder.put(new Context());
                                UUID uuid = UUID.randomUUID();
                                ContextHolder.get().setUuid(uuid);
                                String ip = getIp(socket);
                                ContextHolder.get().setIp(ip);
                                serverLog.trace("Connection from ip " + ip + " started, uuid=" + uuid);

                                ConnectionProcessor connectionProcessor = new PersistentConnectionProcessor();
                                connectionProcessor.handleConnection(socket, configuration);
                            } finally {
                                try {
                                    Closeables.close(socket, true);
                                } catch (IOException e) {/**ignore**/}
                            }
                        });

                    } catch (RejectedExecutionException e) {
                        MessageWriter.writeErrorResponse(new HTTPHeaders(), HTTPStatus.SERVICE_UNAVAILABLE, "", socket.getOutputStream());
                    }
                } catch (IOException e) {
                    serverLog.trace(e.getMessage());
                }
            }

        } catch (IOException e) {
            serverLog.error("Fatal error: " + e.getMessage());
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

    private String getIp(Socket socket) {
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetAddress inetAddress = ((InetSocketAddress) socketAddress).getAddress();
            return inetAddress.toString();
        }
        return "Not an IP socket";

    }


    public static void main(String[] args) {

        Configuration configuration = new Configuration();
        if (args.length > 0)
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

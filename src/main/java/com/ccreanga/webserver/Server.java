package com.ccreanga.webserver;

import ch.qos.logback.classic.Level;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.HttpConnectionProcessor;
import com.ccreanga.webserver.http.HttpMessageWriter;
import com.ccreanga.webserver.logging.Context;
import com.ccreanga.webserver.logging.ContextHolder;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.*;

import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_LOG;

public class Server implements Runnable {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");

    private boolean shouldStop = false;
    private boolean isStopped = false;
    private boolean isReady = false;

    private Configuration configuration;
    private ServerSocket serverSocket;


    public Server() {
        this(new Configuration());
    }

    public Server(Configuration configuration) {
        this.configuration = configuration;
        ch.qos.logback.classic.Logger serverLog = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("serverLog");
        serverLog.setLevel(configuration.isVerbose() ? Level.TRACE : Level.INFO);
    }

    private void initContext(UUID uuid,String ip){
        ContextHolder.put(new Context());
        ContextHolder.get().setUuid(uuid);
        ContextHolder.get().setIp(ip);
        ContextHolder.get().setDate(DateUtil.currentDate(FORMATTER_LOG));
    }

    public void run() {
        serverLog.info("Starting the server...");
        ExecutorService threadPool = new ThreadPoolExecutor(
                configuration.getServerInitialThreads(),
                configuration.getServerMaxThreads(),
                60,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(configuration.getRequestWaitingQueueSize()));


        try {
            serverSocket = new ServerSocket(configuration.getServerPort());
            serverLog.info("Server started,listening on " + configuration.getServerPort() + ". Files will be served from " + configuration.getServerRootFolder());
            isReady = true;
            while (!shouldStop) {
                try {
                    final Socket socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    socket.setSoTimeout(configuration.getRequestTimeoutSeconds() * 1000);
                    try {
                        threadPool.execute(() -> {
                            try {

                                UUID uuid = UUID.randomUUID();
                                String ip = getIp(socket);
                                initContext(uuid,ip);
                                serverLog.trace("Connection from ip " + ip + " started, uuid=" + uuid);

                                ConnectionProcessor connectionProcessor = new HttpConnectionProcessor();
                                connectionProcessor.handleConnection(socket, configuration);
                            } finally {
                                ContextHolder.cleanup();
                                try {
                                    Closeables.close(socket, true);
                                } catch (IOException e) {/**ignore**/}
                            }
                        });

                    } catch (RejectedExecutionException e) {
                        HttpMessageWriter.writeErrorResponse(new HTTPHeaders(), HTTPStatus.SERVICE_UNAVAILABLE, "", socket.getOutputStream());
                    }
                } catch (IOException e) {
                    serverLog.trace(e.getMessage());
                }
            }

        } catch (IOException e) {
            serverLog.error("Fatal error: " + e.getMessage());
        } finally {
            try {
                Closeables.close(serverSocket, true);
            } catch (IOException e) {/**ignore**/}
            threadPool.shutdown();
        }
        isStopped = true;
    }

    public synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized boolean isReady() {
        return isReady;
    }

    public synchronized void stop() {
        serverLog.info("Stopping the server...");
        shouldStop = true;
        try {
            Closeables.close(serverSocket, true);
        } catch (IOException e) {/**ignore**/}
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

        Server server = null;
        try {
            Configuration configuration = new Configuration();
            if (args.length > 0)
                configuration.loadFromFile(args[0]);

            server = new Server(configuration);
            Runtime.getRuntime().addShutdownHook(new Thread(new ServerShutDownHook(server)));


            new Thread(server).start();
        } catch (InternalException e) {
            serverLog.error(e.getMessage());
            if (server != null)
                server.stop();
        }

    }

}

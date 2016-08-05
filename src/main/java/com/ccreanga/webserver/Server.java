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

import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_LOG;

public class Server implements Runnable {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    private static final Logger accessLog = LoggerFactory.getLogger("accessLog");

    private boolean shouldStop = false;
    private boolean isStopped = false;
    private boolean isReady = false;

    private Configuration configuration;
    private ServerSocket serverSocket;


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
                        HttpMessageWriter.writeNoBodyResponse(new HTTPHeaders(),HTTPStatus.SERVICE_UNAVAILABLE,socket.getOutputStream());
                        logEntry(socket);
                        socket.close();

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

    private void logEntry(Socket socket){
        StringBuilder sb = new StringBuilder(128);
        sb.append(getIp(socket)).append('\t');
        sb.append('-').append('\t');
        sb.append(DateUtil.currentDate(FORMATTER_LOG)).append('\t');
        sb.append('-').append('\t');
        sb.append(HTTPStatus.SERVICE_UNAVAILABLE).append('\t');
        sb.append('-').append('\r');
        accessLog.info(sb.toString());
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
            Properties properties = null;
            if (args.length > 0) {
                try {
                    properties = new Properties();
                    properties.load(new FileReader(args[0]));
                } catch (IOException e) {
                    serverLog.error("cannot load file " + args[0]+", error is:"+e.getMessage());
                    System.exit(-1);
                }
            }else{
                properties = new Properties();
                properties.put("serverPort", "8082");
                properties.put("serverRootFolder", "/var/www/html");
                properties.put("serverInitialThreads", "128");
                properties.put("serverMaxThreads", "1000");
                properties.put("requestTimeoutSeconds", "5");
                properties.put("requestWaitingQueueSize", "64");
                properties.put("requestEtag", "weak");
                properties.put("requestMaxLines", "200");
                properties.put("requestMaxLineLength", "1024");
                properties.put("requestMaxHeaders", "64");
                properties.put("requestMaxGetBodySize", "64000");
                properties.put("requestMaxPutBodySize", "2147483648");
                properties.put("verbose", "true");
            }
            Configuration configuration = new Configuration(properties);

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

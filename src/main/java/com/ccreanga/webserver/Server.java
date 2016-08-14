package com.ccreanga.webserver;

import ch.qos.logback.classic.Level;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.HttpConnectionProcessor;
import com.ccreanga.webserver.http.HttpMessageWriter;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.Context;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.logging.LogEntry;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_LOG;

/**
 * Main server class
 */
public class Server implements Runnable {

    /**
     * Used to log messages to console
     */
    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    /**
     * Used for building the access log
     */
    private static final Logger accessLog = LoggerFactory.getLogger("accessLog");

    private boolean shouldStop = false;
    /**
     * True if the server is stopped and not accepting any connection
     */
    private boolean isStopped = false;
    /**
     * True is the server is up and ready to accept connections
     */
    private boolean isReady = false;

    private Configuration configuration;
    private ServerSocket serverSocket;


    public Server(Configuration configuration) {

        this.configuration = Preconditions.checkNotNull(configuration);
        //configure log level
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
                TimeUnit.SECONDS,
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
                                //when logging the events related to a a connection will also print the connection associated UUID in order to make the debugging easier
                                UUID uuid = UUID.randomUUID();
                                String ip = IOUtil.getIp(socket);
                                initContext(uuid,ip);
                                serverLog.trace("Connection from ip " + ip + " started, uuid=" + uuid);

                                ConnectionProcessor connectionProcessor = new HttpConnectionProcessor();
                                connectionProcessor.handleConnection(socket, configuration);

                            } finally {
                                //clear the thread local
                                ContextHolder.cleanup();
                                IOUtil.closeSocketPreventingReset(socket);
                            }
                        });

                    } catch (RejectedExecutionException e) {
                        //if the server will have to reject connection because there is no available thread and the
                        //waiting queue is full it will return SERVICE_UNAVAILABLE
                        HttpMessageWriter.writeNoBodyResponse(new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE,socket.getOutputStream());
                        accessLog.info(LogEntry.generateLogEntry(
                                IOUtil.getIp(socket),
                                "-",
                                DateUtil.currentDate(DateUtil.FORMATTER_LOG),
                                "-",
                                HttpStatus.SERVICE_UNAVAILABLE.toString(),
                                "-"));
                        IOUtil.closeSocketPreventingReset(socket);

                    }
                } catch (IOException e) {
                    serverLog.trace(e.getMessage());
                }
            }

        } catch (IOException e) {
            //we cannot create the serversocket, we'll shutdown
            serverLog.error("Fatal error: " + e.getMessage());
        } finally {
            IOUtil.closeSilent(serverSocket);
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
            //close the socket otherwise the main loop is blocked in serverSocket.accept();
            Closeables.close(serverSocket, true);
        } catch (IOException e) {/**ignore**/}
    }



    public static void main(String[] args) {

        Server server = null;
        try {
            Properties properties = null;
            //if the properties files is not passed as a parameter will use the default values
            if (args.length > 0) {
                try {
                    properties = new Properties();
                    properties.load(new FileReader(args[0]));
                } catch (IOException e) {
                    serverLog.error("cannot load file " + args[0]+", error is:"+e.getMessage());
                    System.exit(-1);
                }
            }else{
                serverLog.info("No configuration file was passed as parameter, will use the default values.");
                properties = new Properties();
                properties.put("serverPort", "8082");
                properties.put("serverRootFolder", "/var/www/html");
                properties.put("serverInitialThreads", "128");
                properties.put("serverMaxThreads", "1000");
                properties.put("requestTimeoutSeconds", "5");
                properties.put("requestWaitingQueueSize", "64");
                properties.put("requestEtag", "weak");

                properties.put("requestURIMaxSize", "8000");
                properties.put("requestMessageBodyMaxSize", "1073741824");
                properties.put("requestMaxLineLength", "1024");
                properties.put("requestMaxHeaders", "64");
                properties.put("verbose", "false");

            }
            Configuration configuration = null;
            try {
                configuration = new Configuration(properties);
            }catch (ConfigurationException e){
                serverLog.error("configuration error, cannot start the server");
                serverLog.error(e.getMessage());
                System.exit(-1);
            }

            server = new Server(configuration);
            Runtime.getRuntime().addShutdownHook(new Thread(new ServerShutDownHook(server)));

            new Thread(server).start();
        } catch (InternalException e) {
            //internal exception should never happen unless something unexpected happens
            // (like having a corrupted jar without templates or an internal bug etc)
            serverLog.error(e.getMessage());
            if (server != null)
                server.stop();
        }

    }

}

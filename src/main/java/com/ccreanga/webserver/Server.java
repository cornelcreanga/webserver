package com.ccreanga.webserver;

import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.common.SimpleFormatter;
import com.ccreanga.webserver.filehandler.FileMessageHandler;
import com.ccreanga.webserver.http.*;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.Context;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.logging.LogEntry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_LOG;

/**
 * Main server class
 */
public class Server implements Runnable {

    /**
     * Used to log messages to console
     */
    public static final Logger serverLog = Logger.getLogger("serverLog");
    /**
     * Used for building the access log
     */
    public static final Logger accessLog = Logger.getLogger("accessLog");

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
    private HttpMessageHandler httpMessageHandler;

    public Server(HttpMessageHandler httpMessageHandler,Configuration configuration) {

        this.httpMessageHandler = httpMessageHandler;
        this.configuration = configuration;
    }

    private void initContext(UUID uuid, String ip) {
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
            File file = new File(configuration.getServerRootFolder());
            if (!file.exists() || !file.isDirectory())
                serverLog.warning("root folder does not exists yet...");
            if (file.isDirectory() && !file.canWrite() && configuration.isRootFolderWritable())
                serverLog.warning("root folder was configured writable but it does not have yet the required permissions...");
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
                                initContext(uuid, ip);
                                serverLog.fine("Connection from ip " + ip + " started, uuid=" + uuid);

                                ConnectionProcessor connectionProcessor = new HttpConnectionProcessor();
                                connectionProcessor.handleConnection(socket,httpMessageHandler, configuration);

                            } finally {
                                //clear the thread local
                                ContextHolder.cleanup();
                                IOUtil.closeSocketPreventingReset(socket);
                            }
                        });

                    } catch (RejectedExecutionException e) {
                        //if the server will have to reject connection because there is no available thread and the
                        //waiting queue is full it will return SERVICE_UNAVAILABLE
                        HttpMessageWriter.writeNoBodyResponse(new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE, socket.getOutputStream());
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
                    serverLog.fine(e.getMessage());
                }
            }

        } catch (IOException e) {
            //we cannot create the serversocket, we'll shutdown
            serverLog.severe("Fatal error: " + e.getMessage());
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
        //close the socket otherwise the main loop is blocked in serverSocket.accept();
        IOUtil.closeSilent(serverSocket);

    }


    public static void main(String[] args) {

        Server server = null;
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter("%5$s%6$s%n"));
        consoleHandler.setLevel(Level.INFO);
        serverLog.setUseParentHandlers(false);
        accessLog.setUseParentHandlers(false);
        serverLog.addHandler(consoleHandler);
        serverLog.setLevel(Level.INFO);


        try {
            Properties properties = null;
            //if the properties files is not passed as a parameter will use the default values
            if (args.length > 0) {
                try {
                    properties = new Properties();
                    properties.load(new FileReader(args[0]));
                } catch (IOException e) {
                    serverLog.severe("cannot load file " + args[0] + ", error is:" + e.getMessage());
                    System.exit(-1);
                }
            } else {
                serverLog.info("No configuration file was passed as parameter, will use the default values.");
                properties = new Properties();
                properties.put("serverPort", "8082");
                properties.put("serverRootFolder", "/var/www/html");
                properties.put("rootFolderWritable", "true");

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
            } catch (ConfigurationException e) {
                serverLog.severe("configuration error, cannot start the server");
                serverLog.severe(e.getMessage());
                System.exit(-1);
            }

            server = new Server(new FileMessageHandler(),configuration);
            Runtime.getRuntime().addShutdownHook(new Thread(new ServerShutDownHook(server)));

            new Thread(server).start();
        } catch (InternalException e) {
            //internal exception should never happen unless something unexpected happens
            // (like having a corrupted jar without templates or an internal bug etc)
            serverLog.severe(e.getMessage());
            if (server != null)
                server.stop();
        }

    }

}

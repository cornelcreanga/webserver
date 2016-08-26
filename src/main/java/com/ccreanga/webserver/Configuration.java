package com.ccreanga.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;


public class Configuration {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    public static final String ETAG_NONE = "none";
    public static final String ETAG_WEAK = "weak";
    public static final String ETAG_STRONG = "strong"; //not used yet

    /**
     * Server port
     */
    private int serverPort;
    /**
     * Server root folder
     */
    private String serverRootFolder;
    /**
     * Initial no of threads, it should belong in [1..1024]
     */
    private int serverInitialThreads;
    /**
     * Max no of threads, it should belong in [1..1024]
     */
    private int serverMaxThreads;

    /**
     * Timeout for keepalive connections, it should belong in [1..3600]
     */
    private int requestTimeoutSeconds;

    /**
     * Thread pool waiting queue, it should belong in [1..3600]
     */
    private int requestWaitingQueueSize;

    /**
     * Should the server generate etags? Right now it only supports none/weak
     */
    private String requestEtag;

    /**
     * The maximum length of a request line, it should belong in [8..65535]
     */
    private int requestMaxLineLength;
    /**
     * The maximum amount of headers, it should belong in [8..65535]
     */

    private int requestMessageBodyMaxSize;

    private int requestURIMaxSize;

    private int requestMaxHeaders;

    /**
     * If true the server will display debug informations
     */
    private boolean verbose;

    private Properties properties;


    public Configuration(Properties properties) {
        this.properties = properties;
        load();
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerRootFolder() {
        return serverRootFolder;
    }

    public int getServerInitialThreads() {
        return serverInitialThreads;
    }

    public int getServerMaxThreads() {
        return serverMaxThreads;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public int getRequestWaitingQueueSize() {
        return requestWaitingQueueSize;
    }

    public String getRequestEtag() {
        return requestEtag;
    }

    public int getRequestMaxLineLength() {
        return requestMaxLineLength;
    }

    public int getRequestMessageBodyMaxSize() {
        return requestMessageBodyMaxSize;
    }

    public int getRequestURIMaxSize() {
        return requestURIMaxSize;
    }

    public int getRequestMaxHeaders() {
        return requestMaxHeaders;
    }

    private void load() {
        serverPort = parseInt("serverPort", 1, 65535);
        serverRootFolder = (String) properties.get("serverRootFolder");
        if (serverRootFolder == null || serverRootFolder.trim().isEmpty())
            throw new ConfigurationException("serverRootFolder is missing or is empty");
        File root = new File(serverRootFolder);
        if (root.exists() && !root.isDirectory())
            throw new ConfigurationException(root.getAbsolutePath() + " is not a folder");
        if (root.exists() && root.isDirectory() && !root.canRead())
            throw new ConfigurationException(root.getAbsolutePath() + " can't be read (permission rights?)");
        if (serverRootFolder.equals("/"))
            throw new ConfigurationException("Root folder can't be /");
        if (!root.exists())
            serverLog.info("Warning:" + root.getAbsolutePath() + " does not exists yet");

        serverInitialThreads = parseInt("serverInitialThreads", 1, 1024);
        serverMaxThreads = parseInt("serverMaxThreads", 1, 1024);

        if (serverMaxThreads < serverInitialThreads)
            throw new ConfigurationException("serverMaxThreads is lower than serverInitialThreads " + serverMaxThreads + "<" + serverInitialThreads);

        requestTimeoutSeconds = parseInt("requestTimeoutSeconds", 1, 3600);

        requestWaitingQueueSize = parseInt("requestWaitingQueueSize", 1, 1000);
        requestEtag = (String) properties.get("requestEtag");
        if (requestEtag == null)
            throw new ConfigurationException("missing requestEtag value");
        if ((!requestEtag.equals(ETAG_NONE)) && (!requestEtag.equals(ETAG_WEAK)))
            throw new IllegalArgumentException("unknown etag:" + requestEtag + "; it should be none or weak");


        requestURIMaxSize = parseInt("requestURIMaxSize", 256, 8 * 1204);

        requestMessageBodyMaxSize = parseInt("requestMessageBodyMaxSize", 256, 1073741824);
        requestMaxLineLength = parseInt("requestMaxLineLength", 256, 10 * 1024);
        requestMaxHeaders = parseInt("requestMaxHeaders", 8, 65535);

        if (properties.get("verbose") == null)
            throw new ConfigurationException("missing verbose value");

        verbose = Boolean.valueOf((String) properties.get("verbose"));


    }

    private int parseInt(String name, int min, int max) {
        try {
            String string = (String) properties.get(name);
            if (string == null)
                throw new ConfigurationException("Cannot find the value " + name);
            return (int)ParseUtil.parseLong(string,min, max);
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Error when trying to configure " + name + " - "+e.getMessage());
        }
    }


    public boolean isVerbose() {
        return verbose;
    }
}

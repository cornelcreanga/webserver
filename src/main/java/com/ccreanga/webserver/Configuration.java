package com.ccreanga.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class Configuration {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    public static final String ETAG_NONE = "none";
    public static final String ETAG_WEAK = "weak";
    public static final String ETAG_STRONG = "strong"; //not used yet

    private int serverPort = 8082;
    private String serverRootFolder = "/var/www/html";

    private int serverInitialThreads = 128;
    private int serverMaxThreads = 1000;

    private int requestTimeoutSeconds = 10;

    private int requestWaitingQueueSize = 64;

    private String requestEtag = ETAG_WEAK;//"weak/none". in future maybe support strong too

    private int requestMaxLines = 200;
    private int requestMaxLineLength = 1024;
    private int requestMaxHeaders = 64;
    private int requestMaxGetBodySize = 64000;
    private long requestMaxPutBodySize = 2147483648L;


    private boolean verbose = true;
    private int chunkLength = 128 * 1024;

    private Properties properties = new Properties();

    public Configuration() {
    }

    ;

    public void loadFromProperties(Properties properties) {
        this.properties = (Properties) properties.clone();
        load();
    }

    public void loadFromFile(String file) {
        try {
            properties.load(new FileReader(file));
        } catch (IOException e) {
            serverLog.error("cannot load file " + file);
            System.exit(-1);
        }
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

    public int getRequestMaxLines() {
        return requestMaxLines;
    }

    public int getRequestMaxLineLength() {
        return requestMaxLineLength;
    }

    public int getRequestMaxHeaders() {
        return requestMaxHeaders;
    }

    public int getRequestMaxGetBodySize() {
        return requestMaxGetBodySize;
    }

    public long getRequestMaxPutBodySize() {
        return requestMaxPutBodySize;
    }

    private void load() {
        try {
            serverPort = Integer.parseInt((String) properties.get("serverPort"));
            serverRootFolder = (String) properties.get("serverRootFolder");
            serverInitialThreads = Integer.parseInt((String) properties.get("serverInitialThreads"));
            serverMaxThreads = Integer.parseInt((String) properties.get("serverMaxThreads"));

            requestTimeoutSeconds = Integer.parseInt((String) properties.get("requestTimeoutSeconds"));
            requestWaitingQueueSize = Integer.parseInt((String) properties.get("requestWaitingQueueSize"));
            requestEtag = (String) properties.get("requestEtag");
            if ((!requestEtag.equals(ETAG_NONE)) && (!requestEtag.equals(ETAG_WEAK)))
                throw new IllegalArgumentException("unknown etag:"+requestEtag+"; it should be none or weak");
            requestMaxLines = Integer.parseInt((String) properties.get("requestMaxLines"));
            requestMaxLineLength = Integer.parseInt((String) properties.get("requestMaxLineLength"));
            requestMaxHeaders = Integer.parseInt((String) properties.get("requestMaxHeaders"));
            requestMaxGetBodySize = Integer.parseInt((String) properties.get("requestMaxGetBodySize"));
            requestMaxPutBodySize = Long.parseLong((String) properties.get("requestMaxPutBodySize"));

            verbose = Boolean.getBoolean((String) properties.get("verbose"));
            chunkLength = Integer.parseInt((String) properties.get("chunkLength"));

        } catch (Exception e) {
            //todo - add more details
            serverLog.error("properties cannot be parsed");
            System.exit(-1);
        }
    }


    public int getChunkLength() {
        return chunkLength;
    }

    public boolean isVerbose() {
        return verbose;
    }
}

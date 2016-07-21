package com.ccreanga.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;



public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private int serverPort = 8080;
    private String rootFolder;
    private int initialThreads = 128;
    private int maxThreads = 5000;
    private int waitQueue = 64;
    private boolean weakEtag = true;
    private int maxGetSize = 4096;
    private int maxHeaders = 64;
    private int timeoutSeconds = 5000;

    private int maxGetBodySize = 64000;
    private long maxPutBodySize = 2147483648L;


    private boolean xForwardedForTag = false;
    private int chunkLength = 128 * 1024;
    private long maxEntitySize = 2147483648L;


    private final String requestGetEncoding = "ISO8859_1";
    
    private Properties properties = new Properties();

    public Config(String file) {
        try {
            properties.load(new FileReader(file));
        } catch (IOException e) {
            log.error("cannot load file "+file);
            System.exit(-1);
        }
        try{
            timeoutSeconds = Integer.parseInt((String) properties.get("timeoutSeconds"));
            rootFolder = (String) properties.get("rootFolder");
            serverPort = Integer.parseInt((String) properties.get("serverPort"));
            initialThreads = Integer.parseInt((String) properties.get("initialThreads"));
            maxThreads = Integer.parseInt((String) properties.get("maxThreads"));
            waitQueue = Integer.parseInt((String) properties.get("waitQueue"));
            weakEtag = Boolean.getBoolean((String) properties.get("weakEtag"));
            maxGetSize = Integer.parseInt((String) properties.get("maxGetSize"));
            maxHeaders = Integer.parseInt((String) properties.get("maxHeaders"));
            chunkLength = Integer.parseInt((String) properties.get("chunkLength"));
            maxEntitySize = Long.parseLong((String) properties.get("maxEntitySize"));
            maxGetBodySize = Integer.parseInt((String) properties.get("maxGetBodySize"));
            maxPutBodySize = Long.parseLong((String) properties.get("maxPutBodySize"));
            xForwardedForTag = Boolean.getBoolean((String) properties.get("xForwardedForTag"));

        }catch (Exception e){
            //todo - add more details
            log.error("cannot parse file "+file);
            System.exit(-1);
        }
    }

    public int getMaxGetBodySize() {
        return maxGetBodySize;
    }

    public long getMaxPutBodySize() {
        return maxPutBodySize;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public int getInitialThreads() {
        return initialThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getWaitQueue() {
        return waitQueue;
    }

    public boolean isWeakEtag() {
        return weakEtag;
    }

    public int getMaxGetSize() {
        return maxGetSize;
    }

    public int getMaxHeaders() {
        return maxHeaders;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public boolean isxForwardedForTag() {
        return xForwardedForTag;
    }

    public int getChunkLength() {
        return chunkLength;
    }

    public long getMaxEntitySize() {
        return maxEntitySize;
    }

    public String getRequestGetEncoding() {
        return requestGetEncoding;
    }

}

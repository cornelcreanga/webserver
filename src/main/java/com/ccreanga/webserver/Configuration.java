package com.ccreanga.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    private boolean verbose = true;

    private Properties properties = new Properties();

    public Configuration() {
    }

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

    private void load() {
            serverPort = parseInt("serverPort",1,65535);
            serverRootFolder = (String) properties.get("serverRootFolder");
            if (serverRootFolder==null || serverRootFolder.trim().isEmpty())
                throw new InternalException("serverRootFolder is missing or is empty");
            File root  = new File(serverRootFolder);
            if (!root.exists())
                throw new InternalException(root.getAbsolutePath()+" does not exists");
            if (root.exists() && !root.isDirectory())
                throw new InternalException(root.getAbsolutePath()+" is not a folder");
            if (root.exists() && root.isDirectory() && !root.canRead())
                throw new InternalException(root.getAbsolutePath()+" can't be read (permission rights?)");

            serverInitialThreads = parseInt("serverInitialThreads",8,1024);
            serverMaxThreads = parseInt("serverMaxThreads",8,1024);

            requestTimeoutSeconds = parseInt("requestTimeoutSeconds",1,3600);

            requestWaitingQueueSize = parseInt("requestWaitingQueueSize",1,1000);
            requestEtag = (String) properties.get("requestEtag");
            if (requestEtag==null)
                throw new InternalException("missing requestEtag value");
            if ((!requestEtag.equals(ETAG_NONE)) && (!requestEtag.equals(ETAG_WEAK)))
                throw new IllegalArgumentException("unknown etag:"+requestEtag+"; it should be none or weak");
            requestMaxLines = Integer.parseInt((String) properties.get("requestMaxLines"));

            requestMaxLineLength = parseInt("requestMaxLineLength",256,65535);
            requestMaxHeaders = parseInt("requestMaxHeaders",8,65535);

            if (properties.get("verbose")==null)
                throw new InternalException("missing verbose value");

            verbose = Boolean.valueOf((String) properties.get("verbose"));


    }

    private int parseInt(String name, int min, int max){
        String string="";
        try{
            string = (String) properties.get(name);
            if (string==null)
                throw new InternalException("Cannot find the value "+name);

            int value = Integer.parseInt(string);
            if ((value<min) || (value>max))
                throw new InternalException("Cannot configure "+name+ " - expecting a number between "+min+" and "+max+" instead of "+value);
            return value;
        }catch (NumberFormatException e){
            throw new InternalException("Cannot configure "+name+ " - expecting an integer  instead of "+string);
        }
    }

    public boolean isVerbose() {
        return verbose;
    }
}

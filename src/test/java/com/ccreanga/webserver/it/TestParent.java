package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.Server;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Properties;

public abstract class TestParent {

    protected static Server server;
    protected String host = "127.0.0.1";
    protected static Configuration configuration ;
    protected static CloseableHttpClient httpclient;
    protected static CloseableHttpClient httpclientNoDecompression;

    protected static String port = "8999";

    @BeforeClass
    public static void init() {

        Properties properties = new Properties();
        properties.put("serverPort", port);
        properties.put("serverRootFolder", ClassLoader.getSystemResource("www").getPath());
        properties.put("serverInitialThreads", "128");
        properties.put("serverMaxThreads", "1000");

        properties.put("requestTimeoutSeconds", "3");
        properties.put("requestWaitingQueueSize", "64");

        properties.put("requestEtag", "weak");

        properties.put("requestMaxLines", "200");
        properties.put("requestMaxLineLength", "1024");
        properties.put("requestMaxHeaders", "64");
        properties.put("requestMaxGetBodySize", "64000");
        properties.put("requestMaxPutBodySize", "2147483648");

        properties.put("verbose", "false");
        configuration = new Configuration(properties);

        server = new Server(configuration);
        httpclient = HttpClients.createDefault();
        httpclientNoDecompression = HttpClients.custom().disableContentCompression().build();

        try {
            new Thread(server).start();
            while(!server.isReady());
        } catch (InternalException e) {
            server.stop();
            throw e;
        }
    }

    @AfterClass
    public static void clean() throws IOException {
        httpclient.close();
        httpclientNoDecompression.close();
        server.stop();
        while(!server.isStopped());
//        while (!server.isStopped());
    }

}

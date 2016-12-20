package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.Server;
import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.common.SimpleFormatter;
import com.ccreanga.webserver.filehandler.FileMessageHandler;
import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import static com.ccreanga.webserver.Server.accessLog;
import static com.ccreanga.webserver.Server.serverLog;
import static org.junit.Assert.assertEquals;

public abstract class TestParent {

    protected static Server server;
    protected String host = "127.0.0.1";
    protected static Configuration configuration;
    protected static CloseableHttpClient httpclient;
    protected static CloseableHttpClient httpclientNoDecompression;

    protected static String port = "8999";

    @BeforeClass
    public static void init() {

        Properties properties = new Properties();
        properties.put("serverPort", port);
        properties.put("serverRootFolder", ClassLoader.getSystemResource("www").getPath());
        properties.put("rootFolderWritable", "true");

        properties.put("serverInitialThreads", "128");
        properties.put("serverMaxThreads", "1000");

        properties.put("requestTimeoutSeconds", "3");
        properties.put("requestWaitingQueueSize", "64");

        properties.put("requestEtag", "weak");

        properties.put("requestURIMaxSize", "8000");
        properties.put("requestMessageBodyMaxSize", "" + 1024 * 1024 * 8);
        properties.put("requestMaxLineLength", "10000");
        properties.put("requestMaxHeaders", "64");

        properties.put("verbose", "true");
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter("%5$s%6$s%n"));
        consoleHandler.setLevel(Level.INFO);
        serverLog.setUseParentHandlers(false);
        accessLog.setUseParentHandlers(false);
        serverLog.addHandler(consoleHandler);
        serverLog.setLevel(Level.INFO);
        configuration = new Configuration(properties);

        server = new Server(new FileMessageHandler(),configuration);
        httpclient = HttpClients.createDefault();
        httpclientNoDecompression = HttpClients.custom().disableContentCompression().build();

        try {
            new Thread(server).start();
            while (!server.isReady()) ;
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
        while (!server.isStopped()) ;
    }

    protected void checkForStatus(HttpUriRequest request, HttpStatus status, String content) throws Exception {
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String entityContent = Util.readAsUtfString(entity.getContent());

            assertEquals(statusLine.getStatusCode(), status.value());
            assertEquals(entityContent, content);

        }
    }

    protected void checkForStatus(HttpUriRequest request, HttpStatus status) throws Exception {
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), status.value());
        }
    }

}

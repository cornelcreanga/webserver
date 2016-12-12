package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.Server;
import com.ccreanga.webserver.common.SimpleFormatter;
import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import static com.ccreanga.webserver.Server.accessLog;
import static com.ccreanga.webserver.Server.serverLog;
import static org.junit.Assert.assertTrue;

public class TestUnavailableServer {

    protected String host = "127.0.0.1";
    protected static String port = "8999";


    @Test
    public void serverUnavailableProperResponse() throws Exception {

        Properties properties = new Properties();
        properties.put("serverPort", port);
        properties.put("serverRootFolder", ClassLoader.getSystemResource("www").getPath());
        properties.put("rootFolderWritable", "true");

        properties.put("serverInitialThreads", "1");
        properties.put("serverMaxThreads", "1");

        properties.put("requestTimeoutSeconds", "20");
        properties.put("requestWaitingQueueSize", "1");

        properties.put("requestEtag", "weak");

        properties.put("requestURIMaxSize", "8000");
        properties.put("requestMessageBodyMaxSize", "15000");
        properties.put("requestMaxLineLength", "10000");
        properties.put("requestMaxHeaders", "64");

        properties.put("verbose", "false");

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter("%5$s%6$s%n"));
        consoleHandler.setLevel(Level.INFO);
        serverLog.setUseParentHandlers(false);
        accessLog.setUseParentHandlers(false);
        serverLog.addHandler(consoleHandler);
        serverLog.setLevel(Level.INFO);

        Server server = new Server(new Configuration(properties));
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(100)
                .build();

        httpclient.start();

        try {
            new Thread(server).start();
            while (!server.isReady()) ;
        } catch (InternalException e) {
            server.stop();
            throw e;
        }

        try {


            final AtomicInteger counter = new AtomicInteger(0);
            List<HttpGet> requests = new ArrayList<>();
            final CountDownLatch latch = new CountDownLatch(10);
            for (int i = 0; i < 10; i++) {
                HttpGet request = new HttpGet("http://" + host + ":" + port + "/folder1/bigFile.txt");
                request.setProtocolVersion(HttpVersion.HTTP_1_1);
                request.addHeader("Connection", "Close");
                request.addHeader("Accept-Encoding", "gzip,deflate");
                requests.add(request);
            }
            for (int i = 0; i < 10; i++) {
                httpclient.execute(requests.get(i), new FutureCallback<HttpResponse>() {
                    public void completed(final HttpResponse response) {
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE.value())
                            counter.incrementAndGet();
                        latch.countDown();
                    }

                    public void failed(final Exception ex) {
                        latch.countDown();
                    }

                    public void cancelled() {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            assertTrue(counter.get() > 1);

        } finally {
            httpclient.close();
            server.stop();
            while (!server.isStopped()) ;
        }
    }
}

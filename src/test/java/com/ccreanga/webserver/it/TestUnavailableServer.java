package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.Server;
import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.http.HTTPStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestUnavailableServer {

    protected String host = "127.0.0.1";
    protected static String port = "8999";


    @Test
    public void serverUnavailableProperResponse() throws Exception {


        Properties properties = new Properties();
        properties.put("serverPort",port);
        properties.put("serverRootFolder", ClassLoader.getSystemResource("www").getPath());
        properties.put("serverInitialThreads", "1");
        properties.put("serverMaxThreads", "1");

        properties.put("requestTimeoutSeconds", "20");
        properties.put("requestWaitingQueueSize", "1");

        properties.put("requestEtag", "weak");

        properties.put("requestMaxLines", "200");
        properties.put("requestMaxLineLength", "1024");
        properties.put("requestMaxHeaders", "64");
        properties.put("requestMaxGetBodySize", "64000");
        properties.put("requestMaxPutBodySize", "2147483648");

        properties.put("verbose", "false");

        Server server = new Server(new Configuration(properties));
        CloseableHttpAsyncClient httpclient =HttpAsyncClients.custom()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(100)
                .build();

        httpclient.start();

        try {
            new Thread(server).start();
            while(!server.isReady());
        } catch (InternalException e) {
            server.stop();
            throw e;
        }

        final AtomicInteger counter = new AtomicInteger(0);
        List<HttpGet> requests = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(10);
        for(int i=0;i<10;i++){
            HttpGet request = new HttpGet("http://" + host + ":" + port + "/folder1/bigFile.txt");
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            request.addHeader("Connection", "Close");
            request.addHeader("Accept-Encoding", "gzip,deflate");
            requests.add(request);
        }
        for(int i=0;i<10;i++){
            httpclient.execute(requests.get(i), new FutureCallback<HttpResponse>(){
                public void completed(final HttpResponse response) {
                    if (response.getStatusLine().getStatusCode()==HTTPStatus.SERVICE_UNAVAILABLE.value())
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
        assertTrue(counter.get()>1);
    }
}

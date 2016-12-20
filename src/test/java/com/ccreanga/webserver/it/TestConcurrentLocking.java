package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT_ENCODING;
import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.assertTrue;

public class TestConcurrentLocking extends TestParent {

    @Test
    public void serverUnavailableProperResponse() throws Exception {

        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(100)
                .build();

        httpclient.start();

        try {


            final AtomicInteger counter = new AtomicInteger(0);
            List<HttpUriRequest> requests = new ArrayList<>();
            final CountDownLatch latch = new CountDownLatch(10);
            for (int i = 0; i < 5; i++) {


                String uri = "/testconcurrent/file.txt";
                HttpPut putRequest = new HttpPut("http://" + host + ":" + port + uri);
                putRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
                putRequest.setHeader(CONTENT_TYPE, "text/plain");
                StringBuilder text = new StringBuilder();
                for (int j = 0; j < 10000; j++)
                    text.append("test").append(j);

                InputStreamEntity reqEntity = new InputStreamEntity(
                        new ByteArrayInputStream(text.toString().getBytes()), -1, ContentType.APPLICATION_OCTET_STREAM);

                reqEntity.setChunked(true);
                putRequest.setEntity(reqEntity);
                requests.add(putRequest);


                HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
                patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
                patchRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");
                patchRequest.addHeader("X-UPDATE", "APPEND");

                final String textToAppend = "FGH";
                patchRequest.setEntity(new ByteArrayEntity(textToAppend.getBytes()));
                requests.add(patchRequest);


            }
            for (int i = 0; i < 10; i++) {
                httpclient.execute(requests.get(i), new FutureCallback<HttpResponse>() {
                    public void completed(final HttpResponse response) {
                        if (response.getStatusLine().getStatusCode() == HttpStatus.LOCKED.value())
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
        }
    }

}

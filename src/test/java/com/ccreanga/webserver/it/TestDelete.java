package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static com.ccreanga.webserver.http.HttpHeaders.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestDelete extends TestParent {

    @Test
    public void testFileDelete() throws Exception {

        String uri = "/சுப்ரமணிய/cucu.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "test text";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        testDeleted(uri, request);

    }

    private void testDeleted(String uri, HttpPut request) throws IOException {
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpDelete deleteRequest = new HttpDelete("http://" + host + ":" + port + uri);
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            try (CloseableHttpResponse delResponse = httpclient.execute(deleteRequest)) {
                assertEquals(delResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                assertEquals(delResponse.getFirstHeader(CONTENT_LENGTH).getValue(), "0");
            }

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + uri);
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.NOT_FOUND.value());
            }


        }
    }

    @Test
    public void testFolderDelete() throws Exception {
        String uri = "/" + UUID.randomUUID().toString();
        String uriChild = uri + "/" + UUID.randomUUID().toString();
        HttpPut request = new HttpPut("http://" + host + ":" + port + uriChild + "/file.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "test text";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        testDeleted(uri, request);


    }

    //todo - add partial delete

}

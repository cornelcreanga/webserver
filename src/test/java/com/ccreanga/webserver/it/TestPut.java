package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestPut extends TestParent {


    @Test
    public void testPutChunkedBody() throws Exception {
        String uri = "/testputchunked/file_put.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 10000; i++)
            text.append("test").append(i);

        InputStreamEntity reqEntity = new InputStreamEntity(
                new ByteArrayInputStream(text.toString().getBytes()), -1, ContentType.APPLICATION_OCTET_STREAM);

        reqEntity.setChunked(true);
        request.setEntity(reqEntity);

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + uri);
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text.toString());
            }

        }


    }


    @Test
    public void testPutSimpleBody() throws Exception {
        String uri = "/testput/test_put.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "test text";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + uri);
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text);
            }


        }


    }

}

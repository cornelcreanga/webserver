package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Base64;

import static com.ccreanga.webserver.http.HttpHeaders.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestPost extends TestParent {


    @Test
    public void testPostChunkedBody() throws Exception {
        HttpPost request = new HttpPost("http://" + host + ":" + port + "/cucu/");
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
            String entityContent = Util.readAsUtfString(entity.getContent());

            assertEquals(statusLine.getStatusCode(), HttpStatus.CREATED.value());
            assertEquals(entityContent, "");
            assertNotNull(response.getFirstHeader(LOCATION));
            String location = response.getFirstHeader(LOCATION).getValue();
            assertNotNull(location);
            location = new String(Base64.getDecoder().decode(location));
            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + location);
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text.toString());
            }

        }


    }


    @Test
    public void testPostSimpleBody() throws Exception {

        HttpPost request = new HttpPost("http://" + host + ":" + port + "/சுப்ரமணிய/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "test text";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String entityContent = Util.readAsUtfString(entity.getContent());

            assertEquals(statusLine.getStatusCode(), HttpStatus.CREATED.value());
            assertEquals(entityContent, "");
            assertNotNull(response.getFirstHeader(LOCATION));
            String location = response.getFirstHeader(LOCATION).getValue();
            assertNotNull(location);
            location = new String(Base64.getDecoder().decode(location));

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + location);
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text);
            }


        }


    }

}

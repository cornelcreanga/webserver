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

import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT_ENCODING;
import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;
import static com.ccreanga.webserver.http.HttpHeaders.LOCATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestPostInvalidRequests extends TestParent {

    @Test
    public void testPostBodyTooLarge() throws Exception {
        HttpPost request = new HttpPost("http://" + host + ":" + port + "/cucu/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 1024*1024*32+1000; i++)
            text.append('c');

        InputStreamEntity reqEntity = new InputStreamEntity(
                new ByteArrayInputStream(text.toString().getBytes()), -1, ContentType.APPLICATION_OCTET_STREAM);

        reqEntity.setChunked(true);
        request.setEntity(reqEntity);

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String entityContent = Util.readAsUtfString(entity.getContent());
            assertEquals(statusLine.getStatusCode(), HttpStatus.PAYLOAD_TOO_LARGE.value());
            assertEquals(entityContent, "");
        }


    }


    @Test
    public void testUnknownContentType() throws Exception {
        HttpPost request = new HttpPost("http://" + host + ":" + port + "/cucu/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "unknown");
        final String text = "test text";
        request.setEntity(new ByteArrayEntity(text.getBytes()));
        checkForStatus(request, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testForbiddenURI() throws Exception {
        HttpPost request = new HttpPost("http://" + host + ":" + port + "/../");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "test text";
        request.setEntity(new ByteArrayEntity(text.getBytes()));
        checkForStatus(request, HttpStatus.FORBIDDEN);
    }


    @Test
    public void testFormUrlEncodedContentType() throws Exception {
        HttpPost request = new HttpPost("http://" + host + ":" + port + "/cucu/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
        final String text = "test text";
        request.setEntity(new ByteArrayEntity(text.getBytes()));
        checkForStatus(request, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testMultiPartContentType() throws Exception {
        HttpPost request = new HttpPost("http://" + host + ":" + port + "/cucu/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "multipart/form-data");
        final String text = "test text";
        request.setEntity(new ByteArrayEntity(text.getBytes()));
        checkForStatus(request, HttpStatus.BAD_REQUEST);
    }


}
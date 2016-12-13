package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Test;

import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;

public class TestPostInvalidRequests extends TestParent {

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
        request.setHeader(CONTENT_TYPE, "unknown");
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
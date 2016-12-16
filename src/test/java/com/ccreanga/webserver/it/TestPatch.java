package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Test;

import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT_ENCODING;
import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestPatch extends TestParent {

    @Test
    public void testPatchAppend() throws Exception {

        String uri = "/testpatch/file.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "ABCDE";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            patchRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            patchRequest.addHeader("X-UPDATE","APPEND");

            final String textToAppend = "FGH";
            patchRequest.setEntity(new ByteArrayEntity(textToAppend.getBytes()));

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.NO_CONTENT.value());
            }

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + uri);
            getRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            getRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");

            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text+textToAppend);
            }

        }


    }


    @Test
    public void testPatchInsert() throws Exception {

        String uri = "/testpatch/file.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "ABCDE";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            patchRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            int index = 1;
            patchRequest.addHeader("X-UPDATE","INSERT "+index);

            final String textToAppend = "FGH";
            patchRequest.setEntity(new ByteArrayEntity(textToAppend.getBytes()));

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.NO_CONTENT.value());
            }

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + uri);
            getRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            getRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");

            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text.substring(0,index)+textToAppend+text.substring(index));
            }

        }


    }

    @Test
    public void testPatchRemoveStartIndex() throws Exception {
        String uri = "/testpatch/file.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "ABCDE";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            patchRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            int index = 1;
            patchRequest.addHeader("X-UPDATE", "REMOVE " + index);

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.NO_CONTENT.value());
            }

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + uri);
            getRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            getRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");

            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text.substring(0, index));
            }

        }
    }

    @Test
    public void testPatchRemoveStartEndIndex() throws Exception {
        String uri = "/testpatch/file.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "ABCDE";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            patchRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            int start = 1,len=3;
            patchRequest.addHeader("X-UPDATE", "REMOVE " + start+" "+len);

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.NO_CONTENT.value());
            }

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + uri);
            getRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            getRequest.addHeader(ACCEPT_ENCODING, "gzip,deflate");

            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, text.substring(0, start)+text.substring(start+len));
            }

        }
    }

}
package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Test;

import java.util.UUID;

import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestPatchInvalidRequests extends TestParent {

    @Test
    public void testPatchBadCommand() throws Exception {
        String uri = "/testpatch/file.txt";
        HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
        patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
        patchRequest.addHeader("X-UPDATE", "NONE");

        try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
            assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
        }

    }

    @Test
    public void testPatchNotFound() throws Exception {
        String uri = "/testpatch/" + UUID.randomUUID().toString();
        HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
        patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
        patchRequest.addHeader("X-UPDATE", "INSERT");

        try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
            assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.NOT_FOUND.value());
        }

    }

    @Test
    public void testPatchInsertWrongIndex() throws Exception {

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
            int index = 11;
            patchRequest.addHeader("X-UPDATE", "INSERT " + index);

            String textToAppend = "FGH";
            patchRequest.setEntity(new ByteArrayEntity(textToAppend.getBytes()));

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
            }

            patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            patchRequest.addHeader("X-UPDATE", "INSERT " + "cucu");

            textToAppend = "FGH";
            patchRequest.setEntity(new ByteArrayEntity(textToAppend.getBytes()));
            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
            }

            patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            patchRequest.addHeader("X-UPDATE", "INSERT " + "-1");

            textToAppend = "FGH";
            patchRequest.setEntity(new ByteArrayEntity(textToAppend.getBytes()));
            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
            }

        }


    }

    @Test
    public void testPatchRemoveWrongIndexes() throws Exception {
        String uri = "/testpatch/file.txt";
        HttpPut request = new HttpPut("http://" + host + ":" + port + uri);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        String text = "ABCDE";

        request.setEntity(new ByteArrayEntity(text.getBytes()));

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            assertEquals(statusLine.getStatusCode(), HttpStatus.NO_CONTENT.value());
            assertNull(entity);

            HttpPatch patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            int start = 1, len = 33;
            patchRequest.addHeader("X-UPDATE", "REMOVE " + start + " " + len);

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
            }

            patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            start = 1;
            patchRequest.addHeader("X-UPDATE", "REMOVE " + start + " " + " abc");

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
            }

            patchRequest = new HttpPatch("http://" + host + ":" + port + uri);
            patchRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
            patchRequest.addHeader("X-UPDATE", "REMOVE");

            try (CloseableHttpResponse patchResponse = httpclient.execute(patchRequest)) {
                assertEquals(patchResponse.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());
            }
        }
    }


}

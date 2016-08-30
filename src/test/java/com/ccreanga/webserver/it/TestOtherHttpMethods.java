package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.net.HttpHeaders.ALLOW;
import static org.junit.Assert.assertEquals;

public class TestOtherHttpMethods extends TestParent {


//    @Test
//    public void testPost() throws IOException {
//        testNotImplemented(new HttpPost("http://" + host + ":" + port + "/"));
//    }

//    @Test
//    public void testPut() throws IOException {
//        testNotImplemented(new HttpPut("http://" + host + ":" + port + "/"));
//    }
//
//    @Test
//    public void testPatch() throws IOException {
//        testNotImplemented(new HttpPatch("http://" + host + ":" + port + "/"));
//    }
//
//    @Test
//    public void testDelete() throws IOException {
//        testNotImplemented(new HttpDelete("http://" + host + ":" + port + "/"));
//    }
//
//    @Test
//    public void testOptions() throws IOException {
//        HttpOptions request = new HttpOptions("http://" + host + ":" + port + "/");
//        request.setProtocolVersion(HttpVersion.HTTP_1_1);
//        try (CloseableHttpResponse response = httpclient.execute(request)) {
//            StatusLine statusLine = response.getStatusLine();
//            assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
//            assertEquals(response.getFirstHeader(ALLOW).getValue(), "GET, HEAD, OPTIONS");
//        }
//    }


    private void testNotImplemented(HttpRequestBase request) throws IOException {
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.NOT_IMPLEMENTED.value());
        }

    }

}

package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpOptions;
import org.junit.Test;

import java.io.IOException;

import static com.ccreanga.webserver.http.HttpHeaders.ALLOW;
import static org.junit.Assert.assertEquals;

public class TestOtherHttpMethods extends TestParent {

    @Test
    public void testOptions() throws IOException {
        HttpOptions request = new HttpOptions("http://" + host + ":" + port + "/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
            assertEquals(response.getFirstHeader(ALLOW).getValue(), "GET, HEAD, OPTIONS,PUT,POST,DELETE,PATCH");
        }
    }
}

package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.net.HttpHeaders.*;
import static org.junit.Assert.assertEquals;

public class TestGetHttp1_0 extends TestParent {

    @Test
    public void testResourceFoundInRoot() throws IOException {
        String fileName = "www/file.txt";
        String extension = Util.extension(fileName);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/file.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_0);
        request.addHeader("Connection", "Keep-Alive");
        request.addHeader("Accept-Encoding", "gzip,deflate");

        try (CloseableHttpResponse response = httpclient.execute(request)) {

            String fileContent = Util.readAsUtfString(fileName);

            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
            assertEquals(response.getFirstHeader(CONNECTION).getValue(), "Keep-Alive");
            assertEquals(response.getFirstHeader(CONTENT_LENGTH).getValue(), "" + fileContent.length());
            assertEquals(response.getFirstHeader(CONTENT_TYPE).getValue(), Mime.getType(extension));
            assertEquals(response.getFirstHeader(CONTENT_ENCODING), null);//will not use gzip for http1.0
            assertEquals(response.getFirstHeader(ETAG), null);//will not return ETAG for http1.0


            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());
            assertEquals(content, Util.readAsUtfString("www/file.txt"));
        }

    }
}

package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.common.StringUtil;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.ioutil.IOUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;

import static com.ccreanga.webserver.http.HttpHeaders.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestPost extends TestParent {

    @Test
    public void testPostSimpleBody() throws Exception {


        HttpPost request = new HttpPost("http://" + host + ":" + port + "/cucu/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE,"text/plain");
        request.setEntity(new ByteArrayEntity("cucubau".getBytes()));


        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String entityContent = Util.readAsUtfString(entity.getContent());

            assertEquals(statusLine.getStatusCode(), HttpStatus.CREATED.value());
            assertEquals(entityContent, "");
            assertNotNull(response.getFirstHeader(LOCATION));
            String location = response.getFirstHeader(LOCATION).getValue();
            assertNotNull(location);

            HttpGet getRequest = new HttpGet("http://" + host + ":" + port + location);
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
            try (CloseableHttpResponse getResponse = httpclient.execute(getRequest)) {
                assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.OK.value());
                String content = Util.readAsUtfString(getResponse.getEntity().getContent());
                assertEquals(content, "cucubau");
            }


        }


    }

}

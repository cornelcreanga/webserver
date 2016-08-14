package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.http.HttpStatus;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.io.File;

import static com.google.common.net.HttpHeaders.*;
import static org.junit.Assert.assertEquals;

public class TestGetRange extends TestParent{

    @Test
    public void testRangeOK() throws Exception{
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file,"gz", true);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        //request.addHeader(IF_RANGE,etag);
        request.addHeader(RANGE,"1-5");
        request.addHeader(ACCEPT_ENCODING,"gzip,deflate");

        checkForStatus(request,HttpStatus.OK,"2345");
    }

    private void checkForStatus(HttpGet request,HttpStatus status,String content) throws Exception{
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String entityContent = Util.readAsUtfString(entity.getContent());

            assertEquals(statusLine.getStatusCode(), status.value());
            assertEquals(entityContent, content);

        }
    }


}

package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;

import static com.google.common.net.HttpHeaders.*;
import static org.junit.Assert.assertEquals;

public class TestHead extends TestParent {

    @Test
    public void testHeadResource() throws Exception {
        String fileName = "www/file.txt";
        File file = new File(ClassLoader.getSystemResource(fileName).toURI());
        String extension = Util.extension(fileName);
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();


        HttpHead request = new HttpHead("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        try (CloseableHttpResponse response = httpclient.execute(request)) {

            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
            assertEquals(response.getFirstHeader(CONNECTION).getValue(), "Keep-Alive");
            assertEquals(response.getFirstHeader(CONTENT_LENGTH), null);
            assertEquals(response.getFirstHeader(CONTENT_TYPE).getValue(), Mime.getType(extension));
            //this header is removed in case of content decompression by the http client
//        assertEquals(response.getFirstHeader(CONTENT_ENCODING).getValue(),"gzip");
            assertEquals(response.getFirstHeader(ETAG).getValue(), EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true));

            LocalDateTime date = DateUtil.parseRfc2161CompliantDate(response.getFirstHeader(LAST_MODIFIED).getValue());

            LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);
            assertEquals(date, modifiedDate);

            HttpEntity entity = response.getEntity();
            assertEquals(entity, null);

        }

    }


}

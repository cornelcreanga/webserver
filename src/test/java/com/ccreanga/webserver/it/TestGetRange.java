package com.ccreanga.webserver.it;

import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
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
import java.time.Instant;

import static com.google.common.net.HttpHeaders.*;
import static org.junit.Assert.assertEquals;

public class TestGetRange extends TestParent {

    @Test
    public void testRangePartialContent() throws Exception {
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(RANGE, "1-5");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.PARTIAL_CONTENT, "2345");
    }

    @Test
    public void testRangeInvalid1() throws Exception {
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(RANGE, "-1-5");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "");
    }

    @Test
    public void testMultiRangeIsNotAccepted() throws Exception {
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(RANGE, "1-5,8-11");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "");
    }

    @Test
    public void testIfRangeEtagPartialContent() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_RANGE, etag);
        request.addHeader(RANGE, "1-5");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
        checkForStatus(request, HttpStatus.PARTIAL_CONTENT, "2345");
    }

    @Test
    public void testIfRangeModifiedPartialContent() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);

        request.addHeader(IF_RANGE, DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_RFC822));
        request.addHeader(RANGE, "1-5");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
        checkForStatus(request, HttpStatus.PARTIAL_CONTENT, "2345");
    }

    @Test
    public void testIfRangeModifiedOk() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);

        request.addHeader(IF_RANGE, DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()).minusSeconds(100), DateUtil.FORMATTER_RFC822));
        request.addHeader(RANGE, "1-5");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
        checkForStatus(request, HttpStatus.OK, "123456789");
    }

    @Test
    public void testIfRangeInvalid() throws Exception {
        //todo - check if all thre response headers are ok! it might not be the case, this test takes too much seconds
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_RANGE, etag);
        request.addHeader(RANGE, "d1-5");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");
        checkForStatus(request, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "");
    }

    private void checkForStatus(HttpGet request, HttpStatus status, String content) throws Exception {
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String entityContent = Util.readAsUtfString(entity.getContent());

            assertEquals(statusLine.getStatusCode(), status.value());
            assertEquals(entityContent, content);

        }
    }


}

package com.ccreanga.webserver.it;

import com.ccreanga.webserver.common.StringUtil;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.ioutil.IOUtil;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.ccreanga.webserver.http.HttpHeaders.*;
import static org.junit.Assert.assertEquals;

public class TestGetConditionals extends TestParent {


    private void checkForStatus(HttpGet request, HttpStatus status) throws Exception {
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), status.value());
        }
    }

    @Test
    public void testConditionalsWithInvalidDate() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH, etag);
        request.addHeader(IF_MODIFIED_SINCE, "not parsable");
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testIfMatchNotModified() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true);
        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH, etag);
        request.addHeader(IF_MODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.NOT_MODIFIED);


    }

    @Test
    public void testIfMatchPrecondFailed() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true) + "_not_match";
        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH, etag);
        request.addHeader(IF_MODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.PRECONDITION_FAILED);

    }

    @Test
    public void testIfMatchOk() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true);
        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH, etag);
        request.addHeader(IF_MODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.minusDays(1).toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.OK);

    }

    @Test
    public void testIfUnmodifiedSincePrecondFailed() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_UNMODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.minusDays(1).toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.PRECONDITION_FAILED);

    }

    @Test
    public void testIfUnmodifiedSinceOk() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true) + "_not_match";
        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH, etag);
        request.addHeader(IF_UNMODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.OK);
    }

    @Test
    public void testIfUnmodifiedNotModified() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true);
        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH, etag);
        request.addHeader(IF_UNMODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.NOT_MODIFIED);
    }

    @Test
    public void testIfNoneMatchOk() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true) + "_not_match";

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH, etag);
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.OK);

    }

    @Test
    public void testIfNoneMatchNotModified() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        String etag = EtagManager.getInstance().getFileEtag(file, EtagManager.GZIP_EXT, true);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH, etag);
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.NOT_MODIFIED);

    }

    @Test
    public void ifModifiedSinceOk() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());


        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.minusDays(1).toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.OK);

    }

    @Test
    public void ifModifiedSinceNotModified() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());

        LocalDateTime modifiedDate = IOUtil.modifiedDateAsUTC(file);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + StringUtil.escapeURLComponent("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MODIFIED_SINCE, DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC), DateUtil.FORMATTER_RFC822));
        request.addHeader(ACCEPT_ENCODING, "gzip,deflate");

        checkForStatus(request, HttpStatus.NOT_MODIFIED);

    }

}

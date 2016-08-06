package com.ccreanga.webserver.it;

import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import static com.google.common.net.HttpHeaders.*;
import java.io.File;
import java.time.*;

import static org.junit.Assert.assertEquals;

public class TestGetConditionals extends TestParent {


    private void checkForStatus(HttpGet request,HTTPStatus status) throws Exception{
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), status.value());
        }
    }

    @Test
    public void testConditionalsWithInvalidDate() throws Exception{
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH,etag);
        request.addHeader(IF_MODIFIED_SINCE,"not parsable");

        checkForStatus(request,HTTPStatus.BAD_REQUEST);
    }

    @Test
    public void testIfMatchNotModified() throws Exception{
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH,etag);
        request.addHeader(IF_MODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.NOT_MODIFIED);


    }

    @Test
    public void testIfMatchPrecondFailed() throws Exception{
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true)+"_not_match";
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH,etag);
        request.addHeader(IF_MODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.PRECONDITION_FAILED);

    }

    @Test
    public void testIfMatchDateExpired() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MATCH,etag);
        request.addHeader(IF_MODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.minusDays(1).toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.OK);

    }

    @Test
    public void testIfUnmodifiedSincePrecondFailed() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        //request.addHeader(IF_MATCH,etag);
        request.addHeader(IF_UNMODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.minusDays(1).toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.PRECONDITION_FAILED);

    }

    @Test
    public void testIfUnmodifiedSincePrecondNotModified() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true)+"_not_match";
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH,etag);
        request.addHeader(IF_UNMODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.NOT_MODIFIED);
    }

    @Test
    public void testIfUnmodifiedSincePrecondExpired() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH,etag);
        request.addHeader(IF_UNMODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.OK);
    }

    @Test
    public void testIfNoneMatch_NotMatch() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true)+"_not_match";
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH,etag);

        checkForStatus(request,HTTPStatus.NOT_MODIFIED);

    }

    @Test
    public void testIfNoneMatch_Failed() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_NONE_MATCH,etag);

        checkForStatus(request,HTTPStatus.OK);

    }

    @Test
    public void ifModifiedSinceNotModified() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.minusDays(1).toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.OK);

    }

    @Test
    public void ifModifiedSinceFailed() throws Exception {
        File file = new File(ClassLoader.getSystemResource("www/file.txt").toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        String etag = EtagManager.getInstance().getFileEtag(file, true);
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("file.txt"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader(IF_MODIFIED_SINCE,DateUtil.formatDateToUTC(modifiedDate.toInstant(ZoneOffset.UTC),DateUtil.FORMATTER_RFC822));

        checkForStatus(request,HTTPStatus.NOT_MODIFIED);

    }

}

package com.ccreanga.webserver.it;


import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.http.representation.FileResourceRepresentation;
import com.ccreanga.webserver.http.representation.RepresentationManager;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static com.google.common.net.HttpHeaders.*;
import static org.junit.Assert.assertEquals;

public class TestGetHttp1_1 extends TestParent {


    @Test
    public void testResourceNotFound() throws Exception {

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/notExisting.html");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        try (CloseableHttpResponse response = httpclient.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.NOT_FOUND.value());
            assertEquals(statusLine.getReasonPhrase(), HttpStatus.NOT_FOUND.getReasonPhrase());
            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());

            assertEquals(content, getRepresentation(response).errorRepresentation(HttpStatus.NOT_FOUND, ""));
        }
    }

    @Test
    public void testResourceFoundInRoot() throws Exception {
        testResourceOk("file.txt");
    }

    @Test
    @Ignore
    //the file name is not supported on windows so the file a?b.txt was removed
    public void testResourceWithSpecialChars() throws Exception {
        testResourceOk("folder1/a?b.txt");
    }

    @Test
    public void testFolderHtmlContentType() throws Exception {
        testFolder("html");
    }

    @Test
    public void testFolderJsonContentType() throws Exception {
        testFolder("json");
    }


    public void testFolder(String mime) throws Exception {
        String fileName = "www/folder1";
        File file = new File(ClassLoader.getSystemResource(fileName).toURI());
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();


        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape("folder1"));
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader("Accept", Mime.getType(mime));

        try (CloseableHttpResponse response = httpclient.execute(request)) {

            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
            assertEquals(response.getFirstHeader(CONNECTION).getValue(), "Keep-Alive");
            assertEquals(response.getFirstHeader(CONTENT_LENGTH), null);
            assertEquals(response.getFirstHeader(CONTENT_TYPE).getValue(), Mime.getType(mime));
            assertEquals(response.getFirstHeader(ETAG), null);

            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());

            assertEquals(content, getRepresentation(response).folderRepresentation(file, new File(configuration.getServerRootFolder())));
        }

    }

    private FileResourceRepresentation getRepresentation(CloseableHttpResponse response) {
        return RepresentationManager.getInstance().getRepresentation(response.getFirstHeader(CONTENT_TYPE).getValue());
    }

    @Test
    public void testForbiddenResource() throws IOException {

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/../outsideWWWParent.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        try (CloseableHttpResponse response = httpclient.execute(request)) {

            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.FORBIDDEN.value());
            assertEquals(statusLine.getReasonPhrase(), HttpStatus.FORBIDDEN.getReasonPhrase());
            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());
            assertEquals(content, getRepresentation(response).errorRepresentation(HttpStatus.FORBIDDEN, ""));
        }
    }

    @Test
    public void testGzipContentEncoding() throws Exception {

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/folder1/bigFile.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader("Connection", "Keep-Alive");
        request.addHeader("Accept-Encoding", "gzip,deflate");

        try (CloseableHttpResponse response = httpclientNoDecompression.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
            assertEquals(response.getFirstHeader(CONTENT_ENCODING).getValue(), "gzip");
        }
        request = new HttpGet("http://" + host + ":" + port + "/folder1/jsse.dat");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader("Connection", "Keep-Alive");
        request.addHeader("Accept-Encoding", "gzip,deflate");

        try (CloseableHttpResponse response = httpclientNoDecompression.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
            assertEquals(response.getFirstHeader(CONTENT_ENCODING), null);
        }
    }

    @Test
    public void testResourceWithUtfChars() throws Exception {
        testResourceOk("சுப்ரமணிய/பாரதியார்.html");
    }

    private void testResourceOk(String resource) throws Exception {
        String fileName = "www/" + resource;
        File file = new File(ClassLoader.getSystemResource(fileName).toURI());
        String extension = Util.extension(fileName);
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();


        HttpGet request = new HttpGet("http://" + host + ":" + port + "/" + urlPathEscaper.escape(resource));
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
            String content = Util.readAsUtfString(entity.getContent());
            assertEquals(content, Util.readAsUtfString(fileName));
        }

    }

}

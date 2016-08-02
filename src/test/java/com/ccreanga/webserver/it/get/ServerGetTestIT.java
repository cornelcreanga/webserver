package com.ccreanga.webserver.it.get;


import com.ccreanga.webserver.*;
import com.ccreanga.webserver.etag.EtagManager;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.Mime;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;

import static com.ccreanga.webserver.http.HTTPHeaders.*;
import static org.junit.Assert.assertEquals;

public class ServerGetTestIT extends HttpTest{


    @Test
    public void testResourceNotFound() throws IOException {


        HttpGet request = new HttpGet("http://" + host + ":" + port + "/notExisting.html");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        CloseableHttpResponse response = httpclient.execute(request);
        StatusLine statusLine = response.getStatusLine();
        assertEquals(statusLine.getStatusCode(), HTTPStatus.NOT_FOUND.value());
        assertEquals(statusLine.getReasonPhrase(), HTTPStatus.NOT_FOUND.getReasonPhrase());
        HttpEntity entity = response.getEntity();
        String content = Util.readAsUtfString(entity.getContent());

        assertEquals(content, TemplateRepository.instance().buildError(HTTPStatus.NOT_FOUND, ""));

    }

    @Test
    public void testResourceFoundInRoot() throws IOException {
        String fileName = "www/file.txt";
        File file = new File(ClassLoader.getSystemResource("www/file.txt").getPath());
        String extension = Util.extension(fileName);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/file.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.addHeader("Connection","Keep-Alive");
        request.addHeader("Accept-Encoding","gzip,deflate");


        CloseableHttpResponse response = httpclient.execute(request);


        String fileContent = Util.readAsUtfString(fileName);

        StatusLine statusLine = response.getStatusLine();
        assertEquals(statusLine.getStatusCode(), HTTPStatus.OK.value());
        assertEquals(response.getFirstHeader(CONNECTION).getValue(), "Keep-Alive");
        assertEquals(response.getFirstHeader(CONTENT_LENGTH),null);
        assertEquals(response.getFirstHeader(CONTENT_TYPE).getValue(), Mime.getType(extension));
        //this header is removed in case of content decompression by the http client
//        assertEquals(response.getFirstHeader(CONTENT_ENCODING).getValue(),"gzip");
        assertEquals(response.getFirstHeader(ETAG).getValue(), EtagManager.getInstance().getFileEtag(file,true));//will not return ETAG for http1.0
//
//        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
//        FileTime atime = attrs.lastAccessTime();
//        FileTime mtime = attrs.lastModifiedTime();


        LocalDateTime date = DateUtil.parseRfc2161CompliantDate(response.getFirstHeader(LAST_MODIFIED).getValue());
        LocalDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("UTC")).toLocalDateTime();
        assertEquals(date,modifiedDate);


        HttpEntity entity = response.getEntity();
        String content = Util.readAsUtfString(entity.getContent());
        assertEquals(content, Util.readAsUtfString("www/file.txt"));
    }

    @Test
    public void testResourceWithSpecialChars() throws IOException {
        String fileName = "www/folder1/a?b.txt";
        String extension = Util.extension(fileName);

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/folder1/a%3Fb.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        CloseableHttpResponse response = httpclient.execute(request);

        String fileContent = Util.readAsUtfString(fileName);

        StatusLine statusLine = response.getStatusLine();
        assertEquals(statusLine.getStatusCode(), HTTPStatus.OK.value());
        assertEquals(response.getFirstHeader(CONNECTION).getValue(), "Keep-Alive");
        assertEquals(response.getFirstHeader(HTTPHeaders.CONTENT_LENGTH).getValue(), "" + fileContent.length());
        assertEquals(response.getFirstHeader(HTTPHeaders.CONTENT_TYPE).getValue(), Mime.getType(extension));
        HttpEntity entity = response.getEntity();
        String content = Util.readAsUtfString(entity.getContent());
        assertEquals(content, Util.readAsUtfString(fileName));

    }

    @Test
    public void testForbiddenResource() throws IOException {

        HttpGet request = new HttpGet("http://" + host + ":" + port + "/../outsideWWWParent.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        CloseableHttpResponse response = httpclient.execute(request);

        StatusLine statusLine = response.getStatusLine();
        assertEquals(statusLine.getStatusCode(), HTTPStatus.FORBIDDEN.value());
        assertEquals(statusLine.getReasonPhrase(), HTTPStatus.FORBIDDEN.getReasonPhrase());
        HttpEntity entity = response.getEntity();
        String content = Util.readAsUtfString(entity.getContent());
        assertEquals(content, TemplateRepository.instance().buildError(HTTPStatus.FORBIDDEN, ""));

    }

}

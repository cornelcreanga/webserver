package com.ccreanga.webserver.it.get;


import com.ccreanga.webserver.*;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class ServerGetTestIT {

    private static Server server;
    private String host = "127.0.0.1";
    private static Configuration configuration = new Configuration();
    private static String port = "8999";


    @BeforeClass
    public static void init() {
        Properties properties = new Properties();
        properties.put("serverPort", port);
        properties.put("rootFolder", ClassLoader.getSystemResource("www").getPath());
        properties.put("initialThreads", "128");
        properties.put("maxThreads", "1000");
        properties.put("waitQueue", "64");
        properties.put("weakEtag", "true");
        properties.put("maxGetSize", "4096");
        properties.put("maxHeaders", "64");
        properties.put("timeoutSeconds", "5000");
        properties.put("maxGetBodySize", "64000");
        properties.put("maxPutBodySize", "2147483648");
        properties.put("xForwardedForTag", "false");
        properties.put("chunkLength", "131072");
        configuration.loadFromProperties(properties);

        server = new Server(configuration);

        try {
            new Thread(server).start();
            Thread.sleep(100);
        } catch (InternalException e) {
            e.printStackTrace();//todo
            server.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void clean() {
        server.stop();
    }

    @Test
    public void testResourceNotFound() {

        try {
            Request.Get("http://" + host + ":" + port + "/notExisting.html")
                    .execute().handleResponse((ResponseHandler<Object>) response -> {
                StatusLine statusLine = response.getStatusLine();
                Assert.assertEquals(statusLine.getStatusCode(), HttpStatus.NOT_FOUND.value());
                Assert.assertEquals(statusLine.getReasonPhrase(), HttpStatus.NOT_FOUND.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                String content = Util.readAsUtfString(entity.getContent());
                Assert.assertEquals(content, "");
                return entity;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testResourceFoundInRoot() {

        String fileName = "www/file.txt";
        String extension = Util.extension(fileName);

        try {
            Request.Get("http://" + host + ":" + port + "/file.txt")
                    .execute().handleResponse((ResponseHandler<Object>) response -> {

                String fileContent = Util.readAsUtfString(fileName);

                StatusLine statusLine = response.getStatusLine();
                Assert.assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
                Assert.assertEquals(response.getFirstHeader(HTTPHeaders.connection).getValue(), "keep-alive");
                Assert.assertEquals(response.getFirstHeader(HTTPHeaders.contentLength).getValue(), "" + fileContent.length());
                Assert.assertEquals(response.getFirstHeader(HTTPHeaders.contentType).getValue(), Mime.getType(extension));
                HttpEntity entity = response.getEntity();
                String content = Util.readAsUtfString(entity.getContent());
                Assert.assertEquals(content, Util.readAsUtfString("www/file.txt"));
                return entity;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testResourceWithSpecialChars() {
        String fileName = "www/parent1/a?b.txt";
        String extension = Util.extension(fileName);
        try {
            Request.Get("http://" + host + ":" + port + "/parent1/a%3Fb.txt")
                    .execute().handleResponse((ResponseHandler<Object>) response -> {

                String fileContent = Util.readAsUtfString(fileName);

                StatusLine statusLine = response.getStatusLine();
                Assert.assertEquals(statusLine.getStatusCode(), HttpStatus.OK.value());
                Assert.assertEquals(response.getFirstHeader(HTTPHeaders.connection).getValue(), "keep-alive");
                Assert.assertEquals(response.getFirstHeader(HTTPHeaders.contentLength).getValue(), "" + fileContent.length());
                Assert.assertEquals(response.getFirstHeader(HTTPHeaders.contentType).getValue(), Mime.getType(extension));
                HttpEntity entity = response.getEntity();
                String content = Util.readAsUtfString(entity.getContent());
                Assert.assertEquals(content, Util.readAsUtfString(fileName));
                return entity;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testForbiddenResource() {
        try {
            Request.Get("http://" + host + ":" + port + "/../outsideWWWParent.txt")
                    .execute().handleResponse((ResponseHandler<Object>) response -> {

                StatusLine statusLine = response.getStatusLine();
                Assert.assertEquals(statusLine.getStatusCode(), HttpStatus.FORBIDDEN.value());
                Assert.assertEquals(statusLine.getReasonPhrase(), HttpStatus.FORBIDDEN.getReasonPhrase());
                HttpEntity entity = response.getEntity();
                String content = Util.readAsUtfString(entity.getContent());
                Assert.assertEquals(content, "");
                return entity;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

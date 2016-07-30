package com.ccreanga.webserver.it.get;


import com.ccreanga.webserver.*;
import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.Mime;
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
        properties.put("serverRootFolder", ClassLoader.getSystemResource("www").getPath());
        properties.put("serverInitialThreads", "128");
        properties.put("serverMaxThreads", "1000");

        properties.put("requestTimeoutSeconds", "100");
        properties.put("requestWaitingQueueSize", "64");

        properties.put("shouldUseWeakEtag", "weak");

        properties.put("requestMaxLines", "200");
        properties.put("requestMaxLineLength", "1024");
        properties.put("requestMaxHeaders", "64");
        properties.put("requestMaxGetBodySize", "64000");
        properties.put("requestMaxPutBodySize", "2147483648");

        properties.put("verbose", "true");
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
    public void testResourceNotFound() throws IOException {
        Request.Get("http://" + host + ":" + port + "/notExisting.html")
                .execute().handleResponse((ResponseHandler<Object>) response -> {
            StatusLine statusLine = response.getStatusLine();
            Assert.assertEquals(statusLine.getStatusCode(), HTTPStatus.NOT_FOUND.value());
            Assert.assertEquals(statusLine.getReasonPhrase(), HTTPStatus.NOT_FOUND.getReasonPhrase());
            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());

            Assert.assertEquals(content, TemplateRepository.instance().buildError(HTTPStatus.NOT_FOUND, ""));

            return entity;
        });


    }

    @Test
    public void testResourceFoundInRoot() throws IOException {
        String fileName = "www/file.txt";
        String extension = Util.extension(fileName);

        Request.Get("http://" + host + ":" + port + "/file.txt")
                .execute().handleResponse((ResponseHandler<Object>) response -> {

            String fileContent = Util.readAsUtfString(fileName);

            StatusLine statusLine = response.getStatusLine();
            Assert.assertEquals(statusLine.getStatusCode(), HTTPStatus.OK.value());
            Assert.assertEquals(response.getFirstHeader(HTTPHeaders.CONNECTION).getValue(), "Keep-Alive");
            Assert.assertEquals(response.getFirstHeader(HTTPHeaders.CONTENT_LENGTH).getValue(), "" + fileContent.length());
            Assert.assertEquals(response.getFirstHeader(HTTPHeaders.CONTENT_TYPE).getValue(), Mime.getType(extension));
            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());
            Assert.assertEquals(content, Util.readAsUtfString("www/file.txt"));
            return entity;
        });


    }

    @Test
    public void testResourceWithSpecialChars() throws IOException {
        String fileName = "www/folder1/a?b.txt";
        String extension = Util.extension(fileName);

        Request.Get("http://" + host + ":" + port + "/folder1/a%3Fb.txt")
                .execute().handleResponse((ResponseHandler<Object>) response -> {

            String fileContent = Util.readAsUtfString(fileName);

            StatusLine statusLine = response.getStatusLine();
            Assert.assertEquals(statusLine.getStatusCode(), HTTPStatus.OK.value());
            Assert.assertEquals(response.getFirstHeader(HTTPHeaders.CONNECTION).getValue(), "Keep-Alive");
            Assert.assertEquals(response.getFirstHeader(HTTPHeaders.CONTENT_LENGTH).getValue(), "" + fileContent.length());
            Assert.assertEquals(response.getFirstHeader(HTTPHeaders.CONTENT_TYPE).getValue(), Mime.getType(extension));
            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());
            Assert.assertEquals(content, Util.readAsUtfString(fileName));
            return entity;
        });

    }

    @Test
    public void testForbiddenResource() throws IOException {

        Request.Get("http://" + host + ":" + port + "/../outsideWWWParent.txt")
                .execute().handleResponse((ResponseHandler<Object>) response -> {

            StatusLine statusLine = response.getStatusLine();
            Assert.assertEquals(statusLine.getStatusCode(), HTTPStatus.FORBIDDEN.value());
            Assert.assertEquals(statusLine.getReasonPhrase(), HTTPStatus.FORBIDDEN.getReasonPhrase());
            HttpEntity entity = response.getEntity();
            String content = Util.readAsUtfString(entity.getContent());
            Assert.assertEquals(content, TemplateRepository.instance().buildError(HTTPStatus.FORBIDDEN, ""));


            return entity;
        });

    }

}

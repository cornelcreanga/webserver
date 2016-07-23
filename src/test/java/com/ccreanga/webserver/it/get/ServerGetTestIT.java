package com.ccreanga.webserver.it.get;


import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.Server;
import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.Util;
import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class ServerGetTestIT {

    static Server server;
    Configuration configuration = new Configuration();
    String host = "127.0.0.1";
    int port = configuration.getServerPort();

    @BeforeClass
    public static void init(){
        server = new Server();

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
    public static void clean(){
        server.stop();
    }

    @Test
    public void testProjects() {
        System.out.println(ClassLoader.getSystemResource("www").getPath());

        try {
            Request.Get("http://"+host+":"+port+"/notExisting.html")
                    .execute().handleResponse((ResponseHandler<Object>) response -> {
                        StatusLine statusLine = response.getStatusLine();
//                        Assert.assertEquals(statusLine.getStatusCode(),HttpStatus.NOT_FOUND.value());
//                        Assert.assertEquals(statusLine.getReasonPhrase(),HttpStatus.NOT_FOUND.getReasonPhrase());
                        HttpEntity entity = response.getEntity();
                        String content  = Util.readAsUtfString(entity.getContent());

                        if (entity == null) {
                            throw new ClientProtocolException("Response contains no content");
                        }


                        ContentType contentType = ContentType.getOrDefault(entity);
                        if (!contentType.equals(ContentType.APPLICATION_XML)) {
                            throw new ClientProtocolException("Unexpected content type:" +
                                    contentType);
                        }
                        return entity;
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}

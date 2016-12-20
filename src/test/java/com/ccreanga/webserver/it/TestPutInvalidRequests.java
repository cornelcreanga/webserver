package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import static com.ccreanga.webserver.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;

public class TestPutInvalidRequests extends TestParent {

    @Test
    public void testForbiddenURI() throws Exception {
        HttpPut request = new HttpPut("http://" + host + ":" + port + "/../cucu.txt");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "text/plain");
        final String text = "test text";
        request.setEntity(new ByteArrayEntity(text.getBytes()));
        checkForStatus(request, HttpStatus.FORBIDDEN);
    }


    @Test
    public void testFormUrlEncodedContentType() throws Exception {
        HttpPut request = new HttpPut("http://" + host + ":" + port + "/cucu/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
        final String text = "test text";
        request.setEntity(new ByteArrayEntity(text.getBytes()));
        checkForStatus(request, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testMultiPartContentType() throws Exception {
        HttpPut request = new HttpPut("http://" + host + ":" + port + "/cucu/");
        request.setProtocolVersion(HttpVersion.HTTP_1_1);
        request.setHeader(CONTENT_TYPE, "multipart/form-data");
        final String text = "test text";
        request.setEntity(new ByteArrayEntity(text.getBytes()));
        checkForStatus(request, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testPutInvalidCharName() throws Exception {

        try (Socket socket = new Socket(host, Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            out.write("PUT /cucu/puc\u0000u/file.txt HTTP/1.1\n".getBytes());
            out.write("Host: localhost\n".getBytes());

            out.write("Accept: */*\n".getBytes());
            out.write("Content-Type: text/plain\n".getBytes());
            out.write("Content-Length: 35\n\n".getBytes());
            out.write("{\"username\":\"xyz\",\"password\":\"xyz\"}".getBytes());

            out.flush();
            Thread.sleep(2000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = reader.readLine();
            assertEquals(line, "HTTP/1.1 400 Bad Request");
        }


    }


}

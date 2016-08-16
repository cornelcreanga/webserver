package com.ccreanga.webserver.it;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.Assert.assertEquals;


public class TestGetPersistentHttp1_1 extends TestParent {

    @Test
    public void testKeepAlive() throws Exception {
        try (Socket socket = new Socket(host, Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            out.write("GET /file.txt HTTP/1.1\n".getBytes());
            out.write("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n".getBytes());
            out.write("Host:localhost:8082\n".getBytes());
            out.write("Accept-Encoding:gzip, deflate, sdch\n".getBytes());
            out.write("\n".getBytes());
            out.flush();

            Thread.sleep(2000);

            out.write("GET /folder2/test.txt HTTP/1.1\n".getBytes());
            out.write("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n".getBytes());
            out.write("Host:localhost:8082\n".getBytes());
            out.write("Accept-Encoding:gzip, deflate, sdch\n".getBytes());
            out.write("\n".getBytes());
            out.flush();

            Thread.sleep(2000);

            //we should be able to read both responses using the same socket
            int count = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("HTTP/1.1 200 OK"))
                    count++;
            }
            assertEquals(count, 2);

        }

    }

}

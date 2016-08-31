package com.ccreanga.webserver.it;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import static com.ccreanga.webserver.http.HttpHeaders.EXPECT;
import static org.junit.Assert.assertEquals;

public class TestExpectContinue extends TestParent {

    @Test
    public void testHttpContinue() throws Exception {
        try (Socket socket = new Socket(host, Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            OutputStream out = socket.getOutputStream();

            out.write("GET /file.txt HTTP/1.1\n".getBytes());
            out.write("Host: test.com\n".getBytes());
            out.write((EXPECT + ": 100-continue\n").getBytes());
            out.write("\n".getBytes());
            out.flush();

            //Thread.sleep(500);

            String line = reader.readLine();
            assertEquals(line, "HTTP/1.1 100 Continue");
            line = reader.readLine();
            assertEquals(line, "HTTP/1.1 200 OK");
        }

    }

}

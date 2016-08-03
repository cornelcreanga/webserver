package com.ccreanga.webserver.it.get;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestGetPersistentConnectionHttp1_0 extends ClientTest {


    @Test
    public void testWithKeepAlive() throws Exception {
        testTwoConnections(true);
    }
    @Test
    public void testWithoutKeepAlive() throws Exception {
        testTwoConnections(false);
    }

    public void testTwoConnections(boolean keepAlive) throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            out.write("GET /file.txt HTTP/1.0\n".getBytes());
            out.write("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n".getBytes());
            out.write("Host:localhost:8082\n".getBytes());
            out.write("Accept-Encoding:gzip, deflate, sdch\n".getBytes());
            if (keepAlive)
                out.write("Connection:keep-alive\n".getBytes());

            out.write("\n".getBytes());
            out.flush();

            Thread.sleep(2000);

            boolean brokenConnection = false;
            try {
                out.write("GET /folder2/test.txt HTTP/1.0\n".getBytes());
                out.flush();
                out.write("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n".getBytes());
                out.flush();
                out.write("Host:localhost:8082\n".getBytes());
                out.flush();
                out.write("Accept-Encoding:gzip, deflate, sdch\n".getBytes());
                out.flush();
                if (keepAlive)
                    out.write("Connection:keep-alive\n".getBytes());
                out.flush();
                out.write("\n".getBytes());
                out.flush();
            }catch (SocketException e){
                brokenConnection = true;
            }
            if (!keepAlive)
                assertTrue(brokenConnection);

            Thread.sleep(2000);

            //we should be able to read both responses using the same socket
            int count = 0;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            String line;
            while((line=reader.readLine())!=null){
                System.out.println(line);
                if (line.contains("HTTP/1.1 200 OK"))
                    count++;
            }
            assertEquals(count, keepAlive?2:1);


        }

    }


}

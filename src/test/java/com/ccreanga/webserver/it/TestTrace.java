package com.ccreanga.webserver.it;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import static com.google.common.net.HttpHeaders.EXPECT;
import static org.junit.Assert.assertEquals;

public class TestTrace extends TestParent {

    @Test
    public void testTrace() throws Exception {
        try (Socket socket = new Socket(host, Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            OutputStream out = socket.getOutputStream();

            out.write("TRACE /file.txt HTTP/1.1\n".getBytes());
            out.write("Host: test.com\n".getBytes());
            out.write("Connection:keep-alive\n".getBytes());
            out.write("Accept-Encoding:gzip, deflate, sdch\n".getBytes());
            out.write("\n".getBytes());
            out.flush();

            //Thread.sleep(500);
            while(true){
                String line = reader.readLine();
                if (line==null)
                    break;
                System.out.println(line);
            }
        }

    }

}

package com.ccreanga.webserver.it.get;

import com.ccreanga.webserver.http.HTTPStatus;
import org.apache.http.config.SocketConfig;
import org.junit.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class TestGetInvalidRequest extends ClientTest{

    @Test
    public void testInvalidHttpMessage() throws Exception {
        Socket socket = new Socket(host,Integer.parseInt(port));
        socket.setSoTimeout(10000);
        InputStream in = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        OutputStream out = socket.getOutputStream();
        out.write("not an http request\n".getBytes());
        out.flush();
        String  line = reader.readLine();
        assertEquals(line, "HTTP/1.1 400 Bad Request");
        out.write("not an http request\n".getBytes());
        Thread.sleep(20000);
        System.out.println(socket.isClosed());
        System.out.println(socket.isInputShutdown());
        System.out.println(socket.isOutputShutdown());

    }

}

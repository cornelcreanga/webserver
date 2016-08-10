package com.ccreanga.webserver.it;

import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestGetInvalidRequests extends TestParent {

    @Test
    public void testInvalidHttpMessage1() throws Exception {
        testInvalidHttpMessage("not an http request\n");
    }

    @Test
    public void testInvalidHttpVersion() throws Exception {
        testInvalidHttpMessage("GET /file.txt HTTP/4.2\n");
    }

    @Test
    public void testInvalidHttpMethod() throws Exception {
        testInvalidHttpMessage("SING /file.txt HTTP/1.1\n");
    }

    public void testInvalidHttpMessage(String httpMessage) throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))){
            socket.setSoTimeout(3000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            OutputStream out = socket.getOutputStream();
            out.write(httpMessage.getBytes());
            out.flush();
            String  line = reader.readLine();
            assertEquals(line, "HTTP/1.1 400 Bad Request");
        }
        //todo - check if the connection is closed.

    }

    @Test
    public void testHttpMessageNoHeaders() throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            out.write("GET /file.txt HTTP/1.1\n".getBytes());
            out.flush();
            //do not write anything else. the server will wait for headers until it will throw SocketTimeoutException and
            //it will close the connection
            //
            boolean socketClosed = true;
            try {
                while (in.read() != -1) ;
            } catch (SocketTimeoutException e) {
                socketClosed = false;
            }
            assertTrue(socketClosed);
        }
    }
    @Test
    public void testHttpMessageUnparsableHeaders() throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            OutputStream out = socket.getOutputStream();
            out.write("GET /file.txt HTTP/1.1\n".getBytes());
            out.write("not a header\n".getBytes());
            out.flush();
            String  line = reader.readLine();
            assertEquals(line, "HTTP/1.1 400 Bad Request");
        }
    }

    @Test
    public void testHttpMessageLineTooLong() throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            OutputStream out = socket.getOutputStream();
            StringBuilder sb = new StringBuilder("GET");
            for (int i = 0; i < 2000; i++) {
                 sb.append(" ");
            }
            sb.append(" /file.txt HTTP/1.1\n");
            out.write(sb.toString().getBytes());
            out.write("Host: test.com\n".getBytes());
            out.flush();
            //the long line will be truncated to Configuration.requestMaxLineLength and the request will be unparsable
            String  line = reader.readLine();
            assertEquals(line, "HTTP/1.1 400 Bad Request");
        }

    }

    @Test
    public void testHttpMessageUriTooLong() throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            OutputStream out = socket.getOutputStream();
            StringBuilder sb = new StringBuilder(9000);
            sb.append("GET /");
            for (int i = 0; i < 8001; i++) {
                sb.append("f");
            }
            sb.append(".txt HTTP/1.1\n");
            out.write(sb.toString().getBytes());
            out.write("Host: test.com\n".getBytes());
            out.flush();
            //the long line will be truncated to Configuration.requestMaxLineLength and the request will be unparsable
            String  line = reader.readLine();
            assertEquals(line, "HTTP/1.1 414 URI Too Long");
        }

    }

    @Test
    @Ignore//todo
    public void testHttpMessageTooLong() throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            OutputStream out = socket.getOutputStream();
            out.write("GET /file.txt HTTP/1.1\n".getBytes());
            out.write("Host: test.com\n".getBytes());
            out.write("Content-length: 20000\n\n".getBytes());
            StringBuilder sb = new StringBuilder(20000);
            for (int i = 0; i < 20000; i++) {
                sb.append("x");
            }
            out.write(sb.toString().getBytes());
            out.flush();
            String  line = reader.readLine();
            assertEquals(line, "HTTP/1.1 400 Bad Request");
        }

    }

    @Test
    public void testHttpMessageTooManyHeaders() throws Exception {
        try(Socket socket = new Socket(host,Integer.parseInt(port))) {
            socket.setSoTimeout(30000);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            OutputStream out = socket.getOutputStream();

            out.write("GET /file.txt HTTP/1.1\n".getBytes());
            StringBuilder sb = new StringBuilder(3000);
            for (int i = 0; i < 150; i++) {
                sb.append("Header"+i+": test\n");
            }
            sb.append("\n");
            out.write(sb.toString().getBytes());
            out.flush();

            String  line = reader.readLine();
            assertEquals(line, "HTTP/1.1 431 Request Header Fields Too Large");
        }

    }


}

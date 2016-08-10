package com.ccreanga.webserver.it;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.ioutil.ChunkedInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestChunkedInputStream extends TestParent {

    @Test
    public void testChunked() throws IOException {

        String s = "1a; ignore-stuff-here\n" +
                "abcdefghijklmnopqrstuvwxyz\n" +
                "10\n" +
                "1234567890abcdef\n" +
                "0\n" +
                "some-footer: some-value\n" +
                "another-footer: another-value\n\n\r";

        InputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        HTTPHeaders headers = new HTTPHeaders();
        headers.appendHeader("Trailer","some-footer");
        ChunkedInputStream stream = new ChunkedInputStream(in,headers,100,10);
        int i;
        while((i=stream.read())!=-1){
            System.out.print((char)i);
        }
        //todo
        System.out.println(headers.getAllHeadersMap().size());


    }

}

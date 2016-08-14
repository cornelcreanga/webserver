package com.ccreanga.webserver.http.chunked;

import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.chunked.ChunkedInputStream;
import com.ccreanga.webserver.http.chunked.ChunkedParseException;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ChunkedInputStreamTest {

    @Test
    public void testChunkedOK() throws IOException {

        String initial="abcdefghijklmnopqrstuvwxyz1234567890abcdef";

        String chunkedData = "1a; I'm an extension\n" +
                "abcdefghijklmnopqrstuvwxyz\n" +
                "10\n" +
                "1234567890abcdef\n" +
                "0\n" +
                "some-footer: some-value\n" +
                "another-footer: another-value\n\n\r";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.appendHeader("Trailer","some-footer");
        headers.appendHeader("Trailer","another-footer");
        ChunkedInputStream stream = new ChunkedInputStream(in,headers,1000,100,10);
        String string = CharStreams.toString( new InputStreamReader( stream, "UTF-8" ) );
        assertEquals(string,initial);
        assertEquals(headers.getAllHeadersMap().size(),3);
        assertEquals(headers.getHeader("some-footer"),"some-value");
        assertEquals(headers.getHeader("another-footer"),"another-value");
    }

    @Test
    public void testChunkedInvalid() throws IOException {
        String chunkedData = "1a; I'm an extension\n" +
                "abcdefghijkXXXXlmnopqrstuvwxyz\n" +
                "10\n" +
                "1234567890abcdef\n" +
                "0\n" +
                "some-footer: some-value\n" +
                "another-footer: another-value\n\n\r";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        ChunkedInputStream stream = new ChunkedInputStream(in,headers,1000,100,10);

        boolean error = false;
        try{
            String string = CharStreams.toString( new InputStreamReader( stream, "UTF-8" ) );
        }catch (ChunkedParseException e){
            error = true;
        }
        assertTrue(error);
    }


}
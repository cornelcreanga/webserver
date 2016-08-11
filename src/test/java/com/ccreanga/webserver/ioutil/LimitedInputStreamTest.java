package com.ccreanga.webserver.ioutil;

import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class LimitedInputStreamTest {

    @Test
    public void testStreamTooLong() throws IOException {
        String chunkedData = "aaaaaaaaaaaaaaaaaaaa";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        LimitedInputStream stream = new LimitedInputStream(in,10);

        boolean error = false;
        try{
            CharStreams.toString( new InputStreamReader( stream, "UTF-8" ) );
        }catch (LengthExceededException e){
            error = true;
        }
        assertTrue(error);

    }

    @Test
    public void testStreamOk() throws IOException {
        String chunkedData = "aaaaaaaaaaaaaaaaaaaa";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        LimitedInputStream stream = new LimitedInputStream(in,100);

        String result = CharStreams.toString( new InputStreamReader( stream, "UTF-8" ) );
        assertEquals(chunkedData,result);

    }

}
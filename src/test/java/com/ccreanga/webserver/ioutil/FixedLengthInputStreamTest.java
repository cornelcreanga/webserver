package com.ccreanga.webserver.ioutil;

import com.ccreanga.webserver.http.chunked.ChunkedParseException;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;


public class FixedLengthInputStreamTest {

    @Test
    public void testStreamTooShort() throws IOException {
        String chunkedData = "aaaaaaaaaaaaaaaaaaaa";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(in,300,true);

        boolean error = false;
        try{
            String string = CharStreams.toString( new InputStreamReader( fixedLengthInputStream, "UTF-8" ) );
        }catch (StreamExhaustedException e){
            error = true;
        }
        assertTrue(error);

    }

}
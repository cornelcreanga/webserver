package com.ccreanga.webserver.ioutil;

import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class FixedLengthInputStreamTest {

    @Test
    public void testStreamTooShort() throws IOException {
        String chunkedData = "1a; ignore-stuff-here\n" +
                "abcdefghijklmnopqrstuvwxyz\n" +
                "10\n" +
                "1234567890abcdef\n" +
                "0\n" +
                "some-footer: some-value\n" +
                "another-footer: another-value\n\n\r";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        FixedLengthInputStream fixedLengthInputStream = new FixedLengthInputStream(in,300,true);
        String string = CharStreams.toString( new InputStreamReader( fixedLengthInputStream, "UTF-8" ) );
        System.out.println(string);
    }

}
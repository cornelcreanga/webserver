package com.ccreanga.webserver.ioutil;

import com.ccreanga.webserver.Util;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LimitedInputStreamTest {

    @Test
    public void testStreamTooLong() throws IOException {
        String chunkedData = "aaaaaaaaaaaaaaaaaaaa";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        LimitedInputStream stream = new LimitedInputStream(in, 10);

        boolean error = false;
        try {
            Util.readAsUtfString(stream);
        } catch (LimitedInputStream.LengthExceededException e) {
            error = true;
        }
        assertTrue(error);

    }

    @Test
    public void testStreamOk() throws IOException {
        String chunkedData = "aaaaaaaaaaaaaaaaaaaa";
        InputStream in = new ByteArrayInputStream(chunkedData.getBytes(StandardCharsets.UTF_8));
        LimitedInputStream stream = new LimitedInputStream(in, 100);

        String result = Util.readAsUtfString(stream);
        assertEquals(chunkedData, result);

    }

}
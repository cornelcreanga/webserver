package com.ccreanga.webserver.http.chunked;

import com.ccreanga.webserver.http.HttpHeaders;
import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChunkedInputOutputStreamsTest {

    @Test
    public void testChunkedOK() throws IOException {

        String s = "abcdefghijklmnopqrstuvwxyz01234567890";
        byte[] data = s.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChunkedOutputStream out = new ChunkedOutputStream(baos);
        out.write(data, 0, 10);
        out.write(data, 10, 10);
        out.write(data, 20, data.length - 20);
        out.writeClosingChunk();
        byte[] result = baos.toByteArray();

        ChunkedInputStream in = new ChunkedInputStream(new ByteArrayInputStream(result), new HttpHeaders(), 10000, 100, 100);
        baos.reset();
        int i;
        while ((i = in.read()) > -1)
            baos.write(i);
        Assert.assertEquals(s, new String(baos.toByteArray()));

    }

}

package com.ccreanga.webserver.http.chunked;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class ChunkedOutputStreamTest {
    @Test
    public void testChunkedOK() throws IOException {
        String s = "abcdefghijklmnopqrstuvwxyz01234567890";
        byte[] toCompare = new byte[]{97,13,10,97,98,99,100,101,102,103,104,105,106,13,10,97,13,10,107,108,109,110,111,112,113,114,115,116,13,10,49,49,13,10,117,118,119,120,121,122,48,49,50,51,52,53,54,55,56,57,48,13,10,48,13,10,13,10};
        byte[] data = s.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChunkedOutputStream out = new ChunkedOutputStream(baos);
        out.write(data,0,10);
        out.write(data,10,10);
        out.write(data,20,data.length-20);
        out.writeClosingChunk();
        byte[] result = baos.toByteArray();
        Assert.assertArrayEquals(result,toCompare);
        System.out.println(new String(result));
        for (int i = 0; i < result.length; i++) {
            byte b = result[i];
            System.out.print(b);
            System.out.print(',');

        }

    }
}
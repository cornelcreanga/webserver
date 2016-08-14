package com.ccreanga.webserver.ioutil;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class IOUtilTest {
    @Test
    public void testCopy() throws Exception {
        String test = "123456789";
        InputStream in = new ByteArrayInputStream(test.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IOUtil.copy(in,out,0,test.length());
        String result =  new String(out.toByteArray());
        assertEquals(result,"123456789");

        in = new ByteArrayInputStream(test.getBytes());
        out.reset();
        IOUtil.copy(in,out,0,1);
        result =  new String(out.toByteArray());
        assertEquals(result,"1");

        in = new ByteArrayInputStream(test.getBytes());
        out.reset();
        IOUtil.copy(in,out,1,3);
        result =  new String(out.toByteArray());
        assertEquals(result,"234");

    }

}
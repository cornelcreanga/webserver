package com.ccreanga.webserver.http;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class HttpRequestParserTest {
    @Test
    public void parseParameters() throws Exception {

        Map<String, String> params = HttpRequestParser.parseParameters("param1=cucu&param2=mumu");
        Assert.assertEquals(params.get("param1"), "cucu");
        Assert.assertEquals(params.get("param2"), "mumu");

        params = HttpRequestParser.parseParameters("param1&param2=c%26c%26");
        Assert.assertEquals(params.get("param1"), "");
        Assert.assertEquals(params.get("param2"), "c&c&");

        params = HttpRequestParser.parseParameters("param2=c%26c%26");
        Assert.assertEquals(params.get("param2"), "c&c&");

    }

}

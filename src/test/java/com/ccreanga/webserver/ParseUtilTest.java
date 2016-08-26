package com.ccreanga.webserver;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ParseUtilTest {
    @Test
    public void parseLong() throws Exception {
        assertEquals(ParseUtil.parseLong("7",1,10),7);
    }

    @Test(expected = NumberFormatException.class)
    public void parseInvalidLong() throws Exception {
        assertEquals(ParseUtil.parseLong("we7",1,10),7);
    }


    @Test(expected = NumberFormatException.class)
    public void parseLongOutsideOfInterval() throws Exception {
        assertEquals(ParseUtil.parseLong("7",10,20),7);
    }


    @Test
    public void split() throws Exception {

    }

    @Test
    public void left() throws Exception {
        assertEquals(ParseUtil.left("123-4",'-'),"123");
        assertEquals(ParseUtil.left("123",'-'),"123");
    }

    @Test
    public void right() throws Exception {
        assertEquals(ParseUtil.right("123-4",'-'),"4");
        assertEquals(ParseUtil.right("123",'-'),"");
    }

    @Test
    public void parseFormEncodedParams() throws Exception {
        String form = "a=1&a=2&firstname=%CE%B3%CE%BB%CF%8E%CF%83%CF%83%CE%B1&lastname=%26%26nume-%2B%3C%3E%21%3B%3F%3A%26&subject=oferte+de+colaborare";
        Map<String,List<String>> params = ParseUtil.parseFormEncodedParams(form, 1000);
        List<String> list = new ArrayList<>();
        list.add("1");list.add("2");
        assertEquals(params.get("a"),list);
        assertEquals(params.get("firstname").get(0),"γλώσσα");
        assertEquals(params.get("subject").get(0),"oferte de colaborare");
        assertEquals(params.get("lastname").get(0),"&&nume-+<>!;?:&");
    }

}
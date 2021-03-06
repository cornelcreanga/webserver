package com.ccreanga.webserver;

import com.ccreanga.webserver.common.StringUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StringUtilTest {
    @Test
    public void parseLong() {
        assertEquals(StringUtil.parseLong("7", 1, 10), 7);
    }

    @Test(expected = NumberFormatException.class)
    public void parseInvalidLong() throws Exception {
        assertEquals(StringUtil.parseLong("we7", 1, 10), 7);
    }


    @Test(expected = NumberFormatException.class)
    public void parseLongOutsideOfInterval() throws Exception {
        assertEquals(StringUtil.parseLong("7", 10, 20), 7);
    }


    @Test
    public void split() {
        assertEquals(StringUtil.split("a b c", ' ', false, 100), Arrays.asList("a", "b", "c"));
        assertEquals(StringUtil.split("a  b c", ' ', false, 100), Arrays.asList("a", "b", "c"));
        assertEquals(StringUtil.split("a  b c", ' ', true, 100), Arrays.asList("a", "", "b", "c"));
    }

    @Test(expected = TooManyEntriesException.class)
    public void splitTooManyItems() throws Exception {
        StringUtil.split("a b c", ' ', false, 2);
        StringUtil.split("a  b c", ' ', true, 3);
        StringUtil.split("    ", ' ', true, 3);
    }


    @Test
    public void left() {
        assertEquals(StringUtil.left("123-4", '-'), "123");
        assertEquals(StringUtil.left("123", '-'), "123");
    }

    @Test
    public void right() {
        assertEquals(StringUtil.right("123-4", '-'), "4");
        assertEquals(StringUtil.right("123", '-'), "");
    }

    @Test
    public void parseFormEncodedParams() {
        String form = "a=1&a=2&firstname=%CE%B3%CE%BB%CF%8E%CF%83%CF%83%CE%B1&lastname=%26%26nume-%2B%3C%3E%21%3B%3F%3A%26&subject=oferte+de+colaborare";
        Map<String, List<String>> params = StringUtil.parseFormEncodedParams(form, 1000);
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        assertEquals(params.get("a"), list);
        assertEquals(params.get("firstname").get(0), "γλώσσα");
        assertEquals(params.get("subject").get(0), "oferte de colaborare");
        assertEquals(params.get("lastname").get(0), "&&nume-+<>!;?:&");
    }

    @Test
    public void escapeURLComponent() {
        assertEquals(StringUtil.escapeURLComponent("1cucu .,+=-壹いち"), "1cucu%20.,+=-%E5%A3%B9%E3%81%84%E3%81%A1");
    }

}
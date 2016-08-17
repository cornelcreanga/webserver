package com.ccreanga.webserver.http;

import junit.framework.Assert;
import org.junit.Test;

public class RangeManagerTest {
    @Test
    public void testObtainRangeLastBytes() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        long[] range = manager.obtainRange("bytes=-1", 10);
        Assert.assertEquals(range[0], 9);
        Assert.assertEquals(range[1], 10);
    }

    @Test
    public void testObtainRangeFirstBytes() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        long[] range = manager.obtainRange("bytes=1-", 10);
        Assert.assertEquals(range[0], 1);
        Assert.assertEquals(range[1], 10);
    }

    @Test
    public void testObtainRangeIntervalBytes() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        long[] range = manager.obtainRange("bytes=1-5", 10);
        Assert.assertEquals(range[0], 1);
        Assert.assertEquals(range[1], 5);
    }

    @Test(expected = RangeException.class)
    public void testObtainRangeInvalidRange1() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        manager.obtainRange("", 10);
    }

    @Test(expected = RangeException.class)
    public void testObtainRangeInvalidRange2() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        manager.obtainRange("-", 10);
    }

    @Test(expected = RangeException.class)
    public void testObtainRangeInvalidRange3() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        manager.obtainRange("--", 10);
    }

    @Test(expected = RangeException.class)
    public void testObtainRangeInvalidRange4() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        manager.obtainRange("a", 10);
    }

    @Test(expected = RangeException.class)
    public void testObtainRangeInvalidRange5() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        manager.obtainRange("a-b", 10);
    }

    @Test(expected = RangeException.class)
    public void testObtainRangeInvalidRange6() throws Exception {
        RangeManager manager = RangeManager.getInstance();
        manager.obtainRange("3-2", 10);
    }

}
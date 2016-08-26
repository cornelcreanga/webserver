package com.ccreanga.webserver;


import com.google.common.base.Preconditions;

public class ParseUtil {

    public static long parseLong(String string, long min, long max) {
        long value = Preconditions.checkNotNull(Long.parseLong(string));
        if ((value < min) || (value > max))
            throw new NumberFormatException("expecting a number between " + min + " and " + max + " instead of " + value);
        return value;
    }


}

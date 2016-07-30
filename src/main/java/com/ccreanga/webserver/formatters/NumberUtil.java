package com.ccreanga.webserver.formatters;

import java.text.DecimalFormat;

public class NumberUtil {

    public static String fileSizePretty(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String formatMillis(long milliseconds) {
        DecimalFormat df = new DecimalFormat("#########.###");
        df.setGroupingUsed(true);
        df.setGroupingSize(3);
        return df.format((double) milliseconds / 1000);
    }
}
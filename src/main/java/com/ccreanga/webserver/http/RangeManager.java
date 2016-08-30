package com.ccreanga.webserver.http;

public class RangeManager {

    private static final RangeManager manager = new RangeManager();

    private RangeManager() {
    }

    public static RangeManager getInstance() {
        return manager;
    }

    /**
     * Obtain a range or throw an exception is the range is invalid
     *
     * @param rangeValue
     * @param fileLength
     * @return
     */
    public long[] obtainRange(String rangeValue, long fileLength) {
        if (rangeValue.contains(","))
            throw new RangeException("multiple ranges are not accepted " + rangeValue);//the server does not accept multiple ranges
        if (!rangeValue.startsWith("bytes="))
            throw new RangeException("only bytes ranges are accepted (and the header should specify that)" + rangeValue);//the server does not accept multiple ranges
        rangeValue = rangeValue.substring(6);
        int index = rangeValue.indexOf('-');
        if (index == -1)
            throw new RangeException("invalid range (missing -) " + rangeValue);
        String left = rangeValue.substring(0, index);
        String right = "";
        if (index != (rangeValue.length() - 1))
            right = rangeValue.substring(index + 1);
        if (left.isEmpty() && right.isEmpty())
            throw new RangeException("invalid range (missing values) " + rangeValue);
        long[] range = new long[2];
        try {
            if (left.isEmpty()) {
                range[0] = fileLength - Integer.parseInt(right);
                range[1] = fileLength;
            } else if (right.isEmpty()) {
                range[0] = Integer.parseInt(left);
                range[1] = fileLength;
            } else {
                range[0] = Integer.parseInt(left);
                range[1] = Math.min(Integer.parseInt(right), fileLength);
            }
            if ((range[0] < 0) || (range[0] > range[1]))
                throw new RangeException("invalid range values " + rangeValue);
            return range;

        } catch (NumberFormatException nfe) {
            throw new RangeException("invalid range values " + rangeValue);
        }
        /**
         * 500-600,601-999
         * bytes=-500
         Or:
         bytes=9500-
         */
    }

}

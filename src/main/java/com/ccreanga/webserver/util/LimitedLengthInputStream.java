package com.ccreanga.webserver.util;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Prevents DOS attacks - for example forgetting to end a line by CR/LF. Throws LengthExceededException if it's possible to read more from the input stream.
 */
public class LimitedLengthInputStream extends FilterInputStream {

    private long count = 0;
    private long maxSize;

    public LimitedLengthInputStream(InputStream in, long maxSize) {
        super(in);
        this.maxSize = maxSize;
    }

    public int read() throws IOException {
        int res = super.read();
        if (res != -1) {
            count++;
            if (count > maxSize)
                throw new LengthExceededException();
        }
        return res;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res > 0) {
            count += res;
            if (count > maxSize)
                throw new LengthExceededException();
        }
        return res;
    }

}

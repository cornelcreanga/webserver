package com.ccreanga.webserver.ioutil;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends FilterInputStream{
    /**
     * The maximum size of an item, in bytes.
     */
    private long sizeMax;
    /**
     * The current number of bytes.
     */
    private long count;
    private boolean closed;


    public LimitedInputStream(InputStream pIn, long pSizeMax) {
        super(pIn);
        sizeMax = pSizeMax;
    }

    private void checkLimit() throws IOException {
        if (count > sizeMax) {
            throw new LengthExceededException();
        }
    }

    public int read() throws IOException {
        int res = super.read();
        if (res != -1) {
            count++;
            checkLimit();
        }
        return res;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res > 0) {
            count += res;
            checkLimit();
        }
        return res;
    }

    public boolean isClosed() throws IOException {
        return closed;
    }

    public void close() throws IOException {
        closed = true;
        super.close();
    }
}

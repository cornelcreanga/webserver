package com.ccreanga.webserver.ioutil;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends FilterInputStream {

    protected long limit; // decremented when read, until it reaches zero
    protected boolean prematureEndException;

    public LimitedInputStream(InputStream in, long limit, boolean prematureEndException) {
        super(in);
        if (in == null)
            throw new NullPointerException("input stream is null");
        this.limit = limit < 0 ? 0 : limit;
        this.prematureEndException = prematureEndException;
    }

    @Override
    public int read() throws IOException {
        int res = limit == 0 ? -1 : in.read();
        if (res == -1 && limit > 0 && prematureEndException)
            throw new LengthExceededException();
        limit = res == -1 ? 0 : limit - 1;
        return res;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = limit == 0 ? -1 : in.read(b, off, len > limit ? (int)limit : len);
        if (res == -1 && limit > 0 && prematureEndException)
            throw new LengthExceededException();
        limit = res == -1 ? 0 : limit - res;
        return res;
    }

    @Override
    public long skip(long n) throws IOException {
        long res = in.skip(n > limit ? limit : n);
        limit -= res;
        return res;
    }

    @Override
    public int available() throws IOException {
        int res = in.available();
        return res > limit ? (int)limit : res;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() {
        limit = 0; // end this stream, but don't close the underlying stream
    }
}


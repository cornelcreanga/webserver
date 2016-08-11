package com.ccreanga.webserver.ioutil;

import com.google.common.base.Preconditions;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  Read a fixed amount of bytes from the underlying stream. If the underlying stream will return end of stream before the fixed
 *  amount of data is read it will throw an exception if prematureEndException is true
 */
public class FixedLengthInputStream extends FilterInputStream {

    protected long limit;
    protected boolean prematureEndException;

    public FixedLengthInputStream(InputStream in, long limit, boolean prematureEndException) {
        super(in);
        Preconditions.checkNotNull(in);
        this.limit = limit < 0 ? 0 : limit;
        this.prematureEndException = prematureEndException;
    }

    @Override
    public int read() throws IOException {
        int res = limit == 0 ? -1 : in.read();
        if (res == -1 && limit > 0 && prematureEndException)
            throw new StreamExhaustedException("unexpected end of stream");
        limit = res == -1 ? 0 : limit - 1;
        return res;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = limit == 0 ? -1 : in.read(b, off, len > limit ? (int)limit : len);
        if (res == -1 && limit > 0 && prematureEndException)
            throw new StreamExhaustedException("unexpected end of stream");
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


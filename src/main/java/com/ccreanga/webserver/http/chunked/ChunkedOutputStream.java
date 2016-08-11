package com.ccreanga.webserver.http.chunked;


import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * <p>
 * Wrapper supporting the chunked transfer encoding.
 * </p>
 *
 */
public class ChunkedOutputStream extends OutputStream {

    private static final byte CRLF[] = new byte[]{(byte) 13, (byte) 10};

    private static final byte ENDCHUNK[] = CRLF;

    private boolean closed = false;

    private OutputStream stream = null;

    private Function<byte[],byte[]> extensionBuilder;


    public ChunkedOutputStream(OutputStream stream) {
        this(stream,null);
    }


    public ChunkedOutputStream(OutputStream stream, Function<byte[],byte[]> extensionBuilder) {
        this.stream = Preconditions.checkNotNull(stream);
        this.extensionBuilder = extensionBuilder;
    }

    public void write(int b) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("write (int b) is not implemented, use write(byte[] b, int off, int len)");
    }

    public void write(byte[] b, int off, int len) throws IOException {

        if (closed) {
            throw new IllegalStateException("Output stream already closed");
        }
        byte[] chunkHeader = (Integer.toHexString(len)+ "\r\n").getBytes(Charsets.ISO_8859_1);
        stream.write(chunkHeader, 0, chunkHeader.length);
        if (extensionBuilder!=null){
            byte[] extension = extensionBuilder.apply(b);
            if (extension!=null) {
                stream.write(';');
                stream.write(extension,0,extension.length);
            }
        }
        stream.write(b, off, len);
        stream.write(ENDCHUNK, 0, ENDCHUNK.length);
    }

    public void writeClosingChunk() throws IOException {
        if (!closed) {
            try {
                stream.write(0);
                stream.write(CRLF, 0, CRLF.length);
                stream.write(ENDCHUNK, 0, ENDCHUNK.length);
            } finally {
                closed = true;
            }
        }
    }

    public void flush() throws IOException {
        stream.flush();
    }

    /**
     * Close this output stream. The underlying stream is not closed!
     *
     * @throws IOException if an error occurs closing the stream
     */
    public void close() throws IOException {
        writeClosingChunk();
        super.close();
    }

}

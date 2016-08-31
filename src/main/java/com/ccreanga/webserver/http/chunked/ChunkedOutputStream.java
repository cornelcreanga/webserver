package com.ccreanga.webserver.http.chunked;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * todo - format me
 * Chunked streaming enables content streams of unknown size to be transferred as a sequence of length-delimited buffers, which enables the sender to
 * retain connection persistence and the recipient to know when it has received the entire message.
 * Structure
 * chunked-body = *chunk
 * last-chunk
 * trailer-part
 * CRLF
 * chunk chunk-size
 * last-chunk = chunk-size [ chunk-ext ] CRLF
 * chunk-data CRLF
 * = 1*HEXDIG
 * = 1*("0") [ chunk-ext ] CRLF
 * chunk-data = 1*OCTET ; a sequence of chunk-size octets
 * The chunk-size field is a string of hex digits indicating the size of the chunk-data in octets. The chunked transfer coding is complete
 * when a chunk with a chunk-size of zero is received, possibly followed by a trailer, and finally terminated by an empty line.
 */
public class ChunkedOutputStream extends OutputStream {

    private boolean closed = false;

    private OutputStream stream = null;

    private Function<byte[], byte[]> extensionBuilder;


    public ChunkedOutputStream(OutputStream stream) {
        this(stream, null);
    }


    public ChunkedOutputStream(OutputStream stream, Function<byte[], byte[]> extensionBuilder) {
        this.stream = stream;
        this.extensionBuilder = extensionBuilder;
    }

    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("write (int b) is not implemented, use write(byte[] b, int off, int len)");
    }

    public void write(byte[] b, int off, int len) throws IOException {

        if (closed) {
            throw new IllegalStateException("Output stream already closed");
        }
        byte[] chunkHeader = (Integer.toHexString(len) + "\r\n").getBytes(StandardCharsets.ISO_8859_1);
        stream.write(chunkHeader, 0, chunkHeader.length);
        if (extensionBuilder != null) {
            byte[] extension = extensionBuilder.apply(b);
            if (extension != null) {
                stream.write(';');
                stream.write(extension, 0, extension.length);
            }
        }
        stream.write(b, off, len);
        writeCRLF(stream);
    }

    public void writeClosingChunk() throws IOException {
        if (!closed) {
            try {
                stream.write('0');
                writeCRLF(stream);
                //todo - add support for trailing headers
                writeCRLF(stream);
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

    private void writeCRLF(OutputStream out) throws IOException {
        out.write(13);
        out.write(10);
    }

}

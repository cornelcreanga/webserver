package com.ccreanga.webserver.ioutil;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpRequestParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.ccreanga.webserver.ioutil.IOUtil.readLine;
import static com.google.common.net.HttpHeaders.TE;
import static com.google.common.net.HttpHeaders.TRAILER;

public class ChunkedInputStream extends LimitedInputStream {

    protected HTTPHeaders headers;
    protected boolean initialized;
    private int headerMaxNo;
    private int lineMaxNo;

    /**
     * Constructs a ChunkedInputStream with the given underlying stream, and
     * a headers container to which the stream's trailing headers will be
     * added.
     *
     * @param in the underlying "chunked"-encoded input stream
     * @param headers the headers container to which the stream's trailing
     *        headers will be added, or null if they are to be discarded
     * @throws NullPointerException if the given stream is null
     */
    public ChunkedInputStream(InputStream in, HTTPHeaders headers,int lineMaxNo, int headerMaxNo) {
        super(in, 0, true);
        this.headers = headers;
        this.headerMaxNo = headerMaxNo;
        this.lineMaxNo = lineMaxNo;
    }

    @Override
    public int read() throws IOException {
        return limit <= 0 && initChunk() < 0 ? -1 : super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return limit <= 0 && initChunk() < 0 ? -1 : super.read(b, off, len);
    }

    /**
     * Initializes the next chunk. If the previous chunk has not yet
     * ended, or the end of stream has been reached, does nothing.
     *
     * @return the length of the chunk, or -1 if the end of stream
     *         has been reached
     * @throws IOException if an IO error occurs or the stream is corrupt
     */
    protected long initChunk() throws IOException {
        if (limit == 0) { // finished previous chunk
            // read chunk-terminating CRLF if it's not the first chunk
            if (initialized) {
                if (readLine(in).length() > 0)
                    throw new IOException("chunk data must end with CRLF");
            }
            initialized = true;
            limit = parseChunkSize(readLine(in)); // read next chunk size
            if (limit == 0) { // last chunk has size 0
                limit = -1; // mark end of stream
                // read trailing headers, if any
                HTTPHeaders trailingHeaders = HttpRequestParser.consumeHeaders(in,lineMaxNo,headerMaxNo);
                if (headers != null){
                    Map<String,String> headerMap = trailingHeaders.getAllHeadersMap();
                    String trailer = headers.getHeader(TRAILER);
                    if (trailer!=null)
                        headerMap.keySet().stream().
                                filter(trailer::contains).
                                forEach(h->headers.appendHeader(h,headerMap.get(h)));
                }
            }
        }
        return limit;
    }

    /**
     * Parses a chunk-size line.
     *
     * @param line the chunk-size line to parse
     * @return the chunk size
     * @throws IllegalArgumentException if the chunk-size line is invalid
     */
    protected static long parseChunkSize(String line) throws IllegalArgumentException {
        int pos = line.indexOf(';');
        if (pos > -1)
            line = line.substring(0, pos); // ignore params, if any
        try {
            return Long.parseLong(line, 16); // throws NFE
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    "invalid chunk size line: \"" + line + "\"");
        }
    }
}

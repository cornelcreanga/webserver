package com.ccreanga.webserver.ioutil;


import java.io.FilterOutputStream;
import java.io.OutputStream;


/**
 * This class should wrap the output stream if chunked transmission is used. Not yet implemented
 */
public class ChunkedOutputStream extends FilterOutputStream {

    public ChunkedOutputStream(OutputStream out) {
        super(out);
    }
}

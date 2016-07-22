package com.ccreanga.webserver.util;


import java.io.FilterOutputStream;
import java.io.OutputStream;


/**
 * This class should wrap the output stream if chuncked transmission is used. Not yet implemented, not sure if needed taking into
 * account that we only deliver static content
 */
public class ChunkedOutputStream extends FilterOutputStream{

    public ChunkedOutputStream(OutputStream out) {
        super(out);
    }
}

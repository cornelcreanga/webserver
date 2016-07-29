package com.ccreanga.webserver.ioutil;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * This class should wrap the input stream if chuncked transmission is used. Not yet implemented
 */
public class ChunkedInputStream extends FilterInputStream {

    public ChunkedInputStream(InputStream in) {
        super(in);
    }


}

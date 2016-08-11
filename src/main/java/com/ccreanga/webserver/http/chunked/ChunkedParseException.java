package com.ccreanga.webserver.http.chunked;

public class ChunkedParseException extends RuntimeException {
    public ChunkedParseException(String message) {
        super(message);
    }
}

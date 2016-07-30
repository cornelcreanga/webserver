package com.ccreanga.webserver;

public class InternalException extends RuntimeException {

    public InternalException(Throwable cause) {
        super(cause);
    }

    public InternalException(String message) {
        super(message);
    }
}

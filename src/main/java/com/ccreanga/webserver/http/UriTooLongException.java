package com.ccreanga.webserver.http;

public class UriTooLongException extends RuntimeException {

    public UriTooLongException(String message) {
        super(message);
    }
}

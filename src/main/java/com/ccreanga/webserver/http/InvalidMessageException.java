package com.ccreanga.webserver.http;

/**
 * Thrown when a request cannot be parsed
 */
public class InvalidMessageException extends RuntimeException {

    private HttpStatus status;

    public InvalidMessageException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

package com.ccreanga.webserver.http;

/**
 * Thrown when a request cannot be parsed
 */
public class InvalidMessageException extends RuntimeException {

    private HTTPStatus status;

    public InvalidMessageException(String message,HTTPStatus status) {
        super(message);
        this.status = status;
    }

    public HTTPStatus getStatus() {
        return status;
    }
}

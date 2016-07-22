package com.ccreanga.webserver;

/**
 * Thrown when a request cannot be parsed
 */
public class InvalidMessageFormat extends RuntimeException {

    public InvalidMessageFormat() {
    }

    public InvalidMessageFormat(String message) {
        super(message);
    }
}

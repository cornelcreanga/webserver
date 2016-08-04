package com.ccreanga.webserver;

/**
 * Thrown when a request cannot be parsed
 */
public class InvalidMessageFormatException extends RuntimeException {

    public InvalidMessageFormatException(String message) {
        super(message);
    }
}

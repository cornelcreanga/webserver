package com.ccreanga.webserver.ioutil;

public class LineTooLongException extends RuntimeException {

    public LineTooLongException(String message) {
        super(message);
    }
}

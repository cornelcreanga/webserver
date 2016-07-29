package com.ccreanga.webserver.http;

public enum HTTPVersion {

    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");

    private final String version;

    HTTPVersion(String version) {
        this.version = version;
    }

    public static HTTPVersion from(String version) {
        for (HTTPVersion status : values()) {
            if (status.version.equals(version)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + version + "]");
    }

    @Override
    public String toString() {
        return version;
    }
}

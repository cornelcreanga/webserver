package com.ccreanga.webserver.http;

public class HttpRequestLine {

    private HTTPMethod method;
    private String uri;
    private HTTPVersion version;

    public HttpRequestLine(HTTPMethod method, String uri, HTTPVersion version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public HTTPVersion getVersion() {
        return version;
    }
}

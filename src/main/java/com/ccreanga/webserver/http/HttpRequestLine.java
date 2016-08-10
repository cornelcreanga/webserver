package com.ccreanga.webserver.http;

public class HttpRequestLine {

    private HttpMethod method;
    private String uri;
    private HttpVersion version;

    public HttpRequestLine(HttpMethod method, String uri, HttpVersion version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public HttpVersion getVersion() {
        return version;
    }
}

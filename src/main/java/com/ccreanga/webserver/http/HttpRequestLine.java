package com.ccreanga.webserver.http;

import java.util.Collections;
import java.util.Map;

public class HttpRequestLine {

    private HttpMethod method;
    private String uri;
    private Map<String, String> uriParams;
    private HttpVersion version;

    public HttpRequestLine(HttpMethod method, String uri, HttpVersion version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    public HttpRequestLine(HttpMethod method, String uri, Map<String, String> uriParams, HttpVersion version) {
        this.method = method;
        this.uri = uri;
        this.version = version;
        this.uriParams = Collections.unmodifiableMap(uriParams);
    }

    public Map<String, String> getUriParams() {
        return uriParams;
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

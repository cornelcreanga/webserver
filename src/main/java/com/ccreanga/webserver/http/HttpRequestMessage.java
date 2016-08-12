package com.ccreanga.webserver.http;

import java.io.InputStream;

/**
 * Encapsulates an HTTP request.
 */
public class HttpRequestMessage {



    private HttpMethod method;
    private HttpHeaders headers;
    private String uri;
    private HttpVersion version;
    private InputStream body;//it makes sense only for put/post request. it can be too large to be kept in the RAM
    private long length;//body length; makes sense only when chunk=false; -1 otherwise
    private boolean chunked;

    public HttpRequestMessage(HttpRequestLine line, HttpHeaders headers, InputStream body, long length,boolean chunked) {
        this.method = line.getMethod();
        this.headers = headers;
        this.uri = line.getUri();
        this.version = line.getVersion();
        this.body = body;
        this.length = length;
        this.chunked = chunked;
    }

    public boolean isChunked() {
        return chunked;
    }

    public boolean isHTTP1_1() {
        return getVersion().equals(HttpVersion.HTTP_1_1);
    }

    public boolean isHTTP1_0() {
        return getVersion().equals(HttpVersion.HTTP_1_0);
    }

    public boolean headerContains(String header, String value) {
        String headerValue = getHeader(header);
        if (headerValue == null)
            return false;
        return headerValue.contains(value);
    }

    public boolean headerIs(String header, String value) {
        String headerValue = getHeader(header);
        if (headerValue == null)
            return false;
        return headerValue.equalsIgnoreCase(value);
    }

    public long getLength() {
        return length;
    }

    public InputStream getBody() {
        return body;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getHeader(String header) {
        return headers.getHeader(header);
    }

    public String getUri() {
        return uri;
    }

}

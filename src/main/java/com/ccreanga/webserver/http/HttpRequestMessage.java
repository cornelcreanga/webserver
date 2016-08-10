package com.ccreanga.webserver.http;

import com.ccreanga.webserver.ioutil.ChunkedInputStream;

import java.io.InputStream;

/**
 * Encapsulates an HTTP request.
 */
public class HttpRequestMessage {



    private HTTPMethod method;
    private HTTPHeaders headers;
    private String uri;
    private HTTPVersion version;
    protected InputStream body;//it makes sense only for put/post request. it can be too large to be kept in the RAM
    protected long length;//body length; makes sense only when chunk=false; -1 otherwise

    public HttpRequestMessage(HttpRequestLine line,HTTPHeaders headers, InputStream body, long length) {
        this.method = line.getMethod();
        this.headers = headers;
        this.uri = line.getUri();
        this.version = line.getVersion();
        this.body = body;
        this.length = length;
    }

    public boolean isHTTP1_1() {
        return getVersion().equals(HTTPVersion.HTTP_1_1);
    }

    public boolean isHTTP1_0() {
        return getVersion().equals(HTTPVersion.HTTP_1_0);
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

    public HTTPVersion getVersion() {
        return version;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public String getHeader(String header) {
        return headers.getHeader(header);
    }

    public String getUri() {
        return uri;
    }

}

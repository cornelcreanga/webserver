package com.ccreanga.webserver.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates an HTTP request.
 */
public class HttpRequestMessage {

    private HttpMethod method;
    private HttpHeaders headers;
    private String uri;
    private Map<String, List<String>> params;
    private HttpVersion version;
    private InputStream body;//it makes sense only for put/post request. it can be too large to be kept in the RAM
    private long length;//body length; makes sense only when chunk=false; -1 otherwise
    private boolean chunked;

    public HttpRequestMessage(HttpRequestLine line, HttpHeaders headers,Map<String, List<String>> bodyParams, InputStream body, long length, boolean chunked) {
        this.method = line.getMethod();
        this.headers = headers;
        this.uri = line.getUri();
        this.params = new HashMap<>(bodyParams);
        line.getUriParams().forEach(
                (k,v)->{
                    if (params.containsKey(k))
                        params.get(k).add(v);
                    else {
                        List<String> list = new ArrayList<>();
                        list.add(v);
                        params.put(k,list);
                    }
                });

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

    public boolean headerContainsValue(String header, String value) {
        String headerValue = getHeader(header);
        return headerValue != null && headerValue.contains(value);
    }

    public boolean headerIsEqualWithValue(String header, String value) {
        String headerValue = getHeader(header);
        return headerValue != null && headerValue.equalsIgnoreCase(value);
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

    public boolean hasHeader(String header) {
        return headers.hasHeader(header);
    }

    public String getHeader(String header) {
        return headers.getHeader(header);
    }

    public HttpHeaders getHeaders(){
        return headers;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, List<String>> getAllParams() {
        return params;
    }
}

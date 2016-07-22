package com.ccreanga.webserver;

import com.adobe.webserver.util.ChunkedInputStream;
import com.ccreanga.webserver.util.ChunkedInputStream;

import java.io.InputStream;

/**
 * Encapsulates an HTTP request.
 */
public class RequestMessage {

    private static final String HTTP1_1 = "HTTP/1.1";

    private HTTPMethod method;
    private Headers headers;
    private String resource;
    private String version;//for the moment it can only 1.1
    protected InputStream body;//it makes sense only for put/post request. it can be too large to be kept in the RAM
    protected boolean chunk;//specifies if the body will be sent using chunked transmission
    protected long length;//body length; makes sense only when chunk=false

    public RequestMessage(HTTPMethod method, Headers headers, String resource, String version, InputStream body,boolean chunk,long bodyLength) {
        this.method = method;
        this.headers = headers;
        this.resource = resource;
        this.version = version;
        if (chunk)
            this.body = new ChunkedInputStream(body);
        else
            this.body = body;
    }

    public boolean isChunk() {
        return chunk;
    }

    public long getLength() {
        return length;
    }

    public boolean isHTTP1_1() {
        return HTTP1_1.equals(version);
    }

    public InputStream getBody() {
        return body;
    }

    public String getVersion() {
        return version;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public String getHeader(String header) {
        return headers.getHeader(header);
    }

    public String getResource() {
        return resource;
    }
}

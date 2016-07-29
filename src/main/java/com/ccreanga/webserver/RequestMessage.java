package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPMethod;
import com.ccreanga.webserver.http.HTTPVersion;
import com.ccreanga.webserver.ioutil.ChunkedInputStream;

import java.io.InputStream;

/**
 * Encapsulates an HTTP request.
 */
public class RequestMessage {

    private HTTPMethod method;
    private HTTPHeaders headers;
    private String uri;
    private HTTPVersion version;
    protected InputStream body;//it makes sense only for put/post request. it can be too large to be kept in the RAM
    protected boolean chunk;//specifies if the body will be sent using chunked transmission
    protected long length;//body length; makes sense only when chunk=false

    public RequestMessage(HTTPMethod method, HTTPHeaders headers, String uri, HTTPVersion version, InputStream body, boolean chunk, long bodyLength) {
        this.method = method;
        this.headers = headers;
        this.uri = uri;
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

    @Override
    public String toString() {
        return "RequestMessage{" +
                "method=" + method +
                ", headers=" + headers +
                ", uri='" + uri + '\'' +
                ", version='" + version + '\'' +
                ", body=" + body +
                ", chunk=" + chunk +
                ", length=" + length +
                '}';
    }
}

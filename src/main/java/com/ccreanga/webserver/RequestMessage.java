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
    protected boolean chunked;//specifies if the body will be sent using chunked transmission
    protected long length;//body length; makes sense only when chunk=false

    public RequestMessage(HTTPMethod method, HTTPHeaders headers, String uri, HTTPVersion version, InputStream body, boolean chunked, long length) {
        this.method = method;
        this.headers = headers;
        this.uri = uri;
        this.version = version;
        if (chunked)//not yet implemented
            this.body = new ChunkedInputStream(body);
        else
            this.body = body;
        this.length = length;
    }

    public boolean isHTTP1_1(){
        return getVersion().equals(HTTPVersion.HTTP_1_1);
    }

    public boolean isHTTP1_0(){
        return getVersion().equals(HTTPVersion.HTTP_1_0);
    }

    public boolean headerContains(String header,String value){
        String headerValue = getHeader(header);
        if (headerValue==null)
            return false;
        return headerValue.contains(value);
    }

    public boolean headerIs(String header,String value){
        String headerValue = getHeader(header);
        if (headerValue==null)
            return false;
        return headerValue.equals(value);
    }


    public boolean isChunked() {
        return chunked;
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
                ", chunk=" + chunked +
                ", length=" + length +
                '}';
    }
}

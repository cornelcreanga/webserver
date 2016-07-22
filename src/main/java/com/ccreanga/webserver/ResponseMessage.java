package com.ccreanga.webserver;


import com.adobe.webserver.util.DateUtil;
import com.ccreanga.webserver.util.DateUtil;

import java.util.Date;

public class ResponseMessage {

    private HttpStatus status;
    private Headers headers = new Headers();
    private String resourceFullPath;
    private boolean ignoreBody = false;
    //if it's zero (dynamic resource) we will use chunked transmission
    private long resourceLength;

    public ResponseMessage(HttpStatus status) {
        this.status = status;
        headers.putHeader(Headers.date, new DateUtil().formatDate(new Date()) + " GMT");
        headers.putHeader(Headers.connection,"keep-alive");
    }

    public long getResourceLength() {
        return resourceLength;
    }

    public boolean isIgnoreBody() {
        return ignoreBody;
    }

    public void setIgnoreBody(boolean ignoreBody) {
        this.ignoreBody = ignoreBody;
    }

    public void setResourceLength(long resourceLength) {
        this.resourceLength = resourceLength;
    }

    public Headers getHeaders() {
        return headers;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getHeader(String header) {
        return headers.getHeader(header);
    }

    public void setHeader(String header, String value) {
        headers.putHeader(header, value);
    }

    public String getResourceFullPath() {
        return resourceFullPath;
    }

    public void setResourceFullPath(String resourceFullPath) {
        this.resourceFullPath = resourceFullPath;
    }
}

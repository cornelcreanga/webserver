package com.ccreanga.webserver;


import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.util.DateUtil;

import java.util.Date;

public class ResponseMessage {

    private HttpStatus status;
    private HTTPHeaders headers = new HTTPHeaders();
    private String resourceFullPath;
    private boolean ignoreBody = false;
    //if it's zero (dynamic resource) we will use chunked transmission
    private long resourceLength;

    public ResponseMessage(HttpStatus status) {
        this.status = status;
        headers.putHeader(HTTPHeaders.date, new DateUtil().formatDate(new Date()) + " GMT");
        headers.putHeader(HTTPHeaders.connection,"keep-alive");
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

    public HTTPHeaders getHeaders() {
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

package com.ccreanga.webserver;


import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.util.DateUtil;

import java.time.LocalDateTime;
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
        String currentDate = DateUtil.currentDate();
        ContextHolder.get().setDate(currentDate);
        headers.putHeader(HTTPHeaders.DATE, currentDate + " GMT");
        headers.putHeader(HTTPHeaders.CONNECTION,"keep-alive");
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

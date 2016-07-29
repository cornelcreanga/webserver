package com.ccreanga.webserver.logging;

import java.util.UUID;

public class Context {
    private UUID uuid;
    private String ip;
    private String user;
    private String date;
    private String url;
    private String statusCode;
    private String contentLength;

    public Context() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    public String generateLogEntry(){
        StringBuilder sb = new StringBuilder(128);
        sb.append(ip).append('\t');
        sb.append(user==null?'-':user).append('\t');
        sb.append(date).append('\t');
        sb.append(url).append('\t');
        sb.append(statusCode).append('\t');
        sb.append(contentLength).append('\r');
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Context{" +
                "uuid=" + uuid +
                ", ip='" + ip + '\'' +
                ", user='" + user + '\'' +
                ", date='" + date + '\'' +
                ", url='" + url + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", contentLength='" + contentLength + '\'' +
                '}';
    }
}

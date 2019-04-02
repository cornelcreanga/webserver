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

    public Context setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public Context setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getUser() {
        return user;
    }

    public Context setUser(String user) {
        this.user = user;
        return this;
    }

    public String getDate() {
        return date;
    }

    public Context setDate(String date) {
        this.date = date;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Context setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public Context setStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getContentLength() {
        return contentLength;
    }

    public Context setContentLength(String contentLength) {
        this.contentLength = contentLength;
        return this;
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

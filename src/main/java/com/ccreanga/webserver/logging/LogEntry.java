package com.ccreanga.webserver.logging;

public class LogEntry {

    public static String generateLogEntry(Context context){
        return generateLogEntry(context.getIp(),context.getUser(),context.getDate(),context.getUrl(),context.getStatusCode(),context.getContentLength());
    }

    public static String generateLogEntry(String ip,String user,String date,String url,String statusCode,String contentLength) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(ip).append('\t');
        sb.append(user == null ? '-' : user).append('\t');
        sb.append(date).append('\t');
        sb.append(url).append('\t');
        sb.append(statusCode).append('\t');
        sb.append(contentLength).append('\r');
        return sb.toString();
    }
}

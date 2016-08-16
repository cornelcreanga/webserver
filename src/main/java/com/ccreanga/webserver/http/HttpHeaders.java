package com.ccreanga.webserver.http;


import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {

    private HashMap<String, String> headers = new HashMap<>(8);

    public String getHeader(String header) {
        return headers.get(header);
    }

    public Map<String, String> getAllHeadersMap() {
        return ImmutableMap.copyOf(headers);
    }

    public HttpHeaders appendHeader(String header, String value) {
        String previousValue = headers.get(header);
        if (previousValue == null) {
            headers.put(header, value);
        } else {
            String actualValue = previousValue + "," + value;
            headers.put(header, actualValue);
        }
        return this;
    }

    public HttpHeaders putHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public void removeHeader(String header) {
        headers.remove(header);
    }

    public boolean hasHeader(String header){
        return headers.containsKey(header);
    }

    public boolean headerContains(String header,String value){
        String headerValue = headers.get(header);
        if (headerValue==null)
            return false;
        return headerValue.contains(value);
    }

    public int size(){return headers.size();}


    @Override
    public String toString() {
        return "HttpHeaders{" +
                "headers=" + headers +
                '}';
    }
}

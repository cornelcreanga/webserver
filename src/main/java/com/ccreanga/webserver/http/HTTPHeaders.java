package com.ccreanga.webserver.http;


import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class HTTPHeaders {

    private HashMap<String, String> headers = new HashMap<>(8);

    public String getHeader(String header) {
        return headers.get(header);
    }

    public Map<String, String> getAllHeadersMap() {
        return ImmutableMap.copyOf(headers);
    }

    public HTTPHeaders appendHeader(String header, String value) {
        String previousValue = headers.get(header);
        if (previousValue == null) {
            headers.put(header, value);
        } else {
            String actualValue = previousValue + "," + value;
            headers.put(header, actualValue);
        }
        return this;
    }

    public HTTPHeaders putHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }

    @Override
    public String toString() {
        return "HTTPHeaders{" +
                "headers=" + headers +
                '}';
    }
}

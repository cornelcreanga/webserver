package com.ccreanga.webserver.http;


import com.ccreanga.webserver.ParseUtil;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    public boolean hasHeader(String header) {
        return headers.containsKey(header);
    }

    public Map<String, String> getHeaderParams(String header) {
        Map<String, String> headerParams = new LinkedHashMap<String, String>();
        String headerValue = headers.get(header);
        List<String> params = ParseUtil.split(headerValue, ';',false,100);
        for (String param : params) {
            String key = ParseUtil.left(param,'=');
            String value = ParseUtil.right(param,'=');
            if (value.length()>0){
                if (value.startsWith("\""))
                    value = value.substring(1);
                if (value.endsWith("\""))
                    value = value.substring(0,value.length()-1);
            }

            headerParams.put(key.trim(), value);
        }
        return headerParams;
    }

    public int size() {
        return headers.size();
    }


    @Override
    public String toString() {
        return "HttpHeaders{" +
                "headers=" + headers +
                '}';
    }
}

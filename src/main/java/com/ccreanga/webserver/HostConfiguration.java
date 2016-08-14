package com.ccreanga.webserver;

public class HostConfiguration {

    private String name;
    private String rootFolder;
    private String methodLine;

    public HostConfiguration(String name, String methodLine, String rootFolder) {
        this.name = name;
        this.methodLine = methodLine;
        this.rootFolder = rootFolder;
    }

    public String getName() {
        return name;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public String getMethodLine() {
        return methodLine;
    }
}

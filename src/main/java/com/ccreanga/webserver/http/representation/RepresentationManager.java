package com.ccreanga.webserver.http.representation;

public class RepresentationManager {

    private static final RepresentationManager instance = new RepresentationManager();

    public static RepresentationManager getInstance() {
        return instance;
    }

    public FileResourceRepresentation getRepresentation(String acceptHeader) {
        if (acceptHeader.contains("*/*"))
            return new HtmlResourceRepresentation();
        if (acceptHeader.contains("text/html"))
            return new HtmlResourceRepresentation();
        if (acceptHeader.contains("application/json"))
            return new JsonResourceRepresentation();
        return new HtmlResourceRepresentation();//todo - should we throw an error?

    }

}

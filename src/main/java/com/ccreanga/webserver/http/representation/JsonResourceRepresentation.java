package com.ccreanga.webserver.http.representation;

import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.Mime;

import java.io.File;
import java.io.IOException;

public class JsonResourceRepresentation implements FileResourceRepresentation {
    @Override
    public String folderRepresentation(File folder, File root) throws IOException {
        return null;
    }

    @Override
    public String errorRepresentation(HTTPStatus status, String extendedReason) throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return Mime.getType("json");
    }

}

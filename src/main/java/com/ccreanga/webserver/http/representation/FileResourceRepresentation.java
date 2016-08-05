package com.ccreanga.webserver.http.representation;

import com.ccreanga.webserver.http.HTTPStatus;

import java.io.File;
import java.io.IOException;

public interface FileResourceRepresentation {

    String folderRepresentation(File folder, File root) throws IOException;

    String errorRepresentation(HTTPStatus status, String extendedReason) throws IOException;

    String getContentType();
}

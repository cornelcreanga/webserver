package com.ccreanga.webserver.filehandler.representation;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileResourceRepresentation {

    String folderRepresentation(File folder, File root) throws IOException;

    String errorRepresentation(HttpStatus status, String extendedReason) throws IOException;

    String nonDeletedFiles(List<File> files);

    String getContentType();

    String getFileInfo(File file, Configuration cfg, boolean extended) throws IOException;
}

package com.ccreanga.webserver.filehandler.representation;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.repository.FileManager;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Used to generate an json representation for a folder or in case of an error (like Apache does).
 */
public class JsonResourceRepresentation implements FileResourceRepresentation {

    @Override
    public String folderRepresentation(File folder, File root) throws IOException {

        StringBuilder sb = new StringBuilder(1024);
        sb.append("{").append("\"files\": [");


        FileManager.getInstance().getFolderContent(folder).
                forEach(file -> {
                    String name = file.getName().replaceAll("\\\\", "\\\\\\\\");
                    name = name.replaceAll("\\\"", "\\\\\"");
                    String relative = root.toURI().relativize(file.toURI()).getPath();

                    sb.append("{");
                    sb.append("\"name\":").append(name).append(",");
                    sb.append("\"link\":").append(relative).append(",");
                    sb.append("\"lastModified\":").append(DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT)).append(",");
                    sb.append("\"size\":").append(file.isDirectory() ? "" : "" + file.length()).append(",");
                    sb.append("\"type\":").append(file.isDirectory() ? "folder" : "file");
                    sb.append("}");
                });

        sb.append("]}");

        return sb.toString();
    }

    @Override
    public String errorRepresentation(HttpStatus status, String extendedReason) throws IOException {
        return "{\"extendedReason\":\"" + extendedReason + "\"}";
    }

    @Override
    public String nonDeletedFiles(List<File> files) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("{").append("\"files\": [");
        for (File file : files) {
            String name = file.getName().replaceAll("\\\\", "\\\\\\\\");
            name = name.replaceAll("\\\"", "\\\\\"");
            sb.append("{");
            sb.append("\"name\":").append(name).append(",");
            sb.append("\"lastModified\":").append(DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT)).append(",");
            sb.append("\"size\":").append(file.isDirectory() ? "" : "" + file.length()).append(",");
            sb.append("\"type\":").append(file.isDirectory() ? "folder" : "file");
            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    @Override
    public String getContentType() {
        return Mime.getType("json");
    }

    @Override
    public String getFileInfo(File file, Configuration cfg, boolean extended) {
        return null;//todo
    }


}

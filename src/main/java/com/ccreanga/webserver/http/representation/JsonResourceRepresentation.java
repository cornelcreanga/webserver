package com.ccreanga.webserver.http.representation;

import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.repository.FileManager;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;

/**
 * Used to generate an json representation for a folder or in case of an error (like Apache does).
 */
public class JsonResourceRepresentation implements FileResourceRepresentation {

    private static Template index;

    static {
        Configuration cfg = new Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "templates");
        try {
            index = cfg.getTemplate("index_json.ftl");
        } catch (IOException e) {
            throw new InternalException("i/o error, cannot load templates");
        }

    }

    @Override
    public String folderRepresentation(File folder, File root) throws IOException {

        StringWriter writer = new StringWriter();
        Map<String, Object> data = new HashMap<>(2);

        List<Map<String, String>> files = FileManager.getInstance().getFolderContent(folder).
                stream().
                map(file -> {
                    Map<String, String> map = new HashMap<>(4);
                    String name = file.getName().replaceAll("\\\\", "\\\\\\\\");
                    name = name.replaceAll("\\\"", "\\\\\"");
                    map.put("name", name);
                    String relative = root.toURI().relativize(file.toURI()).getPath();
                    map.put("link", relative);
                    map.put("lastModified", "" + DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT));
                    map.put("size", file.isDirectory() ? "" : "" + file.length());
                    map.put("type", file.isDirectory() ? "folder" : "file");
                    return map;
                }).collect(Collectors.toList());

        data.put("folderFiles", files);
        try {
            index.process(data, writer);
        } catch (TemplateException e) {
            //will not appear unless a the template is invalid
            throw new InternalException(e);
        }

        return writer.toString();
    }

    @Override
    public String errorRepresentation(HttpStatus status, String extendedReason) throws IOException {
        return "{\"extendedReason\":\"" + extendedReason + "\"}";
    }

    @Override
    public String getContentType() {
        return Mime.getType("json");
    }


}

package com.ccreanga.webserver.http.representation;

import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.repository.FileManager;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.common.net.UrlEscapers;
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
import java.util.Stack;
import java.util.stream.Collectors;

import static com.ccreanga.webserver.formatters.NumberUtil.fileSizePretty;
import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;

public class HtmlResourceRepresentation implements FileResourceRepresentation {

    private static Template index;
    private static Template error;

    static{
        Configuration cfg = new Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "templates");
        try {
            index = cfg.getTemplate("index.ftl");
            error = cfg.getTemplate("error.ftl");
        } catch (IOException e) {
            throw new InternalException("i/o error, cannot load templates");
        }

    }

    @Override
    public String folderRepresentation(File folder, File root) throws IOException {
        StringWriter writer = new StringWriter();
        Map<String, Object> data = new HashMap<>(2);
        Escaper htmlEscaper = HtmlEscapers.htmlEscaper();
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();

        data.put("folder", htmlEscaper.escape(folder.getName()));
        if (!root.equals(folder)) {
            data.put("allowBrowsing", "true");
            data.put("parentFolder", extractParentResource(folder, root));
        }

        List<Map<String, String>> files = FileManager.getInstance().getFolderContent(folder).
                stream().
                sorted((f1, f2) -> {//first directories, after that the files
                    if ((f1.isDirectory()) && (f2.isFile()))
                        return -1;
                    if ((f1.isFile()) && (f2.isDirectory()))
                        return 1;
                    return f1.compareTo(f2);
                }).
                map(file -> {
                    Map<String, String> map = new HashMap<>(4);
                    map.put("name", htmlEscaper.escape(file.getName()) + (file.isDirectory() ? "/" : ""));
                    map.put("link", urlPathEscaper.escape(file.getName()) + (file.isDirectory() ? "/" : ""));
                    map.put("lastModified", "" + DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT));
                    map.put("size", file.isDirectory() ? "-" : fileSizePretty(file.length()));
                    map.put("type", file.isDirectory() ? "folder" : "file");
                    return map;
                }).collect(Collectors.toList());

        data.put("folderFiles", files);
        try {
            index.process(data, writer);
        } catch (TemplateException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw e;
        }

        return writer.toString();
    }

    @Override
    public String errorRepresentation(HTTPStatus status, String extendedReason) throws IOException {
        Map<String, Object> data = new HashMap<>(3);
        StringWriter writer = new StringWriter();
        data.put("statusCode", status.value());
        data.put("statusReason", status.getReasonPhrase());
        data.put("extendedReason", extendedReason);
        try {
            error.process(data, writer);
        } catch (TemplateException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw e;
        }
        return writer.toString();
    }

    @Override
    public String getContentType() {
        return Mime.getType("html");
    }

    private String extractParentResource(File file, File root){
        File traverse = file.getParentFile();
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        Stack<String> stack = new Stack<>();
        while(!traverse.equals(root)){
            stack.push(traverse.getName());
            traverse = traverse.getParentFile();
        }
        while(!stack.empty()){
            String next = stack.pop();
            sb.append(next).append("/");
        }
        return sb.toString();
    }

}

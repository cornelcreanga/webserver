package com.ccreanga.webserver;

import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HTTPStatus;
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
import java.io.Writer;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ccreanga.webserver.formatters.NumberUtil.fileSizePretty;
import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;

public class TemplateRepository {

    private Template index;
    private Template error;

    private static TemplateRepository instance = new TemplateRepository();

    private TemplateRepository() {
        Configuration cfg = new Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "templates");
        try {
            index = cfg.getTemplate("index.ftl");
            error = cfg.getTemplate("error.ftl");
        } catch (IOException e) {
            throw new InternalException("i/o error, cannot load templates");
            //todo
        }
    }

    public static TemplateRepository instance() {
        return instance;
    }

    public String buildIndex(File folder, String root) throws IOException {
        StringWriter writer = new StringWriter();
        buildIndex(folder, writer, root);
        return writer.toString();
    }


    public void buildIndex(File folder, Writer writer, String root) throws IOException {

        //todo - the accept header should be checked in order to know if we can deliver html/something else

        if (!folder.isDirectory())
            throw new InternalException("internal error, file:" + folder.getName() + " is not a folder");
        Map<String, Object> data = new HashMap<>(2);
        Escaper htmlEscaper = HtmlEscapers.htmlEscaper();
        Escaper urlPathEscaper = UrlEscapers.urlPathSegmentEscaper();
        data.put("folder", htmlEscaper.escape(folder.getName()));
        if (!root.equals(folder.getAbsolutePath())) {
            data.put("allowBrowsing", "true");
            //todo - windows
            data.put("parentFolder", folder.getParent().substring(root.length()) + "/");
        }


        List<Map<String, String>> files = FileManager.getInstance().getFolderContent(folder).
                stream().
                sorted((f1, f2) -> {
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
                    map.put("lastModified", "" + DateUtil.formatDate(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT));
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
    }

    public String buildError(HTTPStatus status, String extendedReason) throws IOException {
        StringWriter writer = new StringWriter();
        buildError(status, extendedReason, writer);
        return writer.toString();
    }


    public void buildError(HTTPStatus status, String extendedReason, Writer writer) throws IOException {
        Map<String, Object> data = new HashMap<>(3);
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
    }

}

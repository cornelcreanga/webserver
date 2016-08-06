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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.ccreanga.webserver.formatters.NumberUtil.fileSizePretty;
import static com.ccreanga.webserver.ioutil.IOUtil.extractParentResource;
import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;

/**
 * Used to generate an html representation for a folder or in case of an error (like Apache does).
 */
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
                    map.put("link", encodeUrl(file.getName()) + (file.isDirectory() ?  "/" : ""));
                    map.put("lastModified", "" + DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT));
                    map.put("size", file.isDirectory() ? "-" : fileSizePretty(file.length()));
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

    private String encodeUrl(String url){
        try {
            return URLEncoder.encode(url,StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {throw new InternalException(e);}
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


}

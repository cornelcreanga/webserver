package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HttpStatus;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;

public class TemplateRepository {

    private Template index;
    private Template error;

    private static TemplateRepository instance = new TemplateRepository();

    private TemplateRepository() {
        Configuration cfg = new Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(),"templates");
        try {
            index = cfg.getTemplate("index.ftl");
            error = cfg.getTemplate("error.ftl");
        } catch (IOException e) {
            throw new InternalException("i/o error, cannot load templates");
            //todo
        }
    }

    public static TemplateRepository instance(){
        return instance;
    }

    public String buildIndex(String folderName, File[] content) throws IOException {
        StringWriter writer = new StringWriter();
        buildIndex(folderName,content,writer);
        return writer.toString();
    }

    public void buildIndex(String folderName, File[] content, Writer writer) throws IOException {
        Map<String,Object> data = new HashMap<>(2);
        data.put("parentFolder",folderName);

        List<Map<String,String>> files = Arrays.stream(content).map(file -> {
            Map<String,String> map = new HashMap<>(4);
            map.put("name",file.getName());
            map.put("lastModified",""+file.lastModified());
            map.put("size",""+file.length());
            map.put("type",file.isDirectory()?"folder":"file");
            return map;
        }).collect(Collectors.toList());

        data.put("folderFiles",files);
        try {
            index.process(data, writer);
        } catch (TemplateException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw e;
        }
    }

    public String buildError(HttpStatus status,String extendedReason) throws IOException {
        StringWriter writer = new StringWriter();
        buildError(status,extendedReason,writer);
        return writer.toString();
    }


    public void buildError(HttpStatus status,String extendedReason, Writer writer) throws IOException {
        Map<String,Object> data = new HashMap<>(3);
        data.put("statusCode",status.value());
        data.put("statusReason",status.getReasonPhrase());
        data.put("extendedReason",extendedReason);
        try {
            error.process(data, writer);
        } catch (TemplateException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw e;
        }
    }

}

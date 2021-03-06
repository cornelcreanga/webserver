package com.ccreanga.webserver.http;

import com.ccreanga.webserver.InternalException;

import java.util.Properties;

/**
 * Map of mime types
 */
public class Mime {

    private static Properties properties = new Properties();
    private static Properties reverted = new Properties();
    private static String applicationStream = "application/octet-stream";

    public static final String MIME_PROPERTIES = "mime.properties";
    public static final String REVERTED_PROPERTIES = "reverted.properties";


    static {
        try {
            properties.load(ClassLoader.getSystemResourceAsStream(MIME_PROPERTIES));
        } catch (Exception e) {
            throw new InternalException("cannot load the " + MIME_PROPERTIES + " file (corrupted archive)");
        }
        try {
            reverted.load(ClassLoader.getSystemResourceAsStream(REVERTED_PROPERTIES));
        } catch (Exception e) {
            throw new InternalException("cannot load the " + REVERTED_PROPERTIES + " file (corrupted archive)");
        }

    }

    public static String getType(String extension) {
        if (extension == null)
            return applicationStream;
        String type = (String) properties.get(extension);
        if (type == null)
            type = applicationStream;
        return type;
    }

    public static String getExtension(String type) {
        return (String) reverted.get(type);
    }

}

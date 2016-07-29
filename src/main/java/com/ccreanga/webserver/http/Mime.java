package com.ccreanga.webserver.http;


import com.ccreanga.webserver.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Map of mime types
 */
public class Mime {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);
    private static Properties properties = new Properties();
    private static String applicationStream = "application/octet-stream";

    public static final String MIME_PROPERTIES = "mime.properties";

    static {
        try {
            properties.load(ClassLoader.getSystemResourceAsStream(MIME_PROPERTIES));
        } catch (Exception e) {
            log.error("cannot load the "+MIME_PROPERTIES+" file (corrupted archive)");
            System.exit(-1);
        }
    }

    public static String getType(String extension) {
        if (extension==null)
            return applicationStream;
        String type = (String) properties.get(extension);
        if (type == null)
            type = applicationStream;
        return type;
    }
}

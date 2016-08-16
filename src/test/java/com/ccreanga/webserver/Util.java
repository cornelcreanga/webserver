package com.ccreanga.webserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

    public static String extension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return (i > 0) ? fileName.substring(i + 1) : "";
    }

    public static String readAsUtfString(String file) throws IOException {
        return new String(readFromClasspath(file), "UTF-8");
    }

    public static String readAsUtfString(InputStream in) throws IOException {
        return new String(readAsByte(in), "UTF-8");
    }

    public static byte[] readAsByte(InputStream in) throws IOException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    public static byte[] readFromClasspath(String file) throws IOException {
        try (InputStream in = ClassLoader.getSystemResourceAsStream(file)) {
            return readAsByte(in);
        }
    }

}

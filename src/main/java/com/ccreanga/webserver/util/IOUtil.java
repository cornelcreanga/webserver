package com.ccreanga.webserver.util;


import com.ccreanga.webserver.InternalException;

import java.io.*;
import java.net.Socket;

/**
 * Utility static methods
 */
public class IOUtil {

    public static byte[] ascii(String s) {
        return convert(s, "ISO8859_1");
    }

    public static byte[] utf(String s) {
        return convert(s, "UTF-8");
    }

    public static void inputToOutput(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int length;
        while ((length = in.read(buf)) > 0)
            out.write(buf, 0, length);
    }

    private static byte[] convert(String s, String encoding) {
        if (s == null)
            throw new IllegalArgumentException("null parameter");

        try {
            return s.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalException("unsupported encoding:"+encoding);
        }
    }

    public static void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException("cannot close socket", e);
            }
        }
    }

    public static void close(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException("cannot close output stream", e);
            }
        }
    }

    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException("cannot close output stream", e);
            }
        }
    }


    public static void close(Writer out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException("cannot close output stream", e);
            }
        }
    }

    public static String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index == -1)
            return null;
        return filename.substring(index + 1);

    }

}

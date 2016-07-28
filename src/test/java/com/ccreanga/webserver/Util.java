package com.ccreanga.webserver;

import com.google.common.html.HtmlEscapers;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Util {

    public static String extension(String fileName){
        int i = fileName.lastIndexOf('.');
        return (i > 0)? fileName.substring(i+1):"";
    }

    public static String readAsUtfString(String file) throws IOException {
        return new String(readFromClasspath(file),"UTF-8");
    }

    public static String readAsUtfString(InputStream in) throws IOException {
        return new String(readAsByte(in),"UTF-8");
    }

    public static byte[] readAsByte(InputStream in) throws IOException {

        try(ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    public static byte[] readFromClasspath(String file) throws IOException {
        try(InputStream in=ClassLoader.getSystemResourceAsStream(file)){
            return readAsByte(in);
        }
    }

    public static void main(String[] args) throws Exception {
        String s = "சுப்ரமணிய";
        String encoded = URLEncoder.encode(s,"UTF-8");
        System.out.println(encoded);
        System.out.println(URLDecoder.decode(s,"UTF-8"));
        System.out.println(URLDecoder.decode(s,"ISO8859_1"));
        System.out.println(URLEncoder.encode("<>","UTF-8"));
        HtmlEscapers.htmlEscaper().escape("<>");


        s = "/%E0%AE%9A%E0%AF%81%E0%AE%AA%E0%AF%8D%E0%AE%B0%E0%AE%AE%E0%AE%A3%E0%AE%BF%E0%AE%AF/";
        System.out.println(URLDecoder.decode(s,"UTF-8"));
        System.out.println(".................");

        String filename = "中国 地图/? a.docx";
        String urlEncoding = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
        prettyPrint("java.net.URLEncoder", urlEncoding);
        prettyPrint("UrlEscapers.urlFormParameterEscaper", UrlEscapers.urlFormParameterEscaper().escape(filename));
        prettyPrint("java.net.URLEncoder then replace to %20", urlEncoding.replaceAll("\\+", "%20"));
        prettyPrint("UrlEscapers.urlPathSegmentEscaper", UrlEscapers.urlPathSegmentEscaper().escape(filename));
        prettyPrint("UrlEscapers.urlFragmentEscaper", UrlEscapers.urlFragmentEscaper().escape(filename));
    }
    static void prettyPrint(String label, String value) {
        System.out.printf("%-40s%-10s%s\n", label, "-->", value);
    }
}

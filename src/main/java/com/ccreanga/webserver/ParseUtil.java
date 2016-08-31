package com.ccreanga.webserver;


import com.ccreanga.webserver.ioutil.IOUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ParseUtil {

    public static long parseLong(String string, long min, long max) {
        long value = Long.parseLong(string);
        if ((value < min) || (value > max))
            throw new NumberFormatException("expecting a number between " + min + " and " + max + " instead of " + value);
        return value;
    }

    public static List<String> split(String str, char separatorChar, boolean preserveAllTokens, int maxNo) {
        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return Collections.emptyList();
        }
        final List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        int counter = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (++counter > maxNo)
                    throw new TooManyEntriesException();
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || preserveAllTokens && lastMatch) {
            if (++counter > maxNo)
                throw new TooManyEntriesException();
            list.add(str.substring(start, i));
        }
        return list;
    }

    public static String left(String s, char separator) {
        int index = s.indexOf(separator);
        return index == -1 ? s : s.substring(0, index);
    }

    public static String right(String s, char separator) {
        int index = s.indexOf(separator);
        return index == -1 ? "" : s.substring(index + 1);
    }

    public static Map<String, List<String>> parseFormEncodedParams(String form, int limit) throws TooManyEntriesException {
        List<String> elements = ParseUtil.split(form, '&', false, limit);
        return elements.stream().
                collect(
                        Collectors.groupingBy(
                                s -> IOUtil.decodeUTF8(ParseUtil.left(s, '=')),
                                Collectors.mapping(
                                        s -> IOUtil.decodeUTF8(ParseUtil.right(s, '=')),
                                        toList()
                                )
                        )
                );

    }
    public static String escapeHTML(String s) {
        StringBuilder sb = new StringBuilder((int) (s.length() * 1.1));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"')
                sb.append("&quot;");
            else if (c == '\\')
                sb.append("&#39;");
            else if (c == '&')
                sb.append("&amp;");
            else if (c == '<')
                sb.append("&lt;");
            else if (c == '>')
                sb.append("&gt;");
            else sb.append(c);
        }
        return sb.toString();

    }

    public static String escapeURLComponent(final String s) {
        StringBuilder sb = new StringBuilder((int) (s.length() * 1.1));

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || ((c >= '0') && (c <= '9')) ||
                    (c == '-') || (c == '.') || (c == '_') || (c == '~') || (c == '@') || (c == ':') ||
                    (c == '!') || (c == '$') || (c == '&') || (c == '\'') || (c == '(') || (c == ')') || (c == '*') || (c == '+') || (c == ',') || (c == ';') || (c == '=')
                    )
                sb.append(c);
            else if (c == ' ')
                sb.append("%20");
            else {
                byte[] bytes = ("" + c).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append('%');
                    int upper = (((int) b) >> 4) & 0xf;
                    sb.append(Integer.toHexString(upper).toUpperCase(Locale.US));
                    int lower = ((int) b) & 0xf;
                    sb.append(Integer.toHexString(lower).toUpperCase(Locale.US));
                }
            }
        }
        return sb.toString();
    }

}

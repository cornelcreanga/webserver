package com.ccreanga.webserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class TooManyEntriesException extends RuntimeException {

    public static void main(String[] args) {
        String form = "p_69342__action=send&p_69342__firstname=%CE%B3%CE%BB%CF%8E%CF%83%CF%83%CE%B1&p_69342__lastname=%26%26nume-%2B%3C%3E%21%3B%3F%3A%26&p_69342__subject=oferte+de+colaborare&p_69342__email=email&p_69342__message=mesaj&p_69342__send=Trimite%3A";
        form = "a=1&a=2&b=1&c&d=3";
        Map<String,List<String>> params = new HashMap<>();
        List<String> elements = ParseUtil.split(form, '&', false, 10000);
        params = elements.stream().
                collect(
                        Collectors.groupingBy(
                                s->ParseUtil.left(s,'='),
                                Collectors.mapping(
                                        s->ParseUtil.right(s,'='),
                                        toList()
                                )
                        )
                );
        System.out.println(params);

    }
}

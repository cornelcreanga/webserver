package com.ccreanga.webserver.util;


import java.time.*;
import java.time.format.DateTimeFormatter;


public class DateUtil {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss");

    public static LocalDateTime parseDate(String date) {
        try {
            return LocalDateTime.parse(date, formatter);
        }catch(DateTimeException exception){
            return null;
        }
    }

    public static String formatDate(Instant instant) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( instant , ZoneId.of("UTC") );
        return zonedDateTime.format(formatter);
    }

    public static String currentDate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( Instant.now() , ZoneId.of("UTC") );
        return zonedDateTime.format(formatter);
    }

}

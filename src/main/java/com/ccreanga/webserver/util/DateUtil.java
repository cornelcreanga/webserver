package com.ccreanga.webserver.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

public class DateUtil {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss");

//    private SimpleDateFormat date() {
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
//        sdf.setCalendar(Calendar.getInstance(new SimpleTimeZone(0, "GMT")));
//        return sdf;
//    }

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

    public static void main(String[] args) {
        LocalDateTime ldt = LocalDateTime.now();

        System.out.println(ldt.atOffset(ZoneOffset.UTC));

        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of("UTC"));

        ZonedDateTime ldt2 =  ldt.atZone(ZoneId.of("UTC"));
        //ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC); //you might use a different zone



        System.out.println(formatter.format(ldt));
        System.out.println(formatter.format(ldt2));

        System.out.println(zdt.format(formatter));

        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss");
        System.out.println(ZonedDateTime.now().format(FORMATTER));

        Instant instant = Instant.now();
        ZonedDateTime zdtutc = ZonedDateTime.ofInstant( instant , ZoneId.of("UTC") );
        System.out.println(zdtutc.format(FORMATTER));


    }

}

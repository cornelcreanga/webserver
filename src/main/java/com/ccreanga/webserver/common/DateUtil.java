package com.ccreanga.webserver.common;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;


public class DateUtil {

    public static DateTimeFormatter FORMATTER_RFC822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
    private static DateTimeFormatter FORMATTER_RFC850 = DateTimeFormatter.ofPattern("EEEE, dd-MMM-yy HH:mm:ss zzz");
    private static DateTimeFormatter FORMATTER_RFC850_YEAR_REDUCED = new DateTimeFormatterBuilder()
            .appendPattern("EEEE, dd-MMM-")
            .appendValueReduced(ChronoField.YEAR_OF_ERA, 2, 2, LocalDate.now().minusYears(100))
            .appendPattern(" HH:mm:ss zzz")
            .toFormatter();
    private static DateTimeFormatter FORMATTER_C_ASCTIME = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");
    private static DateTimeFormatter FORMATTER_C_ASCTIME_ONE_DAY_DIGIT = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy");
    public static DateTimeFormatter FORMATTER_LOG = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    public static DateTimeFormatter FORMATTER_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public static LocalDateTime parseRfc2161CompliantDate(String date) {
        return parseDate(date,
                FORMATTER_RFC822,
                FORMATTER_RFC850,
                FORMATTER_RFC850_YEAR_REDUCED,
                FORMATTER_C_ASCTIME,
                FORMATTER_C_ASCTIME_ONE_DAY_DIGIT);
    }

    private static LocalDateTime parseDate(String date, DateTimeFormatter... formatters) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(date, formatter);
            } catch (DateTimeException exception) {/**ignore**/}
        }
        return null;
    }

    public static String formatDateToUTC(Instant instant, DateTimeFormatter formatter) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        return zonedDateTime.format(formatter);
    }

    public static String formatDateToLocalZone(Instant instant, DateTimeFormatter formatter) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return zonedDateTime.format(formatter);
    }

    public static String currentDate(DateTimeFormatter formatter) {
        return formatDateToUTC(Instant.now(), formatter);
    }

}

package com.ccreanga.webserver.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

public class DateUtil {

    private SimpleDateFormat date() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
        sdf.setCalendar(Calendar.getInstance(new SimpleTimeZone(0, "GMT")));
        return sdf;
    }

    public Date getDate(String date) {
        try {
            return date().parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public String formatDate(Date date) {
        return date().format(date);
    }

    public String formatDate(long date) {
        return date().format(date);
    }


}

package com.ccreanga.webserver;

import com.ccreanga.webserver.http.InvalidMessageException;

import static com.ccreanga.webserver.http.HttpStatus.BAD_REQUEST;

public class ParseUtil {

    private long parseLong(String string, long min, long max) {
        try {
            if (string == null)
                throw new ConfigurationException("Cannot find the value " + string);

            int value = Integer.parseInt(string);
            if ((value < min) || (value > max))
                throw new ConfigurationException("Cannot configure " + string + " - expecting a number between " + min + " and " + max + " instead of " + value);
            return value;
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Cannot configure " + string + " - expecting an integer  instead of " + string);
        }
    }


}

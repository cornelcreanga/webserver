package com.ccreanga.webserver.filehandler;

import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;

public class Common {

    public static void addMandatoryHeaders(HttpHeaders responseHeaders){
        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");
    }

}

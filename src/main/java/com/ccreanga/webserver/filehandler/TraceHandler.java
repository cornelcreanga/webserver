package com.ccreanga.webserver.filehandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpMethodHandler;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class TraceHandler implements HttpMethodHandler {

    @Override
    public void handleResponse(HttpRequestMessage request, Configuration configuration, OutputStream out) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        writeResponseLine(HttpStatus.OK, out);
        responseHeaders.putHeader(CONTENT_TYPE, "message/http");
        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));

        ByteArrayOutputStream body = new ByteArrayOutputStream(128);

        body.write("TRACE ".getBytes(ISO_8859_1));
        body.write(request.getUri().getBytes(ISO_8859_1));
        body.write(" ".getBytes(ISO_8859_1));
        body.write(request.getVersion().toString().getBytes(ISO_8859_1));
        body.write("\n\r".getBytes(ISO_8859_1));


        HttpHeaders requestHeaders = request.getHeaders();
        if (requestHeaders.hasHeader("Cookie"))//todo - add basic auth too
            requestHeaders.putHeader("Cookie", "*****REMOVED*****");

        writeHeaders(request.getHeaders(), body);

        responseHeaders.putHeader(CONTENT_LENGTH, "" + body.size());

        writeHeaders(responseHeaders, out);
        body.writeTo(out);
    }
}

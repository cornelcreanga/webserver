package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.ParseUtil;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpRequestParser;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.ioutil.MultipartStream;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Map;

import static com.ccreanga.webserver.formatters.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeErrorResponse;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;
import static com.google.common.net.HttpHeaders.*;
import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.HOST;

public class PostHandler implements HttpMethodHandler {
    @Override
    public void handleResponse(HttpRequestMessage request, Configuration cfg, OutputStream out) throws IOException {
        //consume body - otherwise the server will try to parse it as a valid HTTP request and it will return 401

        HttpHeaders responseHeaders = new HttpHeaders();
        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");

        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((!request.hasHeader(HOST)) && (request.isHTTP1_1())) {//host is mandatory
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "missing host header", out);
            return;
        }

        String contentType = request.getHeader("Content-Type");
        if (request.headerContainsValue("Content-Type","multipart/form-data")){
            int index = contentType.indexOf("boundary");
            if (index==-1){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data without boundary", out);
                return;
            }
            String boundary = ParseUtil.right(contentType.substring(index+1),'=');
            if (boundary.length()==0){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "boundary value is missing", out);
                return;
            }
            MultipartStream stream = new MultipartStream(request.getBody(),boundary.getBytes());
            boolean nextPart = stream.skipPreamble();
            while(nextPart) {
                String header = stream.readHeaders();
                int maxLineLength = cfg.getRequestMaxLineLength();
                int maxHeaders = cfg.getRequestMaxHeaders();
                HttpHeaders httpHeaders = HttpRequestParser.consumeHeaders(new ByteArrayInputStream(header.getBytes("UTF-8")),maxLineLength,maxHeaders);
                Map<String,String> headerParams = httpHeaders.getHeaderParams("Content-Disposition");

                System.out.println(headerParams.get("name"));
                System.out.println(headerParams.get("filename"));
                stream.readBodyData(System.out);
                nextPart = stream.readBoundary();
            }
            int i;//consume the last byte; todo - why the stream is not fully consumed? could it be from chunkedinputstream
            while ((i=request.getBody().read()) > 0);

        }



        writeResponseLine(HttpStatus.NOT_IMPLEMENTED, out);//todo - yet

        responseHeaders.putHeader(CONTENT_LENGTH, "0");
        writeHeaders(responseHeaders, out);
        ContextHolder.get().setContentLength("-");


    }
}

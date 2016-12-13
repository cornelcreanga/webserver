package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.repository.ForbiddenException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpMessageWriter.*;

public class PostHandler implements HttpMethodHandler {
    @Override
    public void handleResponse(HttpRequestMessage request, Configuration cfg, OutputStream out) throws IOException {

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

        if (request.getUri().contains("..")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.FORBIDDEN, ".. is not allowed", out);
            return;
        }


        if (!cfg.isRootFolderWritable()) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "the root folder is configured read only", out);
            return;
        }

        String contentType = request.getHeader(CONTENT_TYPE);
        if ((contentType==null) || contentType.length()==0){
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "content type is mandatory for POST", out);
            return;
        }

        if (contentType.contains("application/x-www-form-urlencoded")){
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
            return;
//            String form = IOUtil.readToken(in, -1, "UTF-8", 8 * 1024 * 1024);
//            int limit = 10000;//max 10000 params -todo -config that
//            try {
//                params = StringUtil.parseFormEncodedParams(form, limit);
//            } catch (TooManyEntriesException e) {
//                throw new InvalidMessageException("too many form params", HttpStatus.BAD_REQUEST);
//            }

        }

        if (contentType.contains("multipart/form-data")){
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
            return;

//            int index = contentType.indexOf("boundary");
//            if (index == -1) {
//                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data without boundary", out);
//                return;
//            }
//            String boundary = StringUtil.right(contentType.substring(index + 1), '=');
//            if (boundary.length() == 0) {
//                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "boundary value is missing", out);
//                return;
//            }
//            MultipartStream stream = new MultipartStream(request.getBody(), boundary.getBytes());
//            boolean nextPart = stream.skipPreamble();
//            Map<String, List<String>> params = new HashMap<>();
//            while (nextPart) {
//                String header = stream.readHeaders();
//                int maxLineLength = cfg.getRequestMaxLineLength();
//                int maxHeaders = cfg.getRequestMaxHeaders();
//                HttpHeaders httpHeaders = HttpRequestParser.consumeHeaders(new ByteArrayInputStream(header.getBytes("UTF-8")), maxLineLength, maxHeaders);
//                Map<String, String> headerParams = httpHeaders.getHeaderParams("Content-Disposition");
//
//                String name = headerParams.get("name");
//                if (name == null || name.length() == 0) {//bad request
//                    writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "missing/empty name parameter", out);
//                    return;
//                }
//                String filename = headerParams.get("filename");
//                if (filename == null) {
//                    params.putIfAbsent(name, new ArrayList<>());
//                    List<String> values = params.get(name);
//                    ByteArrayOutputStream valueOut = new ByteArrayOutputStream();
//                    stream.readBodyData(valueOut);
//                    values.add(new String(valueOut.toByteArray()));
//                } else {
//                    System.out.println(filename);
//                    stream.readBodyData(System.out);
//                    //todo - save the files somewhere
//                }
//
//                nextPart = stream.readBoundary();
//            }
//            if (request.isChunked()) {//we need to consume the ending 3 bytes of the chunked input stream
//                while (request.getBody().read() > 0) ;
//            }

        }

        String extension = Mime.getExtension(contentType);
        if (extension==null){
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "content type "+contentType+" is not known", out);
            return;
        }
        String uri = request.getUri();
        if (!uri.endsWith("/"))
            uri = uri+"/";

        File file = new File(cfg.getServerRootFolder()+uri+System.currentTimeMillis()+"."+extension);
        if (!file.getParentFile().exists()) {
            boolean created = file.getParentFile().mkdirs();
            if (!created) {
                //todo
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR, "cannot create resource", out);
                return;
            }
        }
        try(FileOutputStream outputStream = new FileOutputStream(file);){
            if (!request.isChunked())
                IOUtil.copy(request.getBody(),outputStream,0,request.getLength());
            else
                IOUtil.copy(request.getBody(),outputStream);
        }catch (Exception e){
            //todo
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR, "cannot create resource", out);
            return;
        }

        writeResponseLine(HttpStatus.CREATED, out);

        responseHeaders.putHeader(LOCATION, uri+file.getName());
        writeHeaders(responseHeaders, out);
        ContextHolder.get().setContentLength("-");


    }
}

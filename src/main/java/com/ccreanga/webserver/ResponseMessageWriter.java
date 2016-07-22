package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.util.DateUtil;
import com.ccreanga.webserver.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ResponseMessageWriter {

    public static final byte[] CRLF = {0x0d, 0x0a};
    public static final byte[] SP = {0x20};
    public static final byte[] HEADER_SEP = {0x20, 0x3A, 0x20};
    private boolean chunked = false;


    private String buildHtmlError(HttpStatus status) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>").append(status).append(' ').append(status.getReasonPhrase()).append("</title></head>");
        sb.append("<body><h1>").append(status).append(' ').append(status.getReasonPhrase()).append("</h1></body>");
        sb.append("</html>");
        return sb.toString();
    }

    private boolean isHTML(String resource) {
        String extension = IOUtil.getExtension(resource);
        return ("html".equals(extension)) || ("htm".equals(extension));
    }

    public void writeRequestError(OutputStream out,HttpStatus status) throws IOException {
        DateUtil dateUtil = new DateUtil();
        String response = "HTTP/1.1 " + status.value() + " " + status.getReasonPhrase() + "\n" +
                "Content-Length: 0\n" +
                "Date:" + dateUtil.formatDate(System.currentTimeMillis()) + "\n\n";
        out.write(IOUtil.ascii(response));
    }


    /**
     * Writes a response to the output stream
     */
    public void write(RequestMessage request, ResponseMessage response, OutputStream out) throws IOException {


        out.write(IOUtil.ascii(request.getVersion()));
        out.write(SP);
        out.write(IOUtil.ascii("" + response.getStatus()));
        out.write(SP);
        out.write((IOUtil.ascii(response.getStatus().getReasonPhrase())));
        out.write(CRLF);

        String errorHtml = "";

        //check if resource exists
        //remove the parameters, as we only deliver static files
        String resource = request.getResource();
        int index = resource.indexOf('?');
        if (index!=-1)
            resource = resource.substring(0,index);

        response.setHeader(HTTPHeaders.contentType, Mime.getType(IOUtil.getExtension(resource)));
        if (response.getStatus() == HttpStatus.OK) {
            response.setHeader(HTTPHeaders.contentLength, "" + new File(response.getResourceFullPath()).length());
        } else {
            if (isHTML(resource)) {
                errorHtml = buildHtmlError(response.getStatus());
                byte[] body = IOUtil.utf(errorHtml);
                response.setHeader(HTTPHeaders.contentLength, "" + body.length);
            } else {
                response.setHeader(HTTPHeaders.contentLength, "0");
            }
        }

        Map<String, String> headerMap = response.getHeaders().getAllHeadersMap();
        for (Map.Entry<String, String> e : headerMap.entrySet()) {
            out.write(IOUtil.ascii(e.getKey()));
            out.write(HEADER_SEP);
            out.write(IOUtil.ascii(e.getValue()));
            out.write(CRLF);
        }
        out.write(CRLF);

        if ((response.getStatus() == HttpStatus.OK) && (!response.isIgnoreBody())) {
            IOUtil.inputToOutput(new FileInputStream(response.getResourceFullPath()), out);
        } else if (isHTML(resource)) {
            out.write(IOUtil.utf(errorHtml));
        }
        out.flush();

    }


}

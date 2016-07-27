package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.util.DateUtil;
import com.ccreanga.webserver.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    public void writeRequestError(OutputStream out,HttpStatus status) throws IOException {
        //todo - recheck
        String response = "HTTP/1.1 " + status.value() + " " + status.getReasonPhrase() + "\n" +
                "Content-Length: 0\n" +
                "Date:" + DateUtil.currentDate() + "\n\n";
        out.write(IOUtil.ascii(response));
    }


    /**
     * Writes a response to the output stream
     */
    public void write(RequestMessage request, ResponseMessage response, OutputStream out) throws IOException {

        ContextHolder.get().setStatusCode(""+response.getStatus().value());
        out.write(IOUtil.ascii(request.getVersion()));
        out.write(SP);
        out.write(IOUtil.ascii("" + response.getStatus()));
        out.write(SP);
        out.write((IOUtil.ascii(response.getStatus().getReasonPhrase())));
        out.write(CRLF);

        String errorHtml = "";

        //remove the parameters, as we only deliver static files
        String resource = request.getUri();
        int index = resource.indexOf('?');
        if (index!=-1)
            resource = resource.substring(0,index);

        response.setHeader(HTTPHeaders.CONTENT_TYPE, Mime.getType(IOUtil.getExtension(resource)));
        if (response.getStatus() == HttpStatus.OK) {
            String length = String.valueOf(new File(response.getResourceFullPath()).length());
            ContextHolder.get().setContentLength(length);
            response.setHeader(HTTPHeaders.CONTENT_LENGTH, length);
        } else {
            //todo - it should not return html unless the client accepts that
            errorHtml = TemplateRepository.instance().buildError(response.getStatus(),"");
            byte[] body = IOUtil.utf(errorHtml);
            ContextHolder.get().setContentLength(String.valueOf(body.length));
            response.setHeader(HTTPHeaders.CONTENT_LENGTH, String.valueOf(body.length));
            //response.setHeader(HTTPHeaders.CONTENT_LENGTH, "0");
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
        } else {
            //todo - it should not return html unless the client accepts that
            out.write(IOUtil.utf(errorHtml));
        }
        out.flush();

    }


}

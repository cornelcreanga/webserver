package com.ccreanga.webserver;


import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPMethod;
import com.ccreanga.webserver.util.LimitedLengthInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

public class RequestParser {

    private static final Logger accessLog = LoggerFactory.getLogger("accessLog");
    private static final String version1_1 = "HTTP/1.1";

    /**
     * Read a line from a stream.
     */
    public String readLine(InputStream in, String enc) throws IOException {
        int buflen = 256;
        byte[] buf = new byte[buflen];
        int count = 0;
        int c;
        while ((c = in.read()) != -1 && (c != '\n') ) {
            if (count == buflen) { // expand buffer
                buflen = 2 * buflen;
                byte[] expanded = new byte[buflen];
                System.arraycopy(buf, 0, expanded, 0, count);
                buf = expanded;
            }
            buf[count++] = (byte) c;
        }
        if (c == -1)
            return null;
        if (count==0)
            return "";
        if (buf[count - 1] == '\r')
            count--;
        return new String(buf, 0, count, enc);
    }


    public RequestMessage parseRequest(InputStream in,Configuration configuration) throws IOException, InvalidMessageFormat {
        String line;
        HTTPMethod httpMethod = null;
        String resource = null;
        String version = null;
        HTTPHeaders headers;
        try {
            //prevent dos attack/huge strings.
            LimitedLengthInputStream reader = new LimitedLengthInputStream(in, configuration.getMaxGetSize());

            //read the first line
            while ((line = readLine(reader, configuration.getRequestGetEncoding())) != null) {
                if (line.isEmpty())//empty line is allowed
                    continue;
                ContextHolder.get().setUrl(line);
                int index = line.indexOf(' ');
                if (index == -1)
                    throw new InvalidMessageFormat("malformed url");
                String method = line.substring(0, index).trim();

                try {
                    httpMethod = HTTPMethod.valueOf(method);
                } catch (IllegalArgumentException e) {
                    throw new InvalidMessageFormat("invalid http method");
                }

                int secondIndex = line.indexOf(' ', index + 1);
                if (secondIndex == -1)
                    throw new InvalidMessageFormat("malformed url");
                //remove the '/' in front of the resource
                resource = line.substring(index + 1, secondIndex).trim();
                version = line.substring(secondIndex).trim();

                break;
            }

            headers = new HTTPHeaders();
            //read the HTTPHeaders.
            String previousHeader = "";
            while ((line = readLine(reader, configuration.getRequestGetEncoding())) != null) {
                if (line.isEmpty()) {
                    break;
                }
                if (Character.isSpaceChar(line.charAt(0))) {
                    headers.appendHeader(previousHeader, line.trim());
                } else {
                    int separator = line.indexOf(':');
                    if (separator == -1)
                        throw new InvalidMessageFormat("malformed http header");
                    String header = line.substring(0, separator).trim();
                    String value = line.substring(separator + 1).trim();
                    headers.appendHeader(header, value);
                    previousHeader = header;
                }
            }

        } catch (IndexOutOfBoundsException e) {
            throw new InvalidMessageFormat("malformed url");
        }
        boolean chunk = "chunked".equals(headers.getHeader(HTTPHeaders.TRANSFER_ENCODING));
        long length = -1;

        String len = headers.getHeader(HTTPHeaders.CONTENT_LENGTH);
        try {
            if (len != null)
                length = Long.parseLong(len);
        } catch (NumberFormatException e) {
            throw new InvalidMessageFormat("invalid content lenght value "+len);
        }
        //we cannot have both chunk and length
        if ((chunk) && (length != -1))
            throw new InvalidMessageFormat("chunked and Content-Length are mutually exclusive");
        return new RequestMessage(httpMethod, headers, resource, version, in, chunk, length);

    }


}

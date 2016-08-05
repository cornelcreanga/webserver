package com.ccreanga.webserver.http;


import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InvalidMessageFormatException;
import com.ccreanga.webserver.ioutil.BoundedBufferedReader;
import com.ccreanga.webserver.logging.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class does the "hard work" - parsing the http request.
 * It tries to respect all the RFC defined conditions; besides them it will also check for very large requests or too many headers (
 * using the Configuration.getRequestMaxLines, Configuration.getRequestMaxLineLength, Configuration.getRequestMaxHeaders values)
 *
 */
public class HttpRequestParser {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");

    /**
     * Parse the http request.
     * @param in - input stream
     * @param configuration - configuration
     * @return The HttpRequestMessage object
     * @throws IOException - i/o error
     * @throws InvalidMessageFormatException - if the request cannot be parsed to a valid HttpRequestMessage object
     */
    public HttpRequestMessage parseRequest(InputStream in, Configuration configuration) throws IOException, InvalidMessageFormatException {
        String line;
        HTTPMethod httpMethod;
        String resource;
        HTTPVersion version;
        HTTPHeaders headers;
        try {
            //prevent large requests / out of memory errors
            BoundedBufferedReader reader = new BoundedBufferedReader(
                    new InputStreamReader(in, "ISO8859_1"),
                    configuration.getRequestMaxLines(),
                    configuration.getRequestMaxLineLength());

            //read the first line;block until timeout/exception
            while (((line = reader.readLine()) == null) || (line.isEmpty())) ;

            serverLog.trace("Connection " + ContextHolder.get().getUuid() + " : " + line);
            ContextHolder.get().setUrl(line);
            int index = line.indexOf(' ');
            if (index == -1)
                throw new InvalidMessageFormatException("malformed url");
            String method = line.substring(0, index).trim();

            try {
                httpMethod = HTTPMethod.valueOf(method);
            } catch (IllegalArgumentException e) {
                throw new InvalidMessageFormatException("invalid http method " + method);
            }

            int secondIndex = line.indexOf(' ', index + 1);
            if (secondIndex == -1)
                throw new InvalidMessageFormatException("malformed url");
            //remove the '/' in front of the resource
            resource = line.substring(index + 1, secondIndex).trim();
            try {
                version = HTTPVersion.from(line.substring(secondIndex).trim());
            } catch (IllegalArgumentException e) {
                throw new InvalidMessageFormatException("invalid http version " + line.substring(secondIndex).trim());
            }

            headers = new HTTPHeaders();
            //read the HTTPHeaders.
            String previousHeader = "";
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }
                if (Character.isSpaceChar(line.charAt(0))) {//multi line headers
                    headers.appendHeader(previousHeader, line.trim());
                } else {
                    int separator = line.indexOf(':');
                    if (separator == -1)
                        throw new InvalidMessageFormatException("malformed http header");
                    String header = line.substring(0, separator).trim();
                    String value = line.substring(separator + 1).trim();
                    headers.appendHeader(header, value);
                    previousHeader = header;
                }
            }

            int headersCount = headers.getAllHeadersMap().size();
            if (headersCount > configuration.getRequestMaxHeaders())//todo - deal with that separately and change the status to 431?
                throw new InvalidMessageFormatException("too many headers " + headersCount);

        } catch (IndexOutOfBoundsException e) {
            throw new InvalidMessageFormatException("malformed url");
        }

        //todo - right now the chunked input stream is not handled as we only deal with GET methods
        boolean chunk = "chunked".equals(headers.getHeader(HTTPHeaders.TRANSFER_ENCODING));
        long length = -1;

        String len = headers.getHeader(HTTPHeaders.CONTENT_LENGTH);
        try {
            if (len != null)
                length = Long.parseLong(len);
        } catch (NumberFormatException e) {
            throw new InvalidMessageFormatException("invalid content lenght value " + len);
        }
        //we cannot have both chunk and length//todo
//        if ((chunk) && (length != -1))
//            throw new InvalidMessageFormatException("chunked and Content-Length are mutually exclusive");
        return new HttpRequestMessage(httpMethod, headers, resource, version, in, chunk, length);

    }


}

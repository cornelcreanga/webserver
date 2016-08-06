package com.ccreanga.webserver.http;


import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.ioutil.BoundedBufferedReader;
import com.ccreanga.webserver.logging.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.ccreanga.webserver.http.HTTPStatus.BAD_REQUEST;
import static com.ccreanga.webserver.http.HTTPStatus.REQUEST_HEADER_FIELDS_TOO_LARGE;
import static com.google.common.net.HttpHeaders.*;

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
     * @throws InvalidMessageException - if the request cannot be parsed to a valid HttpRequestMessage object
     */
    public HttpRequestMessage parseRequest(InputStream in, Configuration configuration) throws IOException, InvalidMessageException {
        String line;
        HTTPMethod httpMethod;
        String resource;
        HTTPVersion version;
        HTTPHeaders headers;
        try {
            //todo - it would be nice to also prevent the "lazy" request
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
                throw new InvalidMessageException("malformed url",BAD_REQUEST);
            String method = line.substring(0, index).trim();

            try {
                httpMethod = HTTPMethod.valueOf(method);
            } catch (IllegalArgumentException e) {
                throw new InvalidMessageException("invalid http method " + method,BAD_REQUEST);
            }

            int secondIndex = line.indexOf(' ', index + 1);
            if (secondIndex == -1)
                throw new InvalidMessageException("malformed url",BAD_REQUEST);
            //remove the '/' in front of the resource
            resource = line.substring(index + 1, secondIndex).trim();
            try {
                version = HTTPVersion.from(line.substring(secondIndex).trim());
            } catch (IllegalArgumentException e) {
                throw new InvalidMessageException("invalid http version " + line.substring(secondIndex).trim(),BAD_REQUEST);
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
                        throw new InvalidMessageException("malformed http header",BAD_REQUEST);
                    String header = line.substring(0, separator).trim();
                    String value = line.substring(separator + 1).trim();
                    headers.appendHeader(header, value);
                    previousHeader = header;
                }
            }

            int headersCount = headers.getAllHeadersMap().size();
            if (headersCount > configuration.getRequestMaxHeaders())
                throw new InvalidMessageException("too many headers " + headersCount,REQUEST_HEADER_FIELDS_TOO_LARGE);

        } catch (IndexOutOfBoundsException e) {
            throw new InvalidMessageException("malformed url",BAD_REQUEST);
        }

        //todo - right now the chunked input stream is not handled as we only deal with GET methods
        boolean chunk = "chunked".equals(headers.getHeader(TRANSFER_ENCODING));
        long length = -1;

        String len = headers.getHeader(CONTENT_LENGTH);
        try {
            if (len != null)
                length = Long.parseLong(len);
        } catch (NumberFormatException e) {
            throw new InvalidMessageException("invalid content lenght value " + len,BAD_REQUEST);
        }
        //we cannot have both chunk and length//
        if ((chunk) && (length != -1))
            throw new InvalidMessageException("chunked and Content-Length are mutually exclusive",BAD_REQUEST);
        return new HttpRequestMessage(httpMethod, headers, resource, version, in, chunk, length);

    }


}

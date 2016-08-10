package com.ccreanga.webserver.http;


import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.ioutil.ChunkedInputStream;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.ccreanga.webserver.http.HttpStatus.BAD_REQUEST;
import static com.ccreanga.webserver.http.HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE;
import static com.google.common.net.HttpHeaders.*;

/**
 * This class does the "hard work" - parsing the http request.
 * It tries to respect all the RFC defined conditions; besides them it will also check for very large requests or too many headers (
 * using the Configuration.getRequestMaxLines, Configuration.getRequestMaxLineLength, Configuration.getRequestMaxHeaders values)
 */
public class HttpRequestParser {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");


    public static HttpRequestLine consumeRequestMethod(InputStream in, int lineMaxLength,int uriMaxLength) throws IOException, InvalidMessageException {
        String line;
        HttpMethod httpMethod;
        String uri;
        HttpVersion version;

        //read the first line;block until timeout/exception
        int counter=0;
        while (((line = IOUtil.readLine(in, lineMaxLength)) == null) || (line.isEmpty())){
            if ((line!=null) && (line.isEmpty())){
                counter++;
                if (counter>=100)
                    throw new InvalidMessageException("too many empty lines ", BAD_REQUEST);
            }
        };


        serverLog.trace("Connection " + ContextHolder.get().getUuid() + " : " + line);
        ContextHolder.get().setUrl(line);
        int index = line.indexOf(' ');
        if (index == -1)
            throw new InvalidMessageException("malformed url", BAD_REQUEST);
        String method = line.substring(0, index).trim();

        try {
            httpMethod = HttpMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            throw new InvalidMessageException("invalid http method " + method, BAD_REQUEST);
        }

        int secondIndex = line.indexOf(' ', index + 1);
        if (secondIndex == -1)
            throw new InvalidMessageException("malformed url", BAD_REQUEST);
        //remove the '/' in front of the uri
        uri = line.substring(index + 1, secondIndex).trim();
        if (uri.length()>uriMaxLength)
            throw new UriTooLongException("uri too long "+uri.length());
        try {
            version = HttpVersion.from(line.substring(secondIndex).trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidMessageException("invalid http version " + line.substring(secondIndex).trim(), BAD_REQUEST);
        }
        return new HttpRequestLine(httpMethod, uri, version);
    }

    public static HttpHeaders consumeHeaders(InputStream in, int lineMaxLength, int headerMaxNo) throws IOException, InvalidMessageException {
        HttpHeaders headers = new HttpHeaders();
        //read the HttpHeaders.
        String previousHeader = "";
        String line;
        while ((line = IOUtil.readLine(in, lineMaxLength)) != null) {
            if (line.isEmpty()) {
                break;
            }
            if (Character.isSpaceChar(line.charAt(0))) {//multi line headers
                headers.appendHeader(previousHeader, line.trim());
            } else {
                int separator = line.indexOf(':');
                if (separator == -1)
                    throw new InvalidMessageException("malformed http header", BAD_REQUEST);
                String header = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                headers.appendHeader(header, value);
                previousHeader = header;
            }
        }

        int headersCount = headers.getAllHeadersMap().size();
        if (headersCount > headerMaxNo)
            throw new InvalidMessageException("too many headers " + headersCount, REQUEST_HEADER_FIELDS_TOO_LARGE);
        return headers;

    }

    /**
     * Parse the http request.
     *
     * @param in  - input stream
     * @param cfg - configuration
     * @return The HttpRequestMessage object
     * @throws IOException             - i/o error
     * @throws InvalidMessageException - if the request cannot be parsed to a valid HttpRequestMessage object
     */
    public HttpRequestMessage parseRequest(InputStream in, Configuration cfg) throws IOException, InvalidMessageException {
        //            //todo - it would be nice to also prevent the "lazy" request

        int maxLineLength = cfg.getRequestMaxLineLength();
        int maxHeaders = cfg.getRequestMaxHeaders();
        HttpRequestLine httpRequestLine = HttpRequestParser.consumeRequestMethod(in, maxLineLength,cfg.getRequestURIMaxSize());
        HttpHeaders httpHeaders = HttpRequestParser.consumeHeaders(in, maxLineLength, maxHeaders);

        boolean chunk = false;
        String encoding = httpHeaders.getHeader(TRANSFER_ENCODING);
        if (encoding!=null){
            //todo - check for gzip/deflate in transfer encoding
            chunk = encoding.contains("chunked");
            if ((chunk) && (encoding.lastIndexOf("chunked")!=(encoding.length()-7)))//chunked is not the last encoding
                throw new InvalidMessageException("invalid encoding header", HttpStatus.BAD_REQUEST);

        }

        long length = -1;
        String len = httpHeaders.getHeader(CONTENT_LENGTH);
        try {
            if (len != null)
                length = Long.parseLong(len);
        } catch (NumberFormatException e) {
            throw new InvalidMessageException("invalid content length value " + len, BAD_REQUEST);
        }
        //we cannot have both chunk and length//
        if ((chunk) && (length != -1))
            throw new InvalidMessageException("chunked and Content-Length are mutually exclusive", BAD_REQUEST);
        if (chunk) {
            return new HttpRequestMessage(httpRequestLine, httpHeaders, new ChunkedInputStream(in, httpHeaders, maxLineLength, maxHeaders), -1);
        }
        return new HttpRequestMessage(httpRequestLine, httpHeaders, in, length);

    }


}

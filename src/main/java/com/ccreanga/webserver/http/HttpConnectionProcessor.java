package com.ccreanga.webserver.http;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.ConnectionProcessor;
import com.ccreanga.webserver.InvalidMessageFormatException;
import com.ccreanga.webserver.formatters.DateUtil;
import com.ccreanga.webserver.ioutil.LengthExceededException;
import com.ccreanga.webserver.logging.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HttpConnectionProcessor implements ConnectionProcessor {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    private static final Logger accessLog = LoggerFactory.getLogger("accessLog");


    public void handleConnection(Socket socket, Configuration configuration) {
        try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream();) {
            /**
             * The connection will be kept open unless
             * a)the connection will explicitly request close (HTTPHeaders.CONNECTION)
             * b)the request message is unparsable (not even http)
             * c)the connection will timeout (Configuration/timeoutSeconds)
             * d)the connection is using HTTP 1.0 and is not using the the keep alive header
             * e)socket error (broken pipe etc)
             */
            while (true) {
                boolean shouldCloseConnection = false;
                HttpRequestMessage request = null;
                boolean responseSyntaxCorrect = true;
                HTTPStatus invalidStatus = null;
                try {
                    HttpRequestParser httpRequestParser = new HttpRequestParser();
                    request = httpRequestParser.parseRequest(input, configuration);
                } catch (InvalidMessageFormatException e) {
                    responseSyntaxCorrect = false;
                    invalidStatus = HTTPStatus.BAD_REQUEST;
                } catch (LengthExceededException e) {
                    responseSyntaxCorrect = false;
                    ContextHolder.get().setUrl("url too long");
                    invalidStatus = HTTPStatus.URI_TOO_LONG;
                }
                if (responseSyntaxCorrect) {

                    HttpMessageHandler httpMessageHandler = new HttpMessageHandler();
                    httpMessageHandler.handleMessage(request, configuration, output);
                    serverLog.trace("Connection " + ContextHolder.get().getUuid() + " responded with " + ContextHolder.get().getStatusCode());

                    if ((request.headerIs(HTTPHeaders.CONNECTION, "close")) ||
                            (request.getVersion().equals(HTTPVersion.HTTP_1_0)) && !request.headerIs(HTTPHeaders.CONNECTION, "Keep-Alive"))
                        shouldCloseConnection = true;
                } else {
                    //we were not event able to parse the first request line (not an HTTP message), so write an error and close the connection.
                    ContextHolder.get().setStatusCode(invalidStatus.toString());
                    ContextHolder.get().setContentLength("-");
                    HttpMessageWriter.writeResponseLine(invalidStatus, output);
                    serverLog.trace("Connection " + ContextHolder.get().getUuid() + " request was unparsable, responded with " + ContextHolder.get().getStatusCode());
                    shouldCloseConnection = true;
                }
                output.flush();
                accessLog.info(ContextHolder.get().generateLogEntry());
                if (shouldCloseConnection) {
                    serverLog.trace("Connection " + ContextHolder.get().getUuid() + " requested close");
                    break;
                }

            }
        } catch (SocketTimeoutException e) {
            serverLog.trace("Connection " + ContextHolder.get().getUuid() + " was closed due to timeout");
        } catch (IOException e) {
            serverLog.trace("Connection " + ContextHolder.get().getUuid() + " was closed because of an I/O error: " + e.getMessage());
        }

    }

}

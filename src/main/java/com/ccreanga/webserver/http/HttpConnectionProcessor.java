package com.ccreanga.webserver.http;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.ConnectionProcessor;
import com.ccreanga.webserver.ioutil.LengthExceededException;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.google.common.net.HttpHeaders.CONNECTION;

public class HttpConnectionProcessor implements ConnectionProcessor {

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    private static final Logger accessLog = LoggerFactory.getLogger("accessLog");


    public void handleConnection(Socket socket, Configuration configuration) {
        try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream();) {
            /**
             * The connection will be kept open unless
             * a)the connection will explicitly request close (HTTPHeaders.CONNECTION)
             * b)the request message is unparsable (we can't eve build an HttpRequestMessage)
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
                    //try to parse the message and to convert it to an HttpRequestMessage. Only HTTP 1.0 and HTTP 1.1 messages are accepted
                    HttpRequestParser httpRequestParser = new HttpRequestParser();
                    request = httpRequestParser.parseRequest(input, configuration);
                } catch (InvalidMessageException e) {
                    responseSyntaxCorrect = false;
                    invalidStatus = e.getStatus();
                } catch (LengthExceededException e) {
                    responseSyntaxCorrect = false;
                    ContextHolder.get().setUrl("url too long");
                    invalidStatus = HTTPStatus.URI_TOO_LONG;
                }
                if (responseSyntaxCorrect) {
                    //we can handle the message now
                    HttpMessageHandler httpMessageHandler = new HttpMessageHandler();
                    httpMessageHandler.handleMessage(request, configuration, output);
                    serverLog.trace("Connection " + ContextHolder.get().getUuid() + " responded with " + ContextHolder.get().getStatusCode());
                    //after the message is handled decide if we should close the connection or not
                    if ((request.headerIs(CONNECTION, "close")) ||
                            (request.getVersion().equals(HTTPVersion.HTTP_1_0)) && !request.headerIs(CONNECTION, "Keep-Alive"))
                        shouldCloseConnection = true;
                } else {
                    //we were not event able to parse the first request line (this is not an HTTP message), so write an error and close the connection.
                    ContextHolder.get().setStatusCode(invalidStatus.toString());
                    ContextHolder.get().setContentLength("-");
                    HttpMessageWriter.writeResponseLine(invalidStatus, output);
                    serverLog.trace("Connection " + ContextHolder.get().getUuid() + " request was unparsable, responded with " + ContextHolder.get().getStatusCode());
                    shouldCloseConnection = true;
                }
                output.flush();
                //write into the access log
                accessLog.info(LogEntry.generateLogEntry(ContextHolder.get()));
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

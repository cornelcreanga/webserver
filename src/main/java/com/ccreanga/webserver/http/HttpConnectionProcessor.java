package com.ccreanga.webserver.http;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.ConnectionProcessor;
import com.ccreanga.webserver.filehandler.FileMessageHandler;
import com.ccreanga.webserver.ioutil.LimitedInputStream;
import com.ccreanga.webserver.ioutil.LineTooLongException;
import com.ccreanga.webserver.logging.ContextHolder;
import com.ccreanga.webserver.logging.LogEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.ccreanga.webserver.Server.accessLog;
import static com.ccreanga.webserver.Server.serverLog;
import static com.ccreanga.webserver.http.HttpHeaders.CONNECTION;

public class HttpConnectionProcessor implements ConnectionProcessor {

    HttpMessageHandler httpMessageHandler;

    public void handleConnection(Socket socket,HttpMessageHandler httpMessageHandler, Configuration configuration) {
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            /**
             * The connection will be kept open unless
             * a)the connection will explicitly request close (HttpHeaders.CONNECTION)
             * b)the request message is unparsable (we can't even build an HttpRequestMessage)
             * c)the connection will timeout (Configuration/timeoutSeconds)
             * d)the connection is using HTTP 1.0 and is not using the the keep alive header
             * e)socket error (broken pipe etc)
             */
            boolean shouldKeepConnectionOpen = true;
            while (shouldKeepConnectionOpen) {
                HttpRequestMessage request = null;
                boolean responseSyntaxCorrect = true;
                HttpStatus invalidStatus = null;
                try {
                    //try to parse the message and to convert it to an HttpRequestMessage. Only HTTP 1.0 and HTTP 1.1 messages are accepted
                    HttpRequestParser httpRequestParser = new HttpRequestParser();
                    request = httpRequestParser.parseRequest(input, configuration);
                } catch (InvalidMessageException e) {
                    responseSyntaxCorrect = false;
                    invalidStatus = e.getStatus();
                } catch (UriTooLongException e) {
                    responseSyntaxCorrect = false;
                    ContextHolder.get().setUrl("uri is too long");
                    invalidStatus = HttpStatus.URI_TOO_LONG;
                } catch (LimitedInputStream.LengthExceededException e) {
                    responseSyntaxCorrect = false;
                    ContextHolder.get().setUrl("request message is too long");
                    invalidStatus = HttpStatus.PAYLOAD_TOO_LARGE;
                } catch (LineTooLongException e) {
                    responseSyntaxCorrect = false;
                    ContextHolder.get().setUrl("request line too long");
                    invalidStatus = HttpStatus.BAD_REQUEST;
                }
                if (responseSyntaxCorrect) {
                    //we can handle the message now
                    try {
                        httpMessageHandler.handleMessage(request, configuration, output);
                    } catch (LimitedInputStream.LengthExceededException e) {
                        ContextHolder.get().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE.toString());
                        ContextHolder.get().setContentLength("-");
                        HttpMessageWriter.writeResponseLine(HttpStatus.PAYLOAD_TOO_LARGE, output);
                        serverLog.fine("Connection " + ContextHolder.get().getUuid() + " request was too large and will be closed, responded with " + ContextHolder.get().getStatusCode());
                        ContextHolder.get().setUrl("request message is too long");
                        //close the connection. the next read will fail anyway because the previous request was not fully consumed
                        shouldKeepConnectionOpen = false;
                    }
                    serverLog.fine("Connection " + ContextHolder.get().getUuid() + " responded with " + ContextHolder.get().getStatusCode());
                    //after the message is handled decide if we should close the connection or not
                    if ((request.headerIsEqualWithValue(CONNECTION, "close")) ||
                            (request.getVersion().equals(HttpVersion.HTTP_1_0)) && !request.headerIsEqualWithValue(CONNECTION, "Keep-Alive")) {
                        shouldKeepConnectionOpen = false;
                        serverLog.fine("Connection " + ContextHolder.get().getUuid() + " requested close");
                    }

                } else {
                    //we were not event able to parse the first request line (this is not an HTTP message), so write an error and close the connection.
                    ContextHolder.get().setStatusCode(invalidStatus.toString());
                    ContextHolder.get().setContentLength("-");
                    HttpMessageWriter.writeResponseLine(invalidStatus, output);
                    serverLog.fine("Connection " + ContextHolder.get().getUuid() + " request was unparsable and will be closed, responded with " + ContextHolder.get().getStatusCode());
                    shouldKeepConnectionOpen = false;
                }
                output.flush();
                //write into the access log
                accessLog.info(LogEntry.generateLogEntry(ContextHolder.get()));

            }
        } catch (SocketTimeoutException e) {
            serverLog.fine("Connection " + ContextHolder.get().getUuid() + " was closed due to timeout");
        } catch (IOException e) {
            e.printStackTrace();
            serverLog.fine("Connection " + ContextHolder.get().getUuid() + " was closed because of an I/O error: " + e.getMessage());
        }

    }

}

package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HTTPStatus;
import com.ccreanga.webserver.http.HTTPVersion;
import com.ccreanga.webserver.ioutil.LengthExceededException;
import com.ccreanga.webserver.logging.Context;
import com.ccreanga.webserver.logging.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class PersistentConnectionProcessor implements ConnectionProcessor {

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
                RequestMessage request = null;
                boolean responseSyntaxCorrect = true;
                HTTPStatus invalidStatus = null;
                try {
                    RequestParser requestParser = new RequestParser();
                    request = requestParser.parseRequest(input, configuration);
                } catch (InvalidMessageFormatException e) {
                    responseSyntaxCorrect = false;
                    invalidStatus = HTTPStatus.BAD_REQUEST;
                } catch (LengthExceededException e) {
                    responseSyntaxCorrect = false;
                    invalidStatus = HTTPStatus.URI_TOO_LONG;
                }
                if (responseSyntaxCorrect) {
                    //todo - we might want to wrap the outstream into another one (zip)
                    MessageHandler messageHandler = new MessageHandler();
                    messageHandler.handleMessage(request, configuration, output);
                    serverLog.trace("Connection " + ContextHolder.get().getUuid() + " responded with " + ContextHolder.get().getStatusCode());
                    //todo - chunked and keep alive dont work together for http 1.0
                    if ((request.headerIs(HTTPHeaders.CONNECTION,"close")) ||
                            (request.getVersion().equals(HTTPVersion.HTTP_1_0)) && !request.headerIs(HTTPHeaders.CONNECTION,"Keep-Alive"))
                        shouldCloseConnection = true;
                } else { //we were not event able to parse the first request line (this is not HTTP), so write an error and close the connection.
                    ContextHolder.get().setStatusCode(invalidStatus.toString());
                    MessageWriter.writeResponseLine(invalidStatus, output);
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
            serverLog.trace("Connection " + ContextHolder.get().getUuid() + " received " + e.getMessage());
        } finally {
            ContextHolder.cleanup();
        }

    }

}

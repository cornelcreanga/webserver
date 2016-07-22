package com.ccreanga.webserver;

import com.ccreanga.webserver.util.IOUtil;
import com.ccreanga.webserver.util.LengthExceededException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class PersistentConnectionProcessor implements Runnable {

    protected Socket socket = null;

    public PersistentConnectionProcessor(Socket socket) {
        this.socket = socket;
    }

    public void run() {

        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            //because we support http 1.1 all the connection are persistent. however, I have a feeling  that the performance is worse in this case.
            while (true) {
                boolean close = false;
                ResponseMessage response = null;
                RequestMessage request = null;

                try {
                    request = new RequestParser().parseRequest(input);
                } catch (InvalidMessageFormat e) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST);
                } catch (LengthExceededException e) {
                    response = new ResponseMessage(HttpStatus.URI_TOO_LONG);
                }

                if (response == null) {//we were able to build a syntax correct request

                    //todo - if X-Do-Not-Track and DNT Requests are not present and logging is enabled log the request

                    response = new MessageHandler().handleMessage(request);
                    new ResponseMessageWriter().write(request, response, output);
                    //we should be at the end of out input stream here. check if we received close
                    close = "close".equals(request.getHeader(Headers.connection));
                } else{ //we were not event able to parse the request body, so write an error and close the connection in order to free the resources.
                    new ResponseMessageWriter().writeRequestError(output,response.getStatus());
                    close = true;
                }
                if (close)
                    break;

            }
        } catch (SocketTimeoutException e) {
            //If the client is not sending any other requests close the socket.
        } catch (IOException e) {
            Server.log.error(Util.getStackTrace(e));
        } finally {
            IOUtil.close(output);
        }


    }


}

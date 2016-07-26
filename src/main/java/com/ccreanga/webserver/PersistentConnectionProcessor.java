package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.util.IOUtil;
import com.ccreanga.webserver.util.LengthExceededException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class PersistentConnectionProcessor implements Runnable {

    protected Socket socket = null;
    Configuration configuration;

    public PersistentConnectionProcessor(Socket socket,Configuration configuration) {

        this.socket = socket;
        this.configuration = configuration;
    }

    public void run() {

        try(InputStream input = socket.getInputStream();OutputStream output=socket.getOutputStream(); ) {

            //because we support http 1.1 all the connection are persistent.
            while (true) {
                boolean close = false;
                ResponseMessage response = null;
                RequestMessage request = null;

                try {
                    request = new RequestParser().parseRequest(input,configuration);
                } catch (InvalidMessageFormat e) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST);
                } catch (LengthExceededException e) {
                    response = new ResponseMessage(HttpStatus.URI_TOO_LONG);
                }

                if (response == null) {//we were able to build a syntax correct request

                    //todo - if X-Do-Not-Track and DNT Requests are not present and logging is enabled log the request

                    response = new MessageHandler().handleMessage(request,configuration);
                    new ResponseMessageWriter().write(request, response, output);
                    //we should be at the end of out input stream here. check if we received close
                    close = "close".equals(request.getHeader(HTTPHeaders.CONNECTION));
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
            //todo
        }


    }


}

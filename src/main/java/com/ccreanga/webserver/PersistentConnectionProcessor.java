package com.ccreanga.webserver;

import com.ccreanga.webserver.http.HTTPHeaders;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.util.IOUtil;
import com.ccreanga.webserver.util.LengthExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.UUID;

public class PersistentConnectionProcessor implements ConnectionProcessor{

    private static final Logger serverLog = LoggerFactory.getLogger("serverLog");
    private static final Logger accessLog = LoggerFactory.getLogger("accessLog");


    public void handleConnection(Socket socket,Configuration configuration) {
        ContextHolder.put(new Context());

        try(InputStream input = socket.getInputStream();OutputStream output=socket.getOutputStream();) {

            UUID uuid = UUID.randomUUID();
            ContextHolder.get().setUuid(uuid);
            String ip = getIp(socket);
            ContextHolder.get().setIp(ip) ;
            serverLog.info("Connection from ip "+ip + " started, uuid="+uuid);

            //because we support http 1.1 all the connection are persistent.
            while (true) {
                boolean close = false;
                ResponseMessage response = null;
                RequestMessage request = null;
                boolean responseSyntaxCorrect = true;

                try {
                    request = new RequestParser().parseRequest(input,configuration);
                } catch (InvalidMessageFormat e) {
                    responseSyntaxCorrect = false;
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST);
                } catch (LengthExceededException e) {
                    responseSyntaxCorrect = false;
                    response = new ResponseMessage(HttpStatus.URI_TOO_LONG);
                }
                if (responseSyntaxCorrect) {

                    response = new MessageHandler().handleMessage(request,configuration);
                    new ResponseMessageWriter().write(request, response, output);
                    serverLog.info("Connection "+ContextHolder.get().getUuid()+ " responded with "+response.getStatus());
                    accessLog.info(ContextHolder.get().generateLogEntry());
                    //todo - log something in the access log
                    //we should be at the end of out input stream here. check if we received close
                    close = "close".equals(request.getHeader(HTTPHeaders.CONNECTION));
                } else{ //we were not event able to parse the request body, so write an error and close the connection in order to free the resources.
                    new ResponseMessageWriter().writeRequestError(output,response.getStatus());
                    serverLog.info("Connection "+ContextHolder.get().getUuid()+ " response was unparsable, responded with "+response.getStatus());
                    close = true;
                }
                if (close) {
                    serverLog.info("Connection "+ ContextHolder.get().getUuid()+" requested close");
                    break;
                }

            }
        } catch (SocketTimeoutException e) {
            serverLog.info("Connection "+ ContextHolder.get().getUuid()+" was closed due to timeout");
        } catch (IOException e) {
            e.printStackTrace();//todo
        }finally{
            ContextHolder.cleanup();
        }

    }

    public String getIp(Socket socket){
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetAddress inetAddress = ((InetSocketAddress)socketAddress).getAddress();
            return inetAddress.toString();
        } else {
            return "Not an internet protocol socket.";
        }
    }

}

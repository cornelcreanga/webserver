package com.ccreanga.webserver.ioutil;

import com.google.common.io.Closeables;
import com.google.common.net.InetAddresses;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class IOUtil {

    public static void closeSilent(Closeable closeable){
        try {
            Closeables.close(closeable, true);
        } catch (IOException e) {/**ignore**/}
    }

    public static String getIp(Socket socket) {
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetAddress inetAddress = ((InetSocketAddress) socketAddress).getAddress();
            return InetAddresses.toAddrString(inetAddress);
        }
        return "Not an IP socket";

    }

}

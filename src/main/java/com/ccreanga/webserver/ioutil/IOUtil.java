package com.ccreanga.webserver.ioutil;

import com.google.common.io.Closeables;
import com.google.common.net.InetAddresses;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Stack;

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


    public static String extractParentResource(File file, File root){
        File traverse = file.getParentFile();
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        Stack<String> stack = new Stack<>();
        while(!traverse.equals(root)){
            stack.push(traverse.getName());
            traverse = traverse.getParentFile();
        }
        while(!stack.empty()){
            String next = stack.pop();
            sb.append(next).append("/");
        }
        return sb.toString();
    }

}

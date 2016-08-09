package com.ccreanga.webserver.ioutil;

import com.google.common.io.Closeables;
import com.google.common.net.InetAddresses;

import javax.naming.LimitExceededException;
import java.io.*;
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

    public static String readToken(InputStream in, int delim,
                                   String enc, int maxLength) throws IOException {
        int buflen = maxLength < 256 ? maxLength : 256; // start with less
        byte[] buf = new byte[buflen];
        int count = 0;
        int c;
        while ((c = in.read()) != -1 && c != delim) {
            if (count == buflen) { // expand buffer
                if (count == maxLength)
                    throw new LineTooLongException("token too large (" + count + ")");
                buflen = maxLength < 2 * buflen ? maxLength : 2 * buflen;
                byte[] expanded = new byte[buflen];
                System.arraycopy(buf, 0, expanded, 0, count);
                buf = expanded;
            }
            buf[count++] = (byte)c;
        }
        if (c == -1 && delim != -1)
            throw new EOFException("unexpected end of stream");
        return new String(buf, 0, count, enc);
    }
    public static String readLine(InputStream in,int maxLength) throws IOException {
        String s = readToken(in, '\n', "ISO8859_1", maxLength);
        return s.length() > 0 && s.charAt(s.length() - 1) == '\r'
                ? s.substring(0, s.length() - 1) : s;
    }


}

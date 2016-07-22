package com.ccreanga.webserver;


import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;

/**
 * This class offers methods for both weak/strong etags
 * Weak etag is computed by looking on the file last modified date
 * Strong etag is computed using md5
 */
public class EtagGenerator {


    public static String getDateBasedEtag(File file) {
        return "W/" + file.lastModified();
    }

    /**
     * Inspired from http://www.rgagnon.com/javadetails/java-0416.html
     *
     * @param in
     * @return
     */
    public static String computeMD5BasedEtag(InputStream in) {
        try {
            byte[] buffer = new byte[2048];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = in.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            byte[] b = complete.digest();

            StringBuilder sb = new StringBuilder(64);
            for (int i = 0; i < b.length; i++)
                sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("cannot compute md5");
        }
    }

    public static String getCachedMD5(URI uri) {
        throw new UnsupportedOperationException("This method is not yet implemented");
    }


}

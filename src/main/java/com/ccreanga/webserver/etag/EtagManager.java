package com.ccreanga.webserver.etag;


import com.ccreanga.webserver.repository.FileManager;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class offers methods for both weak/strong etags
 * Weak etag is computed by looking on the file last modified date
 * Strong etag is computed using md5
 */
public class EtagManager {

    private static final EtagManager manager = new EtagManager();

    private EtagManager(){}

    public static EtagManager getInstance(){
        return manager;
    }

    public String getFileEtag(File file, boolean weak){
        if (weak)
            return getFileWeakEtag(file);
        throw new IllegalArgumentException("strong etags are not yet supported");
    }


    /**
     * This method returns a file weak etag implementation based on file last modified time
     * It works for both files/folders - at least on the NTFS / etx* filesystems(the directory last modified time changes when a file or a subdirectory
     * (directly under this directory) is added, removed or renamed.)
     * @param file
     * @return
     */
    private String getFileWeakEtag(File file) {
        Preconditions.checkNotNull(file);
        return "W/\"" + file.lastModified()+"\"";
    }

    /**
     * This method returns a file strong etag implementation based on file md5. does not work (yet) for folders
     * @param file
     * @return
     */
    //Inspired from http://www.rgagnon.com/javadetails/java-0416.html
    private String getFileStrongEtag(File file) {
        Preconditions.checkNotNull(file);
        try(InputStream in = new FileInputStream(file)){
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
            for (byte aB : b) sb.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }

    }

}

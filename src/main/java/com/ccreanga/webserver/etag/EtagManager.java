package com.ccreanga.webserver.etag;


import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class offers methods for both weak/strong etags.
 * Weak etag is computed by looking on the file last modified date
 * Strong etag can be computed using md5 (not yet implemented)
 */
public class EtagManager {

    private static final EtagManager manager = new EtagManager();

    private EtagManager() {
    }

    public static EtagManager getInstance() {
        return manager;
    }

    /**
     * This method returns a file weak etag implementation based on the file last modified time
     *
     * @param file      file - cannot be a folder
     * @param extension - optional, can be empty or null
     * @param weak      - weak etag generation (based on the file last modified time) or strong (not yet implemented)
     * @return etag
     */
    public String getFileEtag(File file, String extension, boolean weak) {
        return getFileWeakEtag(file, extension);
    }

    public String getFileEtag(File file, boolean weak) {
        return getFileEtag(file, "", weak);
    }

    private String getFileWeakEtag(File file) {
        return getFileWeakEtag(file, "");
    }

    /**
     * This method returns a file weak etag implementation based on the file last modified time
     *
     * @param file
     * @return
     */
    private String getFileWeakEtag(File file, String extension) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.isFile());
        if (extension == null)
            extension = "";
        return "W/\"" + file.lastModified() + extension + "\"";
    }

    /**
     * This method returns a file strong etag implementation based on file md5. does not work (yet) for folders
     *
     * @param file
     * @return
     */
    //Inspired from http://www.rgagnon.com/javadetails/java-0416.html
    private String getFileStrongEtag(File file) {
        Preconditions.checkNotNull(file);
        try (InputStream in = new FileInputStream(file)) {
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

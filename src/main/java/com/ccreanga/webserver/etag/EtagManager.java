package com.ccreanga.webserver.etag;


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

    public static final String GZIP_EXT = "gz";
    public static final String DF_EXT = "df";
    private static final EtagManager manager = new EtagManager();

    private EtagManager() {
    }

    public static EtagManager getInstance() {
        return manager;
    }

    /**
     * This method returns a file weak etag implementation based on the file last modified time
     *
     * @param file     file - cannot be a folder
     * @param encoding - http encoding - the etag for a zipped resource is different than the etag for the uncompressed one. itcan be empty or null
     * @param weak     - weak etag generation (based on the file last modified time) or strong (not yet implemented)
     * @return etag
     */
    public String getFileEtag(File file, String encoding, boolean weak) {
        return getFileWeakEtag(file, encoding);
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
        if (extension == null)
            extension = "";
        return "W/\"" + file.lastModified() + extension + "\"";
    }

}

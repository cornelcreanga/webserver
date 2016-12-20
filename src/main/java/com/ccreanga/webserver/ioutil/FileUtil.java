package com.ccreanga.webserver.ioutil;

import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.common.StringUtil;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileUtil {

    private static File md5File(File file) {
        String name = file.getName();
        int last = name.lastIndexOf('.');
        if (last != -1)
            name = name.substring(0, last);
        return new File(file.getParent() + File.separator + "." + name + ".md5");
    }

    public static String createMD5file(File file, MessageDigest md) throws IOException {
        if (md == null) {
            try (FileInputStream in = new FileInputStream(file)) {
                md = getDigest(in);
            }
        }
        try (FileOutputStream out = new FileOutputStream(md5File(file))) {
            out.write(md.digest());
        }
        return StringUtil.bytesToHex(md.digest());
    }

    public static MessageDigest getDigest(InputStream is) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalException("can't instantiate md5..");
        }
        byte[] bytes = new byte[8192];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            md.update(bytes, 0, numBytes);
        }
        return md;
    }

    public static String getOrCreateMd5AsHex(File file) throws IOException {
        File md5 = md5File(file);
        if (!md5.exists())
            createMD5file(file, null);
        return StringUtil.bytesToHex(Files.readAllBytes(md5.toPath()));
    }

    public static void removeMd5(File file) throws IOException {
        File md5 = md5File(file);
        if (md5.exists())
            md5.delete();//todo
    }

}

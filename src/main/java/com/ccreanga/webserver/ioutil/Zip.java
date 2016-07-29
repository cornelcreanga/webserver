package com.ccreanga.webserver.ioutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {


    public static void main(String[] args) {
        if (args.length!=2)
            System.out.println("Usage Zip <folder> <zipname>");
        recursiveZipFolder(args[0],args[1]);
    }
    public static void recursiveZipFolder(String dir, String zipFileName) {
        File dirObj = new File(dir);
        ZipOutputStream out;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFileName));
            recursiveAddFolder(dirObj, out, dir);
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void recursiveAddFolder(File dirObj, ZipOutputStream out, String rootDirectory) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[4096];

        for (File file : files) {
            if (file.isDirectory()) {
                recursiveAddFolder(file, out, rootDirectory);
            } else {
                FileInputStream in = new FileInputStream(file.getAbsolutePath());
                String target = file.getAbsolutePath().substring(rootDirectory.length() + 1);
                target = target.replace('\\', '/');
                out.putNextEntry(new ZipEntry(target));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    out.write(tmpBuf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
        }
    }
}

package com.ccreanga.webserver.repository;

import java.io.File;
import java.io.InputStream;

/**
 * The FileManager class handles file operations. In future it should deal with consistency problems - reading while updating etc.
 */
public class FileManager {

    private static final FileManager manager = new FileManager();

    private FileManager(){}

    public static FileManager getInstance(){
        return manager;
    }

    public File getFile(String file){
        if (file.contains("../")) {
            throw new ForbiddenException("../ is not allowed");
        }
        return new File(file);
    }
    public void createFile(String file, InputStream in){

    }
    public void updateFile(String file, InputStream in){

    }
    public void deleteFile(String file, InputStream in){

    }

    public static void main(String[] args) {
        File file = new File("../");
        System.out.println(file.list().length);
    }

}

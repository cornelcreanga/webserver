package com.ccreanga.webserver.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The FileManager class handles file operations. In future it should deal with consistency problems too- reading while updating etc.
 */
public class FileManager {

    private static final FileManager manager = new FileManager();

    private FileManager(){}

    public static FileManager getInstance(){
        return manager;
    }

    public File getFile(String fileName){
        if (fileName.contains("../")) {
            throw new ForbiddenException("../ is not allowed");
        }
        File file = new File(fileName);
        //if file is hidden/starts with . return not found. do not return forbidden! - don't allow someone to 'guess'
        if (!file.exists() || (isHidden(file)))
            throw new NotFoundException("can't find file "+fileName);

        if (!file.canRead())
            throw new ForbiddenException("can't read "+fileName);

        return file;
    }
    public void createFile(String file, InputStream in){

    }
    public void updateFile(String file, InputStream in){

    }
    public void deleteFile(String file, InputStream in){

    }

    private boolean isHidden(File file){
        return file.isHidden() || file.getName().charAt(0)=='.';
    }

}

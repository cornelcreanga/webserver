package com.ccreanga.webserver.repository;

import com.ccreanga.webserver.InternalException;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileManager {

    private static final FileManager manager = new FileManager();

    private FileManager() {
    }

    public static FileManager getInstance() {
        return manager;
    }

    public File getFile(String fileName) {
        Preconditions.checkNotNull(fileName);
        if (fileName.contains("../")) {
            throw new ForbiddenException("../ is not allowed");
        }
        File file = new File(fileName);
        //if file is hidden/starts with . return not found. do not return forbidden! - don't allow someone to 'guess'
        if (!file.exists() || (isHidden(file)))
            throw new NotFoundException("can't find file " + fileName);

        if (!file.canRead())
            throw new ForbiddenException("can't read " + fileName);

        return file;
    }

    public List<File> getFolderContent(File folder) {
        Preconditions.checkNotNull(folder);
        if (!folder.isDirectory())
            throw new InternalException("file " + folder.getName() + " is not a folder");
        File[] file = folder.listFiles();
        if (file == null)//this should not happen unless some I/O issue
            throw new InternalException("cannot list folder " + folder.getName());
        return Arrays.stream(file).filter(f->isNotHidden(f) && f.canRead()).collect(Collectors.toList());
    }

//    public void createFile(String file, InputStream in){
//
//    }
//    public void updateFile(String file, InputStream in){
//
//    }
//    public void deleteFile(String file, InputStream in){
//
//    }

    private boolean isHidden(File file) {
        //file.getName().isEmpty() is true for /
        return file.isHidden() || file.getName().isEmpty() || file.getName().charAt(0) == '.';
    }

    private boolean isNotHidden(File file) {
        return !isHidden(file);
    }

}

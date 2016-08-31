package com.ccreanga.webserver.repository;

import com.ccreanga.webserver.InternalException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles file operations. For the moment only the retrieval operation are implemented.
 */
public class FileManager {

    private static final FileManager manager = new FileManager();

    private FileManager() {
    }

    public static FileManager getInstance() {
        return manager;
    }

    /**
     * Returns a file. The file name cannot contain two dots. It will
     *
     * @param fileName - filename, cannot be null
     * @return file
     * @throws ForbiddenException - it the file name contains two dots
     * @throws NotFoundException  - if the file does not exists/it is hidden/it starts with a dot/does not have read permissions
     */
    public File getFile(String fileName) {
        if (fileName.contains("../")) {
            throw new ForbiddenException("../ is not allowed");
        }
        File file = new File(fileName);
        //if file is hidden/starts with . return not found. do not return forbidden! - don't allow someone to 'guess' if a file exists or not
        if (!file.exists() || (isHidden(file)))
            throw new NotFoundException("can't find file " + fileName);
        //if file does not have read rights return not found. do not return forbidden! - don't allow someone to 'guess' if a file exists or not
        if (!file.canRead())
            throw new NotFoundException("can't read " + fileName);

        return file;
    }

    /**
     * Get a folder content.
     *
     * @param folder - folder, cannot be null
     * @return - all the folder content except the hidden files/files without read permission
     * @throws IOException - i/o exception
     */
    public List<File> getFolderContent(File folder) throws IOException {
        if (!folder.isDirectory())
            throw new InternalException("file " + folder.getName() + " is not a folder");
        File[] file = folder.listFiles();
        if (file == null)//this should not happen unless some serious I/O issue
            throw new IOException("cannot list folder " + folder.getName());
        return Arrays.stream(file).filter(f -> isNotHidden(f) && f.canRead()).collect(Collectors.toList());
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

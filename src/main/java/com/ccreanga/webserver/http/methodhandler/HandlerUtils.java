package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.ioutil.FileUtil;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;

import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT;
import static com.ccreanga.webserver.http.HttpHeaders.HOST;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeErrorResponse;

public class HandlerUtils {

    public static final Logger serverLog = Logger.getLogger("serverLog");

    public static boolean hostHeaderIsPresent(HttpRequestMessage request, OutputStream out, HttpHeaders responseHeaders) throws IOException {
        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((!request.hasHeader(HOST)) && (request.isHTTP1_1())) {//host is mandatory
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "missing host header", out);
            return false;
        }
        return true;
    }

    public static boolean contentTypeIsFormRelated(HttpRequestMessage request, OutputStream out, HttpHeaders responseHeaders, String contentType) throws IOException {
        if (contentType.contains("application/x-www-form-urlencoded")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
            return true;
        }

        if (contentType.contains("multipart/form-data")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
            return true;
        }
        return false;
    }

    public static boolean rootFolderIsWritable(HttpRequestMessage request, Configuration cfg, OutputStream out, HttpHeaders responseHeaders) throws IOException {
        if (!cfg.isRootFolderWritable()) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "the root folder is configured read only", out);
            return false;
        }
        return true;
    }

    public static boolean uriContainsIllegalPath(HttpRequestMessage request, OutputStream out, HttpHeaders responseHeaders) throws IOException {
        if (request.getUri().contains("..")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.FORBIDDEN, ".. is not allowed", out);
            return true;
        }
        return false;
    }

    public static boolean createFolderHierarchy(HttpRequestMessage request, OutputStream out, HttpHeaders responseHeaders,  Path path) throws IOException {
        try {
            if (!path.getParent().toFile().exists()) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", cannot mkdirs for " + path.getParent()+",error is "+e.getMessage());
            if (e.getMessage().contains("File name too long"))
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "uri name too long", out);
            else//todo - maybe refine it?
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot create resource", out);
            return false;
        }
        return true;
    }

    public static MessageDigest instantiateMD5(HttpRequestMessage request, OutputStream out, HttpHeaders responseHeaders) throws IOException{
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            serverLog.severe("Connection " + ContextHolder.get().getUuid() + ", message is " + e.getMessage());
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR, "cannot create resource", out);
            return null;
        }
    }

    public static boolean copyRequestBody(HttpRequestMessage request,
                                          OutputStream responseOutStream,
                                          HttpHeaders responseHeaders,
                                          MessageDigest md,
                                          boolean append,
                                          File outFile) throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(outFile,append)) {
            try {
                outputStream.getChannel().lock();
            }catch (OverlappingFileLockException e){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.LOCKED, "locked resource", responseOutStream);
                return false;
            }
            if (!request.isChunked())
                IOUtil.copy(request.getBody(), outputStream, 0, request.getLength(),8192,md);
            else
                IOUtil.copy(request.getBody(), outputStream,-1,-1,8192,md);
        } catch (IOException e) {
            serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
            boolean removed = outFile.delete();
            if (!removed)
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", can't remove " + outFile.getPath());

            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot modify resource", responseOutStream);//todo - refine
            return false;
        }
        return true;
    }

    public static boolean renameTemporaryToMainFile(HttpRequestMessage request, OutputStream out, HttpHeaders responseHeaders, File file, File tempFile) throws IOException {

        if (!file.exists()){
            boolean renamed = tempFile.renameTo(file);
            if (renamed)
                return true;
        }else {

            File intermediate = new File(file.getParentFile() + File.separator + "." + UUID.randomUUID());
            String fileName = file.getPath();
            boolean renamed = file.renameTo(intermediate);
            File newFile = new File(fileName);
            if (renamed) {
                renamed = tempFile.renameTo(newFile);
                if (renamed) {
                    boolean deleted = intermediate.delete();
                    if (!deleted)
                        serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", file " + intermediate + " can't be deleted");
                    return true;
                } else {//try to rename
                    renamed = file.renameTo(newFile);
                    if (!renamed)
                        serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", file " + intermediate + " can't be renamed to " + fileName);
                    else
                        return true;
                }
            }
        }

        FileUtil.removeMd5(file);
        serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", can't rename " + tempFile);
        writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
        return false;
    }

}

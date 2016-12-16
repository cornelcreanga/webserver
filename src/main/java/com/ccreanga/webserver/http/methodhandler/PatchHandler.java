package com.ccreanga.webserver.http.methodhandler;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.common.StringUtil;
import com.ccreanga.webserver.http.HttpHeaders;
import com.ccreanga.webserver.http.HttpRequestMessage;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.ioutil.FileUtil;
import com.ccreanga.webserver.ioutil.IOUtil;
import com.ccreanga.webserver.logging.ContextHolder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeErrorResponse;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;

public class PatchHandler implements HttpMethodHandler {

    public static final Logger serverLog = Logger.getLogger("serverLog");

    @Override
    public void handleResponse(HttpRequestMessage request, Configuration cfg, OutputStream out) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");

        //http://www8.org/w8-papers/5c-protocols/key/key.html
        if ((!request.hasHeader(HOST)) && (request.isHTTP1_1())) {//host is mandatory
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "missing host header", out);
            return;
        }

        if (request.getUri().contains("..")) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.FORBIDDEN, ".. is not allowed", out);
            return;
        }


        if (!cfg.isRootFolderWritable()) {
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "the root folder is configured read only", out);
            return;
        }

        String uri = request.getUri();
        Path path = Paths.get(cfg.getServerRootFolder() + uri);
        File file = path.toFile();

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            serverLog.severe("Connection " + ContextHolder.get().getUuid() + ", message is " + e.getMessage());
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR, "cannot create resource", out);
            return;
        }


        String command = request.getHeader("X-UPDATE");
        if ((command!=null) && (command.startsWith("APPEND"))){
            if ((file.exists()) && (file.isDirectory())){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "cannot patch a folder", out);
                return;
            }
            try {
                if (!path.getParent().toFile().exists()) {
                    Files.createDirectories(path.getParent());
                }
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", cannot mkdirs for " + uri);
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot create resource", out);
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(file,true)) {
                if (!request.isChunked())
                    IOUtil.copy(request.getBody(), outputStream, 0, request.getLength(),8192,md);
                else
                    IOUtil.copy(request.getBody(), outputStream,-1,-1,8192,md);
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

        }else if ((command!=null) && (command.startsWith("INSERT"))){
            if (!file.exists()){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.NOT_FOUND, "not found", out);
                return;
            }

            if ((file.exists()) && (file.isDirectory())){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "cannot patch a folder", out);
                return;
            }
            long index;
            try{
                index = Long.parseLong(StringUtil.right(command,' '));
            }catch (NumberFormatException nfe){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "index for INSERT can't be parsed", out);
                return;
            }

            if ((index<0) || (index>file.length())){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "illegal value for index", out);
                return;
            }

            String tempFileName = file.getPath()+"-temp";

            try(FileInputStream inStream = new FileInputStream(file);FileOutputStream outStream = new FileOutputStream(tempFileName)){
                IOUtil.copy(inStream,outStream,0,index,8192,md);
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

            try (FileOutputStream outStream = new FileOutputStream(tempFileName,true)) {
                if (!request.isChunked())
                    IOUtil.copy(request.getBody(), outStream, 0, request.getLength(),8192,md);
                else
                    IOUtil.copy(request.getBody(), outStream,-1,-1,8192,md);
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

            try(FileInputStream inStream = new FileInputStream(file);FileOutputStream outStream = new FileOutputStream(tempFileName,true)){
                IOUtil.copy(inStream,outStream,index,file.length()-index,8192,md);
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

            Files.delete(file.toPath());//todo
            boolean renamed = new File(tempFileName).renameTo(file);

            if (!renamed){
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", can't rename " + tempFileName);
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

        }else if (((command!=null) && (command.startsWith("REMOVE")))){
            if (!file.exists()){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.NOT_FOUND, "not found", out);
                return;
            }

            if ((file.exists()) && (file.isDirectory())){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "cannot patch a folder", out);
                return;
            }

            String paramLine = command.substring("REMOVE".length());
            List<String> params = StringUtil.split(paramLine,' ',false,10);

            if ((params.size()==0) || (params.size()>2)){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "REMOVE can't be parsed", out);
                return;
            }
            long from, length=-1;
            try{
                from = Long.parseLong(params.get(0));
                if (params.size()==2)
                    length = Long.parseLong(params.get(1));
            }catch (NumberFormatException nfe){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "params for REMOVE can't be parsed", out);
                return;
            }

            if ((from<0) || (from>file.length()) || (from+length)>file.length()){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "params for REMOVE are invalid", out);
                return;
            }

            String tempFileName = file.getPath()+"-temp";

            try(FileInputStream inStream = new FileInputStream(file);FileOutputStream outStream = new FileOutputStream(tempFileName)){
                IOUtil.copy(inStream,outStream,0,from,8192,md);
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

            if (length!=-1){
                try(FileInputStream inStream = new FileInputStream(file);FileOutputStream outStream = new FileOutputStream(tempFileName,true)){
                    IOUtil.copy(inStream,outStream,from+length,file.length()-from-length,8192,md);
                } catch (IOException e) {
                    serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                    writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                    return;
                }

            }

            Files.delete(file.toPath());//todo
            boolean renamed = new File(tempFileName).renameTo(file);

            if (!renamed){
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", can't rename " + tempFileName);
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

        }else{
            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "unknown header X-UPDATE "+command, out);
            return;
        }

        FileUtil.createMD5file(file,md);
        writeResponseLine(HttpStatus.NO_CONTENT, out);

        writeHeaders(responseHeaders, out);
        ContextHolder.get().setContentLength("-");

        //append (body)
        //remove <start> <end>
        //insert <start> (body)
        //

    }
}

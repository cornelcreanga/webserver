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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.logging.Logger;

import static com.ccreanga.webserver.common.DateUtil.FORMATTER_RFC822;
import static com.ccreanga.webserver.http.HttpHeaders.*;
import static com.ccreanga.webserver.http.HttpHeaders.ACCEPT;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeErrorResponse;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeHeaders;
import static com.ccreanga.webserver.http.HttpMessageWriter.writeResponseLine;
import static com.ccreanga.webserver.http.methodhandler.HandlerUtils.*;

public class PatchHandler implements HttpMethodHandler {

    public static final Logger serverLog = Logger.getLogger("serverLog");

    @Override
    public void handleResponse(HttpRequestMessage request, Configuration cfg, OutputStream out) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        String currentDate = DateUtil.currentDate(FORMATTER_RFC822);
        responseHeaders.putHeader(DATE, currentDate.replace("UTC", "GMT"));
        responseHeaders.putHeader(CONNECTION, "Keep-Alive");
        responseHeaders.putHeader(VARY, "Accept-Encoding");

        if (!hostHeaderIsPresent(request, out, responseHeaders)) return;

        if (uriContainsIllegalPath(request, out, responseHeaders)) return;

        if (!rootFolderIsWritable(request, cfg, out, responseHeaders)) return;

        String uri = request.getUri();
        Path path = Paths.get(cfg.getServerRootFolder() + uri);
        File file = path.toFile();

        MessageDigest md = instantiateMD5(request,out,responseHeaders);
        if (md==null)
            return;

        String command = request.getHeader("X-UPDATE");
        if ((command!=null) && (command.startsWith("APPEND"))){
            if ((file.exists()) && (file.isDirectory())){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "cannot patch a folder", out);
                return;
            }

            if (!createFolderHierarchy(request, out, responseHeaders, path)) return;

            String tempFileName = file.getPath()+"-temp";
            File tempFile = new File(tempFileName);

            if (!copyRequestBody(request, out, responseHeaders, md,true, tempFile)) return;

            FileUtil.removeMd5(file);
            try(FileInputStream inStream = new FileInputStream(tempFile);FileOutputStream outStream = new FileOutputStream(file,true)){
                IOUtil.copy(inStream,outStream);
            }catch (IOException e) {
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
            File tempFile = new File(tempFileName);

            try(FileInputStream inStream = new FileInputStream(file);FileOutputStream outStream = new FileOutputStream(tempFileName)){
                IOUtil.copy(inStream,outStream,0,index,8192,md);
            } catch (IOException e) {
                tempFile.delete();
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

            if (!copyRequestBody(request, out, responseHeaders, md,true, tempFile)) return;

            try(FileInputStream inStream = new FileInputStream(file);FileOutputStream outStream = new FileOutputStream(tempFileName,true)){
                IOUtil.copy(inStream,outStream,index,file.length()-index,8192,md);
            } catch (IOException e) {
                serverLog.warning("Connection " + ContextHolder.get().getUuid() + ", message " + e.getMessage());
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.SERVICE_UNAVAILABLE, "cannot patch resource", out);//todo - refine
                return;
            }

            FileUtil.removeMd5(file);
            if (!renameTemporaryToMainFile(request, out, responseHeaders, file, tempFile)) return;

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
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "REMOVE parameters can't be parsed", out);
                return;
            }
            long from, length=-1;
            try{
                from = Long.parseLong(params.get(0));
                if (params.size()==2)
                    length = Long.parseLong(params.get(1));
            }catch (NumberFormatException nfe){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "REMOVE parameters can't be parsed", out);
                return;
            }

            if ((from<0) || (from>file.length()) || (from+length)>file.length()){
                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "params for REMOVE are invalid", out);
                return;
            }

            String tempFileName = file.getPath()+"-temp";
            File tempFile = new File(tempFileName);

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
            FileUtil.removeMd5(file);
            if (!renameTemporaryToMainFile(request, out, responseHeaders, file, tempFile)) return;

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

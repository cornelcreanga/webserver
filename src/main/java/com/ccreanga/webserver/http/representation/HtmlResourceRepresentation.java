package com.ccreanga.webserver.http.representation;

import com.ccreanga.webserver.Configuration;
import com.ccreanga.webserver.InternalException;
import com.ccreanga.webserver.common.DateUtil;
import com.ccreanga.webserver.common.StringUtil;
import com.ccreanga.webserver.http.HttpStatus;
import com.ccreanga.webserver.http.Mime;
import com.ccreanga.webserver.ioutil.FileUtil;
import com.ccreanga.webserver.repository.FileManager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.time.Instant;
import java.util.List;

import static com.ccreanga.webserver.common.NumberUtil.fileSizePretty;
import static com.ccreanga.webserver.ioutil.IOUtil.extractParentResource;

/**
 * Used to generate an html representation for a folder or in case of an error (like Apache does).
 */
public class HtmlResourceRepresentation implements FileResourceRepresentation {

    @Override
    public String folderRepresentation(File folder, File root) throws IOException {

        StringBuilder sb = new StringBuilder(1024);
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head>");
        sb.append("<title>Index of ").append(StringUtil.escapeHTML(folder.getName())).append("</title>");
        sb.append("<head><body>");
        sb.append("<h1>Index of ").append(StringUtil.escapeHTML(folder.getName())).append("</h1>");
        sb.append("<table><tr>");
        sb.append("<th>Name</th>");
        sb.append("<th>Last modified</th>");
        sb.append("<th>Size</th>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<th colspan=\"3\">");
        sb.append("<hr>");
        sb.append("</th>");
        sb.append("</tr>");

        sb.append("");
        sb.append("");

        if (!root.equals(folder)) {
            sb.append("<tr><td>");
            sb.append("<a href=\"").append(extractParentResource(folder, root)).append("\">Go to Parent Directory </a>");
            sb.append("</td><td align=\"right\">&nbsp;</td><td align=\"right\">&nbsp;</td><tr>");
        }


        FileManager.getInstance().getFolderContent(folder).
                stream().
                sorted((f1, f2) -> {//first directories, after that the files
                    if ((f1.isDirectory()) && (f2.isFile()))
                        return -1;
                    if ((f1.isFile()) && (f2.isDirectory()))
                        return 1;
                    return f1.compareTo(f2);
                }).
                forEach(file -> {

                    sb.append("<tr><td>");
                    sb.append("<a href=\"").append(encodeUrl(file.getName()) + (file.isDirectory() ? "/" : "")).append("\">").append(StringUtil.escapeHTML(file.getName()) + (file.isDirectory() ? "/" : "")).append("</a>");
                    sb.append("</td>");
                    sb.append("<td align=\"right\">").append(DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT)).append("</td>");
                    sb.append("<td align=\"right\">").append(file.isDirectory() ? "-" : fileSizePretty(file.length())).append("</td>");
                    sb.append("</tr>");

                });

        sb.append("<tr>");
        sb.append("<th colspan=\"3\"><hr></th>");
        sb.append("</tr>");
        sb.append("</table></body></html>");

        return sb.toString();
    }

    private String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public String errorRepresentation(HttpStatus status, String extendedReason) throws IOException {
        StringBuilder sb = new StringBuilder(256);
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>").append(status.value()).append("-").append(status.getReasonPhrase()).append("</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>").append(status.getReasonPhrase()).append("</h1>");
        if (extendedReason.length() > 0)
            sb.append("<p>").append(extendedReason).append("</p>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public String nonDeletedFiles(List<File> files) {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("<!DOCTYPE html>");
        sb.append("<html><head>");
        sb.append("</head><body>");

        sb.append("<table><tr>");
        sb.append("<th>Name</th>");
        sb.append("<th>Last modified</th>");
        sb.append("<th>Size</th>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<th colspan=\"3\">");
        sb.append("<hr>");
        sb.append("</th>");
        sb.append("</tr>");
        for (File file : files) {
            sb.append("<tr><td>");
            sb.append(StringUtil.escapeHTML(file.getName()));//todo
            sb.append("</td>");
            sb.append("<td align=\"right\">").append(DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_SHORT)).append("</td>");
            sb.append("<td align=\"right\">").append(file.isDirectory() ? "-" : fileSizePretty(file.length())).append("</td>");
            sb.append("</tr>");

        }
        sb.append("<tr>");
        sb.append("<th colspan=\"3\"><hr></th>");
        sb.append("</tr>");
        sb.append("</table></body></html>");
        sb.append("<tr>");
        sb.append("<th colspan=\"3\"><hr></th>");
        sb.append("</tr>");
        sb.append("</table></body></html>");

        return sb.toString();
    }

    @Override
    public String getContentType() {
        return Mime.getType("html");
    }

    @Override
    public String getFileInfo(File file, Configuration cfg, boolean extended) throws IOException {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<table>");

        sb.append("<tr>");
        sb.append("<td>Name</td>");
        sb.append("<td>"+StringUtil.escapeHTML(file.getName())+"</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Last modified</td>");
        sb.append("<td>"+DateUtil.formatDateToUTC(Instant.ofEpochMilli(file.lastModified()), DateUtil.FORMATTER_LONG)+"</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Size</td>");
        sb.append("<td>"+file.length()+"</td>");
        sb.append("</tr>");

        if (extended){
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            sb.append("<tr>");
            sb.append("<td>Creation time</td>");
            sb.append("<td>"+attr.creationTime()+"</td>");
            sb.append("</tr>");

            if (cfg.isRootFolderWritable()) {
                sb.append("<tr>");
                sb.append("<td>MD5</td>");
                sb.append("<td>" + FileUtil.getOrCreateMd5AsHex(file) + "</td>");
                sb.append("</tr>");
            }


            FileStore store = Files.getFileStore(file.toPath());
            if (store.supportsFileAttributeView(UserDefinedFileAttributeView.class)) {
                UserDefinedFileAttributeView view = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
                for (String name: view.list()) {
                    sb.append("<tr>");
                    sb.append("<td>").append(name).append("</td>");
                    sb.append("<td>");
                    int size = view.size(name);
                    ByteBuffer buf = ByteBuffer.allocateDirect(size);
                    view.read(name, buf);
                    buf.flip();
                    sb.append(Charset.defaultCharset().decode(buf).toString());
                    sb.append("</td>");
                    sb.append("</tr>");
                }
            }

        }

        sb.append("</table>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }


}

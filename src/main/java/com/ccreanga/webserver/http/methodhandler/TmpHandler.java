package com.ccreanga.webserver.http.methodhandler;

public class TmpHandler {
    //        if (contentType.contains("application/x-www-form-urlencoded")) {
//            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
//            return;
//            String form = IOUtil.readToken(in, -1, "UTF-8", 8 * 1024 * 1024);
//            int limit = 10000;//max 10000 params -todo -config that
//            try {
//                params = StringUtil.parseFormEncodedParams(form, limit);
//            } catch (TooManyEntriesException e) {
//                throw new InvalidMessageException("too many form params", HttpStatus.BAD_REQUEST);
//            }

//        }

//        if (contentType.contains("multipart/form-data")) {
//            writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data is not allowed", out);
//            return;

//            int index = contentType.indexOf("boundary");
//            if (index == -1) {
//                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "multipart/form-data without boundary", out);
//                return;
//            }
//            String boundary = StringUtil.right(contentType.substring(index + 1), '=');
//            if (boundary.length() == 0) {
//                writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "boundary value is missing", out);
//                return;
//            }
//            MultipartStream stream = new MultipartStream(request.getBody(), boundary.getBytes());
//            boolean nextPart = stream.skipPreamble();
//            Map<String, List<String>> params = new HashMap<>();
//            while (nextPart) {
//                String header = stream.readHeaders();
//                int maxLineLength = cfg.getRequestMaxLineLength();
//                int maxHeaders = cfg.getRequestMaxHeaders();
//                HttpHeaders httpHeaders = HttpRequestParser.consumeHeaders(new ByteArrayInputStream(header.getBytes("UTF-8")), maxLineLength, maxHeaders);
//                Map<String, String> headerParams = httpHeaders.getHeaderParams("Content-Disposition");
//
//                String name = headerParams.get("name");
//                if (name == null || name.length() == 0) {//bad request
//                    writeErrorResponse(request.getHeader(ACCEPT), responseHeaders, HttpStatus.BAD_REQUEST, "missing/empty name parameter", out);
//                    return;
//                }
//                String filename = headerParams.get("filename");
//                if (filename == null) {
//                    params.putIfAbsent(name, new ArrayList<>());
//                    List<String> values = params.get(name);
//                    ByteArrayOutputStream valueOut = new ByteArrayOutputStream();
//                    stream.readBodyData(valueOut);
//                    values.add(new String(valueOut.toByteArray()));
//                } else {
//                    System.out.println(filename);
//                    stream.readBodyData(System.out);
//                    //todo - save the files somewhere
//                }
//
//                nextPart = stream.readBoundary();
//            }
//            if (request.isChunked()) {//we need to consume the ending 3 bytes of the chunked input stream
//                while (request.getBody().read() > 0) ;
//            }

//        }

}

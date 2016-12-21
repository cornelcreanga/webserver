Java  Web server
===================
---------------

Simple Web server delivering static content and allowing file uploads, implemented mostly for fun/exercise. It has very few external dependencies (only for logging and template processing)
 
It supports GET/HEAD/POST/PUT/DELETE/PATCH/OPTIONS/TRACE/CONNECT methods. It is a simple multithreaded Java server using a synchronous I/O single connection per thread model.
 
HTTP 0.9 is not supported - only HTTP 1.0 and HTTP 1.1. The RFC implementation is work in progress (see below).

Requirements
------------
To compile the project: Java 1.8, Maven version > 3. 
mvn clean package will create a "fat" jar named webserver.jar; to run it type java -jar webserver.jar <server.properties>
if you don't provide a property file the default values from the Configuration section will be used

Configuration
=======
The following properties can be configured

| Property   |      Description      |  Default value |
|----------|:-------------:|------:|
|serverPort |  server port  | 8082 |
|serverRootFolder |    document root folder   |    /var/www/html |
|serverInitialThreads | Initial no of threads, it should belong in [1..1024] |    128 |
|serverMaxThreads |Max no of threads, it should belong in [1..1024]  |    1000 |
|requestTimeoutSeconds |Timeout for keep-alive connections, it should belong in [1..3600]    |5  |
|requestWaitingQueueSize | Thread pool waiting queue, it should belong in [1..3600]   |64  |
|requestEtag | Indicates if the server should generate etags - Right now it only supports two values: none/weak (strong is not yet implemented)   | weak |
|rootFolderWritable| Is the root folder writable? It makes sense for POST/PUT/DELETE/PATCH methods||
|requestMessageBodyMaxSize | Maximum amount of request body size. Makes sense for PUT/POST/PATCH requests. It should belong in [1048576..17179869184]   |200  |
|requestMaxLineLength | The maximum number of lines from a request, it should belong in [256..65535]   |1024  |
|requestMaxHeaders  | The maximum amount of headers, it should belong in [8..65535]   | 64 |
|verbose  | If true the server will display debug information   | false |


Features
=======
| Name   |      Implemented      |  Notes
|----------|:-------------:|------:|
|Supported methods|GET, HEAD, POST, PUT, DELETE, PATCH, OPTIONS, TRACE, CONNECT||
|Robust request parsing |  yes  | It should handle properly legacy request/invalid requests |
|Scalability|average |1 thread per socket|
|Keep alive connection|yes|Both HTTP 1.0 and HTTP 1.1|
|Chunked streaming|yes|supported for both request and response|
|HTTP conditionals|yes| |
|Ranged requests|yes| |
|Etag generation|yes|based on file last modification date|
|Access log|yes|Will be generated in the main folder, location can't be customized for the moment|
|Compressed content|yes|only for HTTP 1.1 and only gzip and deflate|
|Directory indexing|yes|HTML or JSON depending on the ACCEPT header|
|Directory index file|no|planned|
|Virtual hosting|no|planned|
|Secure connections support|no|planned|
|Rate limiting|no|planned|
|Authentication/Authorization|no|planned|
|Multipart support|no|planned|
<!--[![Build Status](https://travis-ci.org/cornelcreanga/webserver.svg)](https://travis-ci.org/cornelcreanga/webserver)-->


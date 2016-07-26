package com.ccreanga.webserver.http;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HTTPHeaders {


    /**
     REQUEST

     Accept-Charset	Character sets that are acceptable	Accept-Charset: utf-8
     Accept-Encoding	Acceptable encodings. See HTTP compression.	Accept-Encoding: <compress | gzip | deflate | sdch | identity>
     Accept-Language	Acceptable languages for response	Accept-Language: en-US
     Authorization	Authentication credentials for HTTP authentication	Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     Cache-Control	Used to specify directives that MUST be obeyed by all caching mechanisms along the request/response chain	Cache-Control: no-cache
     Connection	What type of connection the user-agent would prefer	Connection: close
     Cookie	an HTTP cookie previously sent by the server with Set-Cookie (below)	Cookie: $Version=1; Skin=new;
     Content-Length	The length of the request body in octets (8-bit bytes)	Content-Length: 348
     Content-MD5	A Base64-encoded binary MD5 sum of the content of the request body	Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
     Content-Type	The mime type of the body of the request (used with POST and PUT requests)	Content-Type: application/x-www-form-urlencoded
     Date	The date and time that the message was sent	Date: Tue, 15 Nov 1994 08:12:31 GMT
     Expect	Indicates that particular server behaviors are required by the client	Expect: 100-continue
     From	The email address of the user making the request	From: user@example.com
     Host	The domain name of the server (for virtual hosting), mandatory since HTTP/1.1	Host: en.wikipedia.org
     If-Match	Only perform the action if the client supplied entity matches the same entity on the server. This is mainly for methods like PUT to only update a resource if it has not been modified since the user last updated it.	If-Match: "737060cd8c284d8af7ad3082f209582d"
     If-Modified-Since	Allows a 304 Not Modified to be returned if content is unchanged	If-Modified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     If-None-Match	Allows a 304 Not Modified to be returned if content is unchanged, see HTTP ETag	If-None-Match: "737060cd8c284d8af7ad3082f209582d"
     If-Range	If the entity is unchanged, send me the part(s) that I am missing; otherwise, send me the entire new entity	If-Range: "737060cd8c284d8af7ad3082f209582d"
     If-Unmodified-Since	Only send the response if the entity has not been modified since a specific time.	If-Unmodified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     Max-Forwards	Limit the number of times the message can be forwarded through proxies or gateways.	Max-Forwards: 10
     Pragma	Implementation-specific headers that may have various effects anywhere along the request-response chain.	Pragma: no-cache
     Proxy-Authorization	Authorization credentials for connecting to a proxy.	Proxy-Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     Range	Request only part of an entity. Bytes are numbered from 0.	Range: bytes=500-999
     Referer[sic]	This is the address of the previous web page from which a link to the currently requested page was followed. (The word “referrer” is misspelled in the RFC as well as in most implementations.)	Referer: http://en.wikipedia.org/wiki/Main_Page
     TE	The transfer encodings the user agent is willing to accept: the same values as for the response header Transfer-Encoding can be used, plus the "trailers" value (related to the "chunked" transfer method) to notify the server it accepts to receive additional headers (the trailers) after the last, zero-sized, chunk.	TE: trailers, deflate
     Upgrade	Ask the server to upgrade to another protocol.	Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11
     User-Agent	The user agent string of the user agent	User-Agent: Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)
     Via	Informs the server of proxies through which the request was sent.	Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
     Warning	A general warning about possible problems with the entity body.

     X-Requested-With	mainly used to identify Ajax requests. Most JavaScript frameworks send this header with value of XMLHttpRequest	X-Requested-With: XMLHttpRequest
     X-Do-Not-Track	Requests a web application to disable their tracking of a user. Note that, as of yet, this is largely ignored by web applications. It does however open the door to future legislation requiring web applications to comply with a user's request to not be tracked. Mozilla implements the DNT header with a similar purpose.	X-Do-Not-Track: 1
     DNT Requests a web application to disable their tracking of a user. This is Mozilla's version of the X-Do-Not-Track header (since Firefox 4.0 Beta 11). Safari and IE9 also have support for this header.[10] On March 7, 2011, a draft proposal was submitted to IETF.


     Accept-Ranges	What partial content range types this server supports	Accept-Ranges: bytes
     Age	The age the object has been in a proxy cache in seconds	Age: 12
     Allow	Valid actions for a specified resource. To be used for a 405 Method not allowed	Allow: GET, HEAD
     Cache-Control	Tells all caching mechanisms from server to client whether they may cache this object. It is measured in seconds	Cache-Control: max-age=3600
     Connection	Options that are desired for the connection[4]	Connection: close
     Content-Encoding	The type of encoding used on the data. See HTTP compression.	Content-Encoding: gzip
     Content-Language	The language the content is in	Content-Language: da
     Content-Length	The length of the response body in octets (8-bit bytes)	Content-Length: 348
     Content-Location	An alternate location for the returned data	Content-Location: /index.htm
     Content-MD5	A Base64-encoded binary MD5 sum of the content of the response	Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
     Content-Disposition	An opportunity to raise a "File Download" dialogue box for a known MIME type	Content-Disposition: attachment; filename=fname.ext
     Content-Range	Where in a full body message this partial message belongs	Content-Range: bytes 21010-47021/47022
     Content-Type	The mime type of this content	Content-Type: text/html; charset=utf-8
     Date	The date and time that the message was sent	Date: Tue, 15 Nov 1994 08:12:31 GMT
     ETag	An identifier for a specific version of a resource, often a message digest	ETag: "737060cd8c284d8af7ad3082f209582d"
     Expires	Gives the date/time after which the response is considered stale	Expires: Thu, 01 Dec 1994 16:00:00 GMT
     Last-Modified	The last modified date for the requested object, in RFC 2822 format	Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
     Link	Used to express a typed relationship with another resource, where the relation type is defined by RFC 5988	Link: </feed>; rel="alternate"
     Location	Used in redirection, or when a new resource has been created.	Location: http://www.w3.org/pub/WWW/People.html
     P3P	This header is supposed to set P3P policy, in the form of P3P:CP="your_compact_policy". However, P3P did not take off,[5] most browsers have never fully implemented it, a lot of websites set this header with fake policy text, that was enough to fool browsers the existence of P3P policy and grant permissions for third party cookies.	P3P: CP="This is not a P3P policy! See http://www.google.com/support/accounts/bin/answer.py?hl=en&answer=151657 for more info."
     Pragma	Implementation-specific headers that may have various effects anywhere along the request-response chain.	Pragma: no-cache
     Proxy-Authenticate	Request authentication to access the proxy.	Proxy-Authenticate: Basic
     Refresh	Used in redirection, or when a new resource has been created. This refresh redirects after 5 seconds. This is a proprietary, non-standard header extension introduced by Netscape and supported by most web browsers.	Refresh: 5; url=http://www.w3.org/pub/WWW/People.html
     Retry-After	If an entity is temporarily unavailable, this instructs the client to try again after a specified period of time.	Retry-After: 120
     Server	A name for the server	Server: Apache/1.3.27 (Unix) (Red-Hat/Linux)
     Set-Cookie	an HTTP cookie	Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
     Strict-Transport-Security	A HSTS Policy informing the HTTP client how long to cache the HTTPS only policy and whether this applies to subdomains.	Strict-Transport-Security: max-age=16070400; includeSubDomains
     Trailer	The Trailer general field value indicates that the given set of header fields is present in the trailer of a message encoded with chunked transfer-coding.	Trailer: Max-Forwards
     Transfer-Encoding	The form of encoding used to safely transfer the entity to the user. Currently defined methods are: chunked, compress, deflate, gzip, identity.	Transfer-Encoding: chunked
     Vary	Tells downstream proxies how to match future request headers to decide whether the cached response can be used rather than requesting a fresh one from the origin server.	Vary: *
     Via	Informs the client of proxies through which the response was sent.	Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
     Warning	A general warning about possible problems with the entity body.	Warning: 199 Miscellaneous warning
     WWW-Authenticate	Indicates the authentication scheme that should be used to access the requested entity.	WWW-Authenticate: Basic

     X-Frame-Options[12]	Clickjacking protection: "deny" - no rendering within a frame, "sameorigin" - no rendering if origin mismatch	X-Frame-Options: deny
     X-XSS-Protection[13]	Cross-site scripting (XSS) filter	X-XSS-Protection: 1; mode=block
     X-Content-Type-Options[14]	the only defined value, "nosniff", prevents Internet Explorer from MIME-sniffing a response away from the declared content-type	X-Content-Type-Options: nosniff
     X-Forwarded-For[15]	a de facto standard for identifying the originating IP address of a client connecting to a web server through an HTTP proxy or load balancer	X-Forwarded-For: client1, proxy1, proxy2
     X-Forwarded-Proto[16]	a de facto standard for identifying the originating protocol of an HTTP request, since a reverse proxy (load balancer) communicates with a web server using HTTP	X-Forwarded-Proto: https
     X-Powered-By[17]	specifies the technology (ASP.NET, PHP, JBoss, e.g.) supporting the web application (version details are often in X-Runtime, X-Version, or X-AspNet-Version)	X-Powered-By: PHP/5.2.1
     */

    /**
     * RESPONSE
     * <p/>
     * Accept-Ranges	What partial content range types this server supports	Accept-Ranges: bytes
     * Age	The age the object has been in a proxy cache in seconds	Age: 12
     * Allow	Valid actions for a specified resource. To be used for a 405 Method not allowed	Allow: GET, HEAD
     * Cache-Control	Tells all caching mechanisms from server to client whether they may cache this object. It is measured in seconds	Cache-Control: max-age=3600
     * Connection	Options that are desired for the connection[4]	Connection: close
     * Content-Encoding	The type of encoding used on the data. See HTTP compression.	Content-Encoding: gzip
     * Content-Language	The language the content is in	Content-Language: da
     * Content-Length	The length of the response body in octets (8-bit bytes)	Content-Length: 348
     * Content-Location	An alternate location for the returned data	Content-Location: /index.htm
     * Content-MD5	A Base64-encoded binary MD5 sum of the content of the response	Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
     * Content-Disposition	An opportunity to raise a "File Download" dialogue box for a known MIME type	Content-Disposition: attachment; filename=fname.ext
     * Content-Range	Where in a full body message this partial message belongs	Content-Range: bytes 21010-47021/47022
     * Content-Type	The mime type of this content	Content-Type: text/html; charset=utf-8
     * Date	The date and time that the message was sent	Date: Tue, 15 Nov 1994 08:12:31 GMT
     * ETag	An identifier for a specific version of a resource, often a message digest	ETag: "737060cd8c284d8af7ad3082f209582d"
     * Expires	Gives the date/time after which the response is considered stale	Expires: Thu, 01 Dec 1994 16:00:00 GMT
     * Last-Modified	The last modified date for the requested object, in RFC 2822 format	Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
     * Link	Used to express a typed relationship with another resource, where the relation type is defined by RFC 5988	Link: </feed>; rel="alternate"
     * Location	Used in redirection, or when a new resource has been created.	Location: http://www.w3.org/pub/WWW/People.html
     * P3P	This header is supposed to set P3P policy, in the form of P3P:CP="your_compact_policy". However, P3P did not take off,[5] most browsers have never fully implemented it, a lot of websites set this header with fake policy text, that was enough to fool browsers the existence of P3P policy and grant permissions for third party cookies.	P3P: CP="This is not a P3P policy! See http://www.google.com/support/accounts/bin/answer.py?hl=en&answer=151657 for more info."
     * Pragma	Implementation-specific headers that may have various effects anywhere along the request-response chain.	Pragma: no-cache
     * Proxy-Authenticate	Request authentication to access the proxy.	Proxy-Authenticate: Basic
     * Refresh	Used in redirection, or when a new resource has been created. This refresh redirects after 5 seconds. This is a proprietary, non-standard header extension introduced by Netscape and supported by most web browsers.	Refresh: 5; url=http://www.w3.org/pub/WWW/People.html
     * Retry-After	If an entity is temporarily unavailable, this instructs the client to try again after a specified period of time.	Retry-After: 120
     * Server	A name for the server	Server: Apache/1.3.27 (Unix) (Red-Hat/Linux)
     * Set-Cookie	an HTTP cookie	Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
     * Strict-Transport-Security	A HSTS Policy informing the HTTP client how long to cache the HTTPS only policy and whether this applies to subdomains.	Strict-Transport-Security: max-age=16070400; includeSubDomains
     * Trailer	The Trailer general field value indicates that the given set of header fields is present in the trailer of a message encoded with chunked transfer-coding.	Trailer: Max-Forwards
     * Transfer-Encoding	The form of encoding used to safely transfer the entity to the user. Currently defined methods are: chunked, compress, deflate, gzip, identity.	Transfer-Encoding: chunked
     * Vary	Tells downstream proxies how to match future request headers to decide whether the cached response can be used rather than requesting a fresh one from the origin server.	Vary: *
     * Via	Informs the client of proxies through which the response was sent.	Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
     * Warning	A general warning about possible problems with the entity body.	Warning: 199 Miscellaneous warning
     * WWW-Authenticate	Indicates the authentication scheme that should be used to access the requested entity.	WWW-Authenticate: Basic
     * <p/>
     * X-Frame-Options[12]	Clickjacking protection: "deny" - no rendering within a frame, "sameorigin" - no rendering if origin mismatch	X-Frame-Options: deny
     * X-XSS-Protection[13]	Cross-site scripting (XSS) filter	X-XSS-Protection: 1; mode=block
     * X-Content-Type-Options[14]	the only defined value, "nosniff", prevents Internet Explorer from MIME-sniffing a response away from the declared content-type	X-Content-Type-Options: nosniff
     * X-Forwarded-For[15]	a de facto standard for identifying the originating IP address of a client connecting to a web server through an HTTP proxy or load balancer	X-Forwarded-For: client1, proxy1, proxy2
     * X-Forwarded-Proto[16]	a de facto standard for identifying the originating protocol of an HTTP request, since a reverse proxy (load balancer) communicates with a web server using HTTP	X-Forwarded-Proto: https
     * X-Powered-By[17]	specifies the technology (ASP.NET, PHP, JBoss, e.g.) supporting the web application (version details are often in X-Runtime, X-Version, or X-AspNet-Version)	X-Powered-By: PHP/5.2.1
     */

    public static String ACCEPT = "Accept";
    public static String ACCEPT_CHARSET = "Accept-Charset";
    public static String ACCEPT_ENCODING = "Accept-Encoding";
    public static String ACCEPT_LANGUAGE = "Accept-Language";
    public static String ACCEPT_AUTHORIZATION = "Authorization";
    public static String CACHE_CONTROL = "Cache-Control";
    public static String CONNECTION = "Connection";
    public static String COOKIE = "Cookie";
    public static String CONTENT_LENGTH = "Content-Length";
    public static String CONTENT_MD5 = "Content-MD5";
    public static String CONTENT_TYPE = "Content-Type";
    public static String DATE = "Date";
    public static String EXPECT = "Expect";
    public static String FROM = "From";
    public static String HOST = "Host";
    public static String IF_MATCH = "If-Match";
    public static String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static String IF_NONE_MATCH = "If-None-Match";
    public static String IF_RANGE = "If-Range";
    public static String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static String MAX_FORWARDS = "Max-Forwards";
    public static String PRAGMA = "Pragma";
    public static String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static String RANGE = "Range";
    public static String REFERER = "Referer";
    public static String TE = "TE";
    public static String UPGRADE = "Upgrade";
    public static String USER_AGENT = "User-Agent";
    public static String VIA = "Via";
    public static String WARNING = "Warning";
    public static String X_REQUESTED_FOR = "X-Requested-With";
    public static String X_DO_NOT_TRACK = "X-Do-Not-Track";
    public static String DNT = "DNT";

    public static String ACCEPT_RANGE = "Accept-Ranges";
    public static String AGE = "Age";
    public static String ALLOW = "Allow";
    public static String CONTENT_ENCODING = "Content-Encoding";
    public static String CONTENT_LANGUAGE = "Content-Language";
    public static String CONTENT_LOCATION = "Content-Location";
    public static String CONTENT_DISPOSITION = "Content-Disposition";
    public static String CONTENT_RANGE = "Content-Range";
    public static String ETAG = "ETag";
    public static String EXPIRES = "Expires";
    public static String LAST_MODIFIED = "Last-Modified";
    public static String LINK = "Link";
    public static String LOCATION = "Location";
    public static String P3P = "P3P";
    public static String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    public static String REFRESH = "Refresh";
    public static String RETRY_AFTER = "Retry-After";
    public static String SERVER = "Server";
    public static String SET_COOKIE = "Set-Cookie";
    public static String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    public static String TRAILER = "Trailer";
    public static String TRANSFER_ENCODING = "Transfer-Encoding";
    public static String VARY = "Vary";
    public static String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static String X_FRAME_OPTIONS = "X-Frame-Options";
    public static String X_XSS_PROTECTION = "X-XSS-Protection";
    public static String X_CONTENT_TYPE = "X-Content-Type-Options";
    public static String X_FORWARDED_FOR = "X-Forwarded-For";
    public static String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    public static String X_POWERED_BY = "X-Powered-By";

    private HashMap<String, String> headers = new HashMap<>(32);

    public String getHeader(String header) {
        return headers.get(header);
    }

    public Map<String, String> getAllHeadersMap() {
        return Collections.unmodifiableMap(headers);
    }

    public HTTPHeaders appendHeader(String header, String value) {
        String previousValue = headers.get(header);
        if (previousValue == null) {
            headers.put(header, value);
        } else {
            String actualValue = previousValue + "," + value;
            headers.put(header, actualValue);
        }
        return this;
    }

    public HTTPHeaders putHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }

    @Override
    public String toString() {
        return "HTTPHeaders{" +
                "headers=" + headers +
                '}';
    }
}

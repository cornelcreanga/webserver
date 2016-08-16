package com.ccreanga.webserver.http;

import com.ccreanga.webserver.formatters.DateUtil;

import java.time.LocalDateTime;

import static com.ccreanga.webserver.http.HttpMethod.GET;
import static com.ccreanga.webserver.http.HttpMethod.HEAD;
import static com.google.common.net.HttpHeaders.*;

/**
 * Implements HTTP conditionals.
 * <p>
 * See https://tools.ietf.org/html/rfc7232#section-6 and https://tools.ietf.org/html/rfc7232#page-13
 */
public class HttpConditionals {

    /**
     * Implements HTTP conditionals.
     * <p>
     * See https://tools.ietf.org/html/rfc7232#section-6 and https://tools.ietf.org/html/rfc7232#page-13
     *
     * @param request      Request message
     * @param etag         Resource etag
     * @param modifiedDate Resource modified date
     * @return httpstatus (412, 304, 400 - in case of invalid date headers)
     */
    public static HttpStatus evaluateConditional(HttpRequestMessage request, String etag, LocalDateTime modifiedDate) {


        HttpStatus response = HttpStatus.OK;
        String ifMatch = request.getHeader(IF_MATCH);
        String ifUnmodifiedSince = request.getHeader(IF_UNMODIFIED_SINCE);
        String ifNoneMatch = request.getHeader(IF_NONE_MATCH);
        String ifModifiedSince = request.getHeader(IF_MODIFIED_SINCE);

        //todo - if match should use ONLY strong etags for comparison
        if ((ifMatch != null) && (!ifMatch.equals(etag)))
            return HttpStatus.PRECONDITION_FAILED;

        if ((ifUnmodifiedSince != null) && (ifMatch == null)) {
            LocalDateTime date = DateUtil.parseRfc2161CompliantDate(ifUnmodifiedSince);
            if (date == null)//unparsable date
                return HttpStatus.BAD_REQUEST;
            if (modifiedDate.isAfter(date))
                return HttpStatus.PRECONDITION_FAILED;
        }

        if (ifNoneMatch != null) {
            if (ifNoneMatch.equals(etag))
                return (request.getMethod().equals(GET) || request.getMethod().equals(HEAD)) ?
                        HttpStatus.NOT_MODIFIED :
                        HttpStatus.PRECONDITION_FAILED;
        }

        if ((ifModifiedSince != null) && (request.getMethod().equals(GET) || request.getMethod().equals(HEAD))) {
            LocalDateTime date = DateUtil.parseRfc2161CompliantDate(ifModifiedSince);
            if (date == null)//unparsable date
                return HttpStatus.BAD_REQUEST;
            if (modifiedDate.isBefore(date) || modifiedDate.equals(date))
                return HttpStatus.NOT_MODIFIED;
        }

        return response;
    }

    public static HttpStatus evaluateIfRange(HttpRequestMessage request, String etag, LocalDateTime modifiedDate) {
        String ifRange = request.getHeader(IF_RANGE);
        LocalDateTime localDateTime = DateUtil.parseRfc2161CompliantDate(ifRange);
        if (localDateTime != null) {//if resource was modified after the ifrange date return the full resource
            return modifiedDate.isAfter(localDateTime) ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;
        } else {//if the etag are not equal return the full resource; todo - we should use strong etags
            return (!ifRange.equals(etag)) ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;
        }

    }

}

package com.ccreanga.webserver.http;

import com.ccreanga.webserver.formatters.DateUtil;

import java.time.LocalDateTime;

import static com.google.common.net.HttpHeaders.*;

/**
 * Implements HTTP conditionals.
 * <p>
 * See https://tools.ietf.org/html/rfc7232#section-6 and https://tools.ietf.org/html/rfc7232#page-13
 */
public class HttpConditionals {

    /**
     * Implements HTTP conditionals. Right now the method works only for GET methods! (todo - add put/post behaviour)
     * <p>
     * See https://tools.ietf.org/html/rfc7232#section-6 and https://tools.ietf.org/html/rfc7232#page-13
     *
     * @param request Request message
     * @param etag Resource etag
     * @param modifiedDate Resource modified date
     * @return httpstatus (412, 304, 400 - in case of invalid date headers)
     */
    public static HTTPStatus evaluateConditional(HttpRequestMessage request, String etag, LocalDateTime modifiedDate) {

        //todo - conditional should take into account the HTTP method too
        HTTPStatus response = HTTPStatus.OK;
        String ifMatch = request.getHeader(IF_MATCH);
        String ifUnmodifiedSince = request.getHeader(IF_UNMODIFIED_SINCE);
        String ifNoneMatch = request.getHeader(IF_NONE_MATCH);
        String ifModifiedSince = request.getHeader(IF_MODIFIED_SINCE);

        if ((ifMatch != null) && (!ifMatch.equals(etag)))
            return HTTPStatus.PRECONDITION_FAILED;

        if ((ifUnmodifiedSince != null) && (ifMatch == null)) {
            LocalDateTime date = DateUtil.parseRfc2161CompliantDate(ifUnmodifiedSince);
            if (date == null)//unparsable date
                return HTTPStatus.BAD_REQUEST;
            if (modifiedDate.isAfter(date))
                return HTTPStatus.PRECONDITION_FAILED;
        }

        if (ifNoneMatch != null) {
            if (!ifNoneMatch.equals(etag))
                return HTTPStatus.NOT_MODIFIED;//for get and head
        }

        if ((ifModifiedSince != null) && (ifNoneMatch == null)) {
            LocalDateTime date = DateUtil.parseRfc2161CompliantDate(ifModifiedSince);
            if (date == null)//unparsable date
                return HTTPStatus.BAD_REQUEST;
            if (modifiedDate.isBefore(date) || modifiedDate.equals(date))
                return HTTPStatus.NOT_MODIFIED;
        }

        return response;
    }


}

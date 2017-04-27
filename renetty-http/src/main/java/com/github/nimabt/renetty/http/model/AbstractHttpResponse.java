package com.github.nimabt.renetty.http.model;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public abstract class AbstractHttpResponse {

    private final HttpResponseStatus status;
    private final String contentType;


    protected AbstractHttpResponse(final HttpResponseStatus status, final String contentType){
        this.status = status;
        this.contentType = contentType;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public abstract DataType getType();


    @Override
    public String toString() {
        return "status=" + status +
                ", contentType='" + contentType + '\'';
    }
}

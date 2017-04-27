package com.github.nimabt.renetty.http.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class HttpRequestException extends Exception {

    private final HttpResponseStatus httpResponseStatus;

    private final String body;

    private final byte[] data;

    public HttpRequestException(final HttpResponseStatus httpResponseStatus){
        this(httpResponseStatus,null,null,null);
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final String body){
        this(httpResponseStatus,body,null,null);
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final String body, final String message){
        this(httpResponseStatus,body,null,message);
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final byte[] data){
        this(httpResponseStatus,null,data,null);
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final byte[] data, final String message){
        this(httpResponseStatus,null,data,message);
    }


    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final String body, final byte[] data, final String message){
        super(message);
        this.httpResponseStatus = httpResponseStatus;
        this.body = body;
        this.data = data;
    }


    public HttpResponseStatus getHttpResponseStatus() {

        return httpResponseStatus;
    }

    public String getBody() {
        return body;
    }

    public byte[] getData() {
        return data;
    }


    @Override
    public String toString() {
        return "HttpRequestException{" +
                "httpResponseStatus=" + httpResponseStatus +
                ", body='" + body + '\'' +
                ", data='" + ((data!=null) ? "binary#" + data.length : null) + '\'' +
                '}';
    }

}

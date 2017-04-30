package com.github.nimabt.renetty.http.exception;

import com.github.nimabt.renetty.http.model.AbstractHttpResponse;
import com.github.nimabt.renetty.http.model.BinaryHttpResponse;
import com.github.nimabt.renetty.http.model.TextHttpResponse;
import com.github.nimabt.renetty.http.util.ConstValues;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class HttpRequestException extends Exception {


    private final AbstractHttpResponse httpResponse;

    public HttpRequestException(final AbstractHttpResponse httpResponse, final String message){
        super(message);
        this.httpResponse = httpResponse;
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus){
        this(new TextHttpResponse(httpResponseStatus, null,ConstValues.DEFAULT_CONTENT_TYPE),null);
    }


    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final String body){
        this(new TextHttpResponse(httpResponseStatus,body,ConstValues.DEFAULT_CONTENT_TYPE),null);
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final String body, final String contentType){
        this(new TextHttpResponse(httpResponseStatus,body,contentType),null);
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final String body, final String contentType, final String message){
        this(new TextHttpResponse(httpResponseStatus,body,contentType),message);
    }


    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final byte[] data){
        this(new BinaryHttpResponse(httpResponseStatus,data,ConstValues.DEFAULT_BIN_CONTENT_TYPE),null);
    }

    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final byte[] data, final String contentType){
        this(new BinaryHttpResponse(httpResponseStatus,data,contentType),null);
    }


    public HttpRequestException(final HttpResponseStatus httpResponseStatus, final byte[] data, final String contentType, final String message){
        this(new BinaryHttpResponse(httpResponseStatus,data,contentType),message);
    }


    public AbstractHttpResponse getHttpResponse() {
        return httpResponse;
    }


    @Override
    public String toString() {
        return "HttpRequestException{" +
                "httpResponse=" + httpResponse +
                '}';
    }

}

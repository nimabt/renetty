package com.github.nimabt.renetty.http.model;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class TextHttpResponse extends AbstractHttpResponse {

    private final String body;



    public TextHttpResponse(final HttpResponseStatus status, final String contentType){
        this(status,contentType,null);
    }

    public TextHttpResponse(final HttpResponseStatus status, final String contentType, final String body){
        super(status,contentType);
        this.body = body;
    }


    public String getBody() {
        return body;
    }


    public DataType getType() {
        return DataType.TEXT;
    }


    @Override
    public String toString() {
        return "TextHttpResponse{" +
                super.toString() +
                ", body='" + body + '\'' +
                '}';
    }

}

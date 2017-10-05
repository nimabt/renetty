package com.github.nimabt.renetty.http.model.response;

import com.github.nimabt.renetty.http.model.DataType;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public abstract class AbstractHttpResponse {

    private final HttpResponseStatus status;
    private final String contentType;
    private final Map<String,String> headers;


    protected AbstractHttpResponse(final HttpResponseStatus status, final String contentType){
        this.status = status;
        this.contentType = contentType;
        this.headers = new HashMap<String, String>();
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public abstract DataType getType();


    public void addHeader(final String key, final String value){
        headers.put(key,value);
    }

    public String getHeader(final String key){
        return headers.get(key);
    }


    public Map<String,String> getHeaders(){
        return headers;
    }



    @Override
    public String toString() {
        return "status=" + status +
                ", contentType='" + contentType + '\'';
    }
}

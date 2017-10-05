package com.github.nimabt.renetty.http.model;

import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;

import java.io.Serializable;

/**
 * @author: nima.abt
 * @since: 5/17/17
 */
public class InvokeResponse implements Serializable {

    private final boolean ok;
    private final AbstractHttpResponse httpResponse;

    public InvokeResponse(final boolean ok, final AbstractHttpResponse httpResponse){
        this.ok = ok;
        this.httpResponse = httpResponse;
    }


    public boolean isOk() {
        return ok;
    }

    public AbstractHttpResponse getHttpResponse() {
        return httpResponse;
    }


    @Override
    public String toString() {
        return "InvokeResponse{" +
                "ok=" + ok +
                ", httpResponse=" + httpResponse +
                '}';
    }



}

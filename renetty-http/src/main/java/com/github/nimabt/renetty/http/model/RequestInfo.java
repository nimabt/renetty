package com.github.nimabt.renetty.http.model;

import com.sun.jersey.api.uri.UriTemplate;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class RequestInfo implements Serializable {

    private final RequestMethod requestMethod;
    private final String uri;
    private final DataType requestType;
    //private final DataType responseType;
    private final String responseContentType;

    private final int requestHandlerIndex;
    private final Method invokationMethod;

    private final boolean breakOnException;

    private final UriTemplate uriTemplate;

    public RequestInfo(
            final int requestHandlerIndex,
            final Method invokationMethod,
            final RequestMethod requestMethod,
            final String uri,
            final DataType requestType,
            //final DataType responseType,
            final String responseContentType,
            final boolean breakOnException
            ){
        this.requestHandlerIndex = requestHandlerIndex;
        this.invokationMethod = invokationMethod;
        this.requestMethod = requestMethod;
        this.uri = uri;
        this.requestType = requestType;
        //this.responseType = responseType;
        this.responseContentType = responseContentType;
        this.breakOnException = breakOnException;
        this.uriTemplate = new UriTemplate(uri);
    }


    public int getRequestHandlerIndex() {
        return requestHandlerIndex;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getUri() {
        return uri;
    }

    public Method getInvokationMethod() {
        return invokationMethod;
    }

    /*
    public DataType getResponseType() {
        return responseType;
    }
    */

    public DataType getRequestType() {
        return requestType;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    /*
    public boolean isBinResp(){
        return (responseType != null && responseType.equals(DataType.BINARY));
    }
    */

    public boolean isBreakOnException() {
        return breakOnException;
    }

    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }



    @Override
    public String toString() {
        return "RequestInfo{" +
                "requestHandlerIndex=" + requestHandlerIndex +
                ", requestMethod=" + requestMethod +
                ", uri='" + uri + '\'' +
                ", requestType=" + requestType +
                //", responseType=" + responseType +
                ", responseContentType='" + responseContentType + '\'' +
                ", invokationMethod=" + invokationMethod +
                ", breakOnException=" + breakOnException +
                '}';
    }


}

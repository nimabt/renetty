package com.github.nimabt.renetty.http.model;

import java.util.Map;

/**
 * @author: nima.abt
 * @since: 10/9/17
 */
public class IncomingRequestInfo {

    private final RequestInfo requestInfo;
    private final Map<String,String> pathVariables;

    public IncomingRequestInfo(final RequestInfo requestInfo, final Map<String,String> pathVariables){
        this.requestInfo = requestInfo;
        this.pathVariables = pathVariables;
    }


    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }


    @Override
    public String toString() {
        return "IncomingRequestInfo{" +
                "requestInfo=" + requestInfo +
                ", pathVariables=" + pathVariables +
                '}';
    }


}

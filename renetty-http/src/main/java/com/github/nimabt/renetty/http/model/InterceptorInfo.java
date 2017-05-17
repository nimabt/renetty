package com.github.nimabt.renetty.http.model;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author: nima.abt
 * @since: 5/16/17
 */
public class InterceptorInfo implements Serializable {

    private final MethodType type;
    private final int requestHandlerIndex;
    private final Method invokationMethod;
    private final RequestMethod requestMethod;
    private final boolean breakOnException;



    public InterceptorInfo(final MethodType type, final int requestHandlerIndex, final Method invokationMethod, final RequestMethod requestMethod, final boolean breakOnException){
        this.type = type;
        this.requestHandlerIndex = requestHandlerIndex;
        this.invokationMethod = invokationMethod;
        this.requestMethod = requestMethod;
        this.breakOnException = breakOnException;
    }


    public MethodType getType() {
        return type;
    }

    public int getRequestHandlerIndex() {
        return requestHandlerIndex;
    }

    public Method getInvokationMethod() {
        return invokationMethod;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public boolean isBreakOnException() {
        return breakOnException;
    }

    @Override
    public String toString() {
        return "InterceptorInfo{" +
                "type=" + type +
                ", requestHandlerIndex=" + requestHandlerIndex +
                ", invokationMethod=" + invokationMethod +
                ", requestMethod=" + requestMethod +
                ", breakOnException=" + breakOnException +
                '}';
    }


}

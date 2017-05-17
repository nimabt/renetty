package com.github.nimabt.renetty.example.http.intercept;

import com.github.nimabt.renetty.http.annotation.*;
import com.github.nimabt.renetty.http.exception.HttpRequestException;
import com.github.nimabt.renetty.http.model.RequestMethod;
import com.github.nimabt.renetty.http.netty.HttpRequestHandler;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 5/16/17
 */
public class InterceptorHttpHandler implements HttpRequestHandler {

    public InterceptorHttpHandler(){

    }


    @PreIntercept(method = RequestMethod.GET)
    public void preInterceptor(final @RequestId String requestId){
        System.out.println("reqId: " + requestId + " got request !");
    }



    @HttpRequest(method = RequestMethod.GET, path = "/test")
    public String test(final @RequestId String requestId){
        System.out.println("reqId: " + requestId + " processing request :)");
        return "test response";
    }


    @HttpRequest(method = RequestMethod.GET, path = "/test2" , breakOnException = false)
    public String test2(final @RequestId String requestId) throws HttpRequestException{
        System.out.println("reqId: " + requestId + " processing request with exception , post-interceptor will not be executed because of breakOnException");
        throw new HttpRequestException(HttpResponseStatus.INTERNAL_SERVER_ERROR,"exception occurred :(");
    }



    @PostIntercept(method = RequestMethod.GET)
    public void postInterceptor(
            final @RequestId String requestId,
            final @RequestTime long requestTime,
            final @ResponseStatus int responseStatus,
            final @ResponseBody String responseBody
    ){
        System.out.println("reqId: " + requestId + " done processing; returned: {status:" + responseStatus + " , body: " + responseBody + "} , took: " + (System.currentTimeMillis()-requestTime) + " msecs.");
    }



}

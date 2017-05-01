package com.github.nimabt.renetty.example.http.multihandler;

import com.github.nimabt.renetty.http.annotation.HttpRequest;
import com.github.nimabt.renetty.http.model.RequestMethod;
import com.github.nimabt.renetty.http.netty.HttpRequestHandler;

/**
 * @author: nima.abt
 * @since: 5/1/17
 */
public class SecondHttpHandler implements HttpRequestHandler {


    @HttpRequest(method = RequestMethod.GET, path = "/test2")
    public String testGet(){
        return "second-handler's test response";
    }





}

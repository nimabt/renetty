package com.github.nimabt.renetty.example.http.intercept;

import com.github.nimabt.renetty.http.netty.HttpRequestHandler;
import com.github.nimabt.renetty.http.netty.NettyHttpServer;

/**
 * @author: nima.abt
 * @since: 5/16/17
 */
public class InterceptorHandlerTestHttpServerLauncher {


    public static void main(String[] args) throws Exception {


        final HttpRequestHandler handler = new InterceptorHttpHandler();
        final int port = 8090;
        final int workerCount = 1000;
        final int maxContentLength = 65535;
        final NettyHttpServer nettyHttpServer = new NettyHttpServer(port,handler,workerCount,maxContentLength);
        nettyHttpServer.start();


    }
}

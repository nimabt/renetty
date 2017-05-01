package com.github.nimabt.renetty.example.http.multihandler;

import com.github.nimabt.renetty.http.netty.HttpRequestHandler;
import com.github.nimabt.renetty.http.netty.NettyHttpServer;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: nima.abt
 * @since: 5/1/17
 */
public class MultiHandlerTestHttpServerLauncher {

    public static void main(String[] args) throws Exception {

        final List<HttpRequestHandler> handlers = new LinkedList<HttpRequestHandler>();
        handlers.add(new FirstHttpHandler());
        handlers.add(new SecondHttpHandler());
        final int port = 8090;
        final int workerCount = 1000;
        final int maxContentLength = 65535;
        final NettyHttpServer nettyHttpServer = new NettyHttpServer(port,handlers,workerCount,maxContentLength);
        nettyHttpServer.start();

    }

}

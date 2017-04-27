package com.github.nimabt.renetty.example.http;


import com.github.nimabt.renetty.http.netty.NettyHttpServer;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class TestHttpServerLauncher {

    public static void main(String[] args) throws Exception {


        final TestHttpHandler handler = new TestHttpHandler();
        final int port = 8090;
        final int workerCount = 1000;
        final int maxContentLength = 65535;
        final NettyHttpServer nettyHttpServer = new NettyHttpServer(port,handler,workerCount,maxContentLength);
        nettyHttpServer.start();


    }






}

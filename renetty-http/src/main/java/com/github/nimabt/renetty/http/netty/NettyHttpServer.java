package com.github.nimabt.renetty.http.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;


/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class NettyHttpServer {

    private final int port;
    private final List<HttpRequestHandler> requestHandlers;
    private final int workerCount;
    private final int maxContentLength;
    private final List<String> allowedHeaders;

    private final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    public NettyHttpServer(final int port, final HttpRequestHandler requestHandler , final int workerCount, final int maxContentLength) {
        this(port,new LinkedList<HttpRequestHandler>(){{add(requestHandler);}},workerCount,maxContentLength);
    }

    // todo: constructor requires builder pattern ...
    public NettyHttpServer(final int port, final List<HttpRequestHandler> requestHandlers, final int workerCount, final int maxContentLength){
        this(port,requestHandlers,workerCount,maxContentLength, new LinkedList<String>());
    }


    public NettyHttpServer(final int port, final List<HttpRequestHandler> requestHandlers, final int workerCount, final int maxContentLength, final List<String> allowedHeaders){
        logger.info("init: NettyHttpServer (@port: {}) ... ",port);
        this.port = port;
        this.requestHandlers = requestHandlers;
        this.workerCount = workerCount>1 ?  workerCount : 1;
        this.maxContentLength = maxContentLength;
        this.allowedHeaders = allowedHeaders;
    }






    public void start() throws InterruptedException {

        if(logger.isDebugEnabled())
            logger.debug("gonna start netty http server with the following params: (port: {}, workerCount: {}, maxContentLength: {})",port,workerCount,maxContentLength);

        final HttpRequestManager httpRequestManager = new HttpRequestManager(requestHandlers);

        final EventLoopGroup parentGroup = new NioEventLoopGroup(workerCount);
        final EventLoopGroup childGroup = new NioEventLoopGroup(workerCount);

        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.group(parentGroup, childGroup);
        bootstrap.handler(new LoggingHandler(LogLevel.WARN));
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new NettyHttpChannelInitializer(httpRequestManager, maxContentLength, allowedHeaders));

        final Channel channel = bootstrap.bind(port).sync().channel();
        logger.info("netty http server is up-n-running ... :)");
        //channel.closeFuture().sync();
        channel.closeFuture();

    }


}

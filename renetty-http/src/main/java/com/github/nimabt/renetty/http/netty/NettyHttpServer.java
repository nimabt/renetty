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


/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class NettyHttpServer {

    private final int port;
    private final HttpRequestHandler requestHandler;
    private final int workerCount;
    private final int maxContentLength;

    private final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    public NettyHttpServer(final int port, final HttpRequestHandler requestHandler , final int workerCount, final int maxContentLength) {
        logger.info("init: NettyHttpServer (@port: " + port + ") ... ");
        this.port = port;
        this.requestHandler = requestHandler;
        this.workerCount = workerCount>1 ?  workerCount : 1;
        this.maxContentLength = maxContentLength;
    }



    public void start() throws InterruptedException {

        if(logger.isDebugEnabled())
            logger.debug("gonna start netty http server with the following params: (port: " + port + ", workerCount: " + workerCount + " , maxContentLength: " + maxContentLength);

        final HttpRequestManager httpRequestManager = new HttpRequestManager(requestHandler);

        final EventLoopGroup parentGroup = new NioEventLoopGroup(workerCount);
        final EventLoopGroup childGroup = new NioEventLoopGroup();

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.group(parentGroup, childGroup);
            bootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new NettyHttpChannelInitializer(httpRequestManager, maxContentLength));

            final Channel channel = bootstrap.bind(port).sync().channel();
            logger.info("netty http server is up-n-running ... ");
            channel.closeFuture().sync();

        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }

    }


}

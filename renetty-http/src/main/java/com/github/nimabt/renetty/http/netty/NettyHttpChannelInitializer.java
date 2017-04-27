package com.github.nimabt.renetty.http.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class NettyHttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final HttpRequestManager requestManager;
    private final int maxContentLength;

    public NettyHttpChannelInitializer(final HttpRequestManager requestManager, final int maxContentLength){
        this.requestManager = requestManager;
        this.maxContentLength = maxContentLength;
    }


    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));
        pipeline.addLast(new HttpContentDecompressor());
        pipeline.addLast(new NettyHttpChannelInboundHandler(requestManager));

    }

}

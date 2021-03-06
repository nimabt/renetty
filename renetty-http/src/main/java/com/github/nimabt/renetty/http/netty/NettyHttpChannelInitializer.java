package com.github.nimabt.renetty.http.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;

import java.util.List;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class NettyHttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final HttpRequestManager requestManager;
    private final int maxContentLength;
    private final List<String> allowedHeaders;

    public NettyHttpChannelInitializer(final HttpRequestManager requestManager, final int maxContentLength, final List<String> allowedHeaders){
        this.requestManager = requestManager;
        this.maxContentLength = maxContentLength;
        this.allowedHeaders = allowedHeaders;
    }


    @Override
    public void initChannel(SocketChannel ch) throws Exception {



        //final CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().build();
        final CorsConfig corsConfig;
        if(allowedHeaders!=null && allowedHeaders.size()>0){
            corsConfig =  CorsConfigBuilder.forAnyOrigin().allowedRequestHeaders(allowedHeaders.toArray(new String[allowedHeaders.size()])).build();
        } else{
            corsConfig = CorsConfigBuilder.forAnyOrigin().build(); // todo: make forAnyOrigin Configurable ...
        }


        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));
        pipeline.addLast(new HttpContentDecompressor());
        pipeline.addLast(new CorsHandler(corsConfig));
        pipeline.addLast(new NettyHttpChannelInboundHandler(requestManager));

    }

}

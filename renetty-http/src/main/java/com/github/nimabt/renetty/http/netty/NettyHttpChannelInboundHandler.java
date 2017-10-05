package com.github.nimabt.renetty.http.netty;

import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;
import com.github.nimabt.renetty.http.model.response.BinaryHttpResponse;
import com.github.nimabt.renetty.http.model.DataType;
import com.github.nimabt.renetty.http.model.response.RedirectHttpResponse;
import com.github.nimabt.renetty.http.model.response.TextHttpResponse;
import com.github.nimabt.renetty.http.util.ConstValues;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.CONTINUE;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class NettyHttpChannelInboundHandler extends ChannelInboundHandlerAdapter {



    private final HttpRequestManager httpRequestManager;

    private final Logger logger = LoggerFactory.getLogger(NettyHttpChannelInboundHandler.class);

    public NettyHttpChannelInboundHandler(final HttpRequestManager requestHandlerMapper) {
        this.httpRequestManager = requestHandlerMapper;
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        final long startTime = System.currentTimeMillis();

        final String reqId = UUID.randomUUID().toString();

        if(!(msg instanceof FullHttpRequest)){
            // todo: requires test, +think of returning custom http resp
            logger.warn("{{}} received request is not an instance of FullHttpRequest, gonna drop the message: {}",reqId,msg);
            ctx.close();
            return;
        }

        final FullHttpRequest req = (FullHttpRequest) msg;

        if(logger.isDebugEnabled())
            logger.info("{{}} got FullHttpRequest: {}",reqId,req);

        logger.info("{{}} got http request [{}:{}]",reqId,req.method(),req.uri());

        if (HttpUtil.is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        final boolean keepAlive = HttpUtil.isKeepAlive(req);

        final HttpResponseStatus httpResponseStatus;


        final AbstractHttpResponse response = httpRequestManager.process(reqId, startTime, req, ctx);
        if (response == null) {
            logger.error("{{}} received null resp. from the corresponding handler for [{}:{}]",reqId,req.method(),req.uri());
            httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            prepareResponse(httpResponseStatus,ConstValues.EMPTY_RESPONSE,ConstValues.DEFAULT_CONTENT_TYPE,keepAlive,ctx,null);
            return;

        }

        if(response instanceof RedirectHttpResponse){
            prepareResponse(response.getStatus(),new byte[]{},response.getContentType(),keepAlive,ctx,response.getHeaders());
            return;
        }


        httpResponseStatus = response.getStatus();
        final String contentType = response.getContentType();
        final byte[] resp;
        if (response.getType().equals(DataType.BINARY)) {
            resp = ((BinaryHttpResponse) response).getData();
        } else {
            final String body = ((TextHttpResponse) response).getBody();
            resp = ((body != null) ? body.getBytes() : ConstValues.EMPTY_RESPONSE);
        }

        prepareResponse(httpResponseStatus,resp,contentType,keepAlive,ctx,response.getHeaders());




    }


    private void prepareResponse(final HttpResponseStatus httpResponseStatus, final byte[] resp, final String contentType, final boolean keepAlive, final ChannelHandlerContext ctx, final Map<String,String> headers) throws Exception{

        final FullHttpResponse httpResponse =
                new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, Unpooled.wrappedBuffer(resp));

        httpResponse.headers().set(CONTENT_TYPE, contentType);
        httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());

        if(headers!=null && headers.size()>0){
            for(final Map.Entry<String,String> header : headers.entrySet()){
                httpResponse.headers().set(header.getKey(),header.getValue());
            }
        }

        if (!keepAlive) {
            ctx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
        } else {
            httpResponse.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.write(httpResponse);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("exceptionCaught: " + cause);
        // closing the connection when an exception is raised.
        //cause.printStackTrace();
        ctx.close();
    }









}

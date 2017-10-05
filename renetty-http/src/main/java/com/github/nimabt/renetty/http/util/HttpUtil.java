package com.github.nimabt.renetty.http.util;

import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;
import com.github.nimabt.renetty.http.model.response.BinaryHttpResponse;
import com.github.nimabt.renetty.http.model.RequestInfo;
import com.github.nimabt.renetty.http.model.response.TextHttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author: nima.abt
 * @since: 5/18/17
 */
public class HttpUtil {



    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);


    public static String getIpAddr(ChannelHandlerContext ctx, final FullHttpRequest request) {

        if(request.headers()!=null){
            final String xForwardedFor = request.headers().get("X-Forwarded-For");
            if (!StringUtils.isBlank(xForwardedFor)) {
                return xForwardedFor;
            }
        }
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

    }




    public static AbstractHttpResponse getResponse(final RequestInfo requestInfo, final Object response, final HttpResponseStatus httpResponseStatus){

        if(response==null){
            logger.warn("response is null for: {} ; cannot decide the type of the response!",requestInfo); // todo : log reqId too ... ?
            return new TextHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,null,requestInfo.getResponseContentType());
        } else{
            if( (response instanceof AbstractHttpResponse) ){
                return (AbstractHttpResponse) response;
            } else{
                if(response instanceof byte[]){
                    return new BinaryHttpResponse(httpResponseStatus,(byte[]) response, getContentType(requestInfo,true));
                } else{
                    return new TextHttpResponse(httpResponseStatus, (String) response, getContentType(requestInfo,false));
                }
            }
        }

    }





    public static String getContentType(final RequestInfo requestInfo, final boolean binStat){

        final String customResponseContentType = requestInfo.getResponseContentType();
        if(!StringUtils.isBlank(customResponseContentType)){
            return customResponseContentType;
        }
        return binStat ? ConstValues.DEFAULT_BIN_CONTENT_TYPE : ConstValues.DEFAULT_CONTENT_TYPE;

    }



}

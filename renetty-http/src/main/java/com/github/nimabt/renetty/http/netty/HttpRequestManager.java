package com.github.nimabt.renetty.http.netty;

import com.github.nimabt.renetty.http.annotation.*;
import com.github.nimabt.renetty.http.exception.HttpRequestException;
import com.github.nimabt.renetty.http.model.*;
import com.github.nimabt.renetty.http.util.ConstValues;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class HttpRequestManager {

    private final List<HttpRequestHandler> httpRequestHandlers;


    private final Logger logger = LoggerFactory.getLogger(HttpRequestManager.class);


    private final Map<String, RequestInfo> requestMap = new HashMap<String, RequestInfo>();

    public HttpRequestManager(final List<HttpRequestHandler> httpRequestHandlers){

        logger.info("init: HttpRequestManager ... ");
        this.httpRequestHandlers = httpRequestHandlers;

        for(int i=0;i<httpRequestHandlers.size();i++){
            final HttpRequestHandler httpRequestHandler = httpRequestHandlers.get(i);
            final Class c = httpRequestHandler.getClass();
            for (Method method : c.getDeclaredMethods()) {

                if (method.isAnnotationPresent(HttpRequest.class)) {
                    final Annotation annotation = method.getAnnotation(HttpRequest.class);
                    HttpRequest httpRequest = (HttpRequest) annotation;
                    if (httpRequest != null) {
                        final String key = getRequestKey(httpRequest.method().toString(), httpRequest.path());
                        if(requestMap.containsKey(key)){
                            logger.error("duplicate entry for: {}; gonna override the previous mapping info. ... ",key);
                        }
                        //final RequestInfo requestInfo = new RequestInfo(method, httpRequest.method(), httpRequest.path(), httpRequest.requestType(), httpRequest.responseType(), httpRequest.responseContentType());
                        final RequestInfo requestInfo = new RequestInfo(i,method, httpRequest.method(), httpRequest.path(), httpRequest.requestType(), httpRequest.responseContentType());
                        requestMap.put(key, requestInfo);
                    }
                }

            }

        }



        logger.info("done loading #{} HttpRequest items of #{} HttpRequestHandler(s) ...",requestMap.size(),httpRequestHandlers.size());


    }





    AbstractHttpResponse process(final String reqId, final FullHttpRequest req, final ChannelHandlerContext ctx) {

        final HttpMethod method = req.method();
        final String uri = req.uri();
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        final String path = queryStringDecoder.path();


        final String key = getRequestKey(method.name(), path);
        if (!requestMap.containsKey(key)) {
            return new TextHttpResponse(HttpResponseStatus.NOT_FOUND, null, ConstValues.DEFAULT_CONTENT_TYPE);
        }

        final RequestInfo requestInfo = requestMap.get(key);

        try {

            final Annotation[][] annotationArr = requestInfo.getInvokationMethod().getParameterAnnotations();

            final Object[] paramVal = new Object[annotationArr.length];

            if (annotationArr.length > 0) {

                for (int i = 0; i < annotationArr.length; i++) {

                    if(annotationArr[i].length==0) continue;

                    final Annotation annotation = annotationArr[i][0];

                    if (annotation instanceof RequestBody) {
                        final ByteBuf byteBuf = req.content();
                        paramVal[i] = byteBuf.toString(Charset.forName("UTF-8")); // todo: make charset configurable ...
                    } else if (annotation instanceof RequestData) {
                        final ByteBuf byteBuf = req.content();
                        final byte[] bytes = new byte[byteBuf.readableBytes()];
                        int readerIndex = byteBuf.readerIndex();
                        byteBuf.getBytes(readerIndex, bytes);
                        paramVal[i] = bytes;
                    } else if (annotation instanceof IpAddress) {
                        paramVal[i] = getIpAddr(ctx,req);
                    } else if (annotation instanceof QueryParam) {
                        final QueryParam queryParam = (QueryParam) annotation;
                        String value = null;
                        if(queryStringDecoder.parameters()!=null && queryStringDecoder.parameters().size()>0){
                            if(queryStringDecoder.parameters().containsKey(queryParam.key())){
                                final List<String> values = queryStringDecoder.parameters().get(queryParam.key());
                                if(values.size()>0){
                                    value = values.get(values.size()-1);
                                }
                            }
                        }
                        paramVal[i] = value;
                    } else if(annotation instanceof RequestHeader){
                        final RequestHeader requestHeader = (RequestHeader) annotation;
                        paramVal[i] = req.headers().get(requestHeader.key());
                    } else if(annotation instanceof RequestId){
                        paramVal[i] = reqId;
                    } else if(annotation instanceof RequestPath){
                        paramVal[i] = path;
                    }
                }

            }



            try{

                //final Object response = requestInfo.getInvokationMethod().invoke(httpRequestHandler, paramVal);
                final Object response = requestInfo.getInvokationMethod().invoke(httpRequestHandlers.get(requestInfo.getRequestHandlerIndex()), paramVal);

                /*
                if (requestInfo.isBinResp()){
                    return new BinaryHttpResponse(HttpResponseStatus.OK, getContentType(requestInfo), (byte[]) response);
                } else {
                    return new TextHttpResponse(HttpResponseStatus.OK, getContentType(requestInfo), (String) response);
                }
                */
                return getResponse(requestInfo,response,HttpResponseStatus.OK);

            } catch (InvocationTargetException e){
                final Throwable throwable = e.getCause();
                if (throwable instanceof HttpRequestException) {
                    final HttpRequestException httpRequestException = (HttpRequestException) throwable;
                    logger.info("{{}} got HttpRequestException while invoking: {}; gonna return: {}",reqId,requestInfo,httpRequestException);

                    /*
                    if (requestInfo.isBinResp()){
                        return new BinaryHttpResponse(httpRequestException.getHttpResponseStatus(), getContentType(requestInfo), httpRequestException.getData());
                    } else {
                        return new TextHttpResponse(httpRequestException.getHttpResponseStatus(), getContentType(requestInfo), httpRequestException.getBody());
                    }
                    */
                    return getResponse(requestInfo,httpRequestException.getHttpResponse(),httpRequestException.getHttpResponse().getStatus());
                } else{
                    logger.error("{{}} InvocationTargetException occurred while invoking: {}; reason: {}",reqId,requestInfo,e.getMessage());
                }
            } catch (Throwable e){
                logger.error("{{}} UnknownException occurred while invoking:{} ; reason: {}",reqId,requestInfo,e.getMessage());
            }


        } catch (Throwable t) {
            logger.error("{{}} exception occurred; reason: {}",reqId,t.getMessage());
        }

        /*
        if(requestInfo.isBinResp()){
            return new BinaryHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,getContentType(requestInfo));
        } else{
            return new TextHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,getContentType(requestInfo));
        }
        */
        return getResponse(requestInfo,null,HttpResponseStatus.INTERNAL_SERVER_ERROR);



    }




    private String getIpAddr(ChannelHandlerContext ctx, final FullHttpRequest request) {

        if(request.headers()!=null){
            final String xForwardedFor = request.headers().get("X-Forwarded-For");
            if (!StringUtils.isBlank(xForwardedFor)) {
                return xForwardedFor;
            }
        }
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();

    }






    private String getRequestKey(final String method, final String uri) {
        return method + ":" + uri;
    }




    private String getContentType(final RequestInfo requestInfo, final boolean binStat){

        final String customResponseContentType = requestInfo.getResponseContentType();
        if(!StringUtils.isBlank(customResponseContentType)){
            return customResponseContentType;
        }
        return binStat ? ConstValues.DEFAULT_BIN_CONTENT_TYPE : ConstValues.DEFAULT_CONTENT_TYPE;

    }

    private AbstractHttpResponse getResponse(final RequestInfo requestInfo, final Object response, final HttpResponseStatus httpResponseStatus){

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


}

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
import java.util.*;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class HttpRequestManager {

    private final List<HttpRequestHandler> httpRequestHandlers;


    private final Logger logger = LoggerFactory.getLogger(HttpRequestManager.class);


    private final Map<String, RequestInfo> requestMap = new HashMap<String, RequestInfo>();
    private final Map<String,InterceptorInfo> interceptMap = new HashMap<String,InterceptorInfo>();


    public HttpRequestManager(final List<HttpRequestHandler> httpRequestHandlers){

        logger.info("init: HttpRequestManager ... ");
        this.httpRequestHandlers = httpRequestHandlers;

        for(int i=0;i<httpRequestHandlers.size();i++){
            final HttpRequestHandler httpRequestHandler = httpRequestHandlers.get(i);
            final Class c = httpRequestHandler.getClass();

            for (Method method : c.getDeclaredMethods()) {

                if (method.isAnnotationPresent(HttpRequest.class)) {
                    final Annotation annotation = method.getAnnotation(HttpRequest.class);
                    final HttpRequest httpRequest = (HttpRequest) annotation;
                    if (httpRequest != null) {
                        final String key = getRequestKey(httpRequest.method().toString(), httpRequest.path());
                        if(requestMap.containsKey(key)){
                            logger.error("duplicate http-request method for: {}; gonna override the previous mapping info. ... ",key);
                        }
                        final RequestInfo requestInfo = new RequestInfo(i,method, httpRequest.method(), httpRequest.path(), httpRequest.requestType(), httpRequest.responseContentType(),httpRequest.breakOnException());
                        requestMap.put(key, requestInfo);
                    }
                } else{
                    // note: ignoring (Pre|Post)Handler re-execution with HttpRequest method
                    if(method.isAnnotationPresent(PreIntercept.class)){
                        final Annotation annotation = method.getAnnotation(PreIntercept.class);
                        final PreIntercept preIntercept = (PreIntercept) annotation;
                        if(preIntercept!=null){
                            final String key = getInterceptorKey(MethodType.PRE_INTERCEPT,preIntercept.method().toString(),i);
                            if(interceptMap.containsKey(key)){
                                logger.error("duplicate pre-intercept for: {}; gonna override the previous preIntercept ... ",httpRequestHandler);
                            }
                            interceptMap.put(key,new InterceptorInfo(MethodType.PRE_INTERCEPT,i,method,preIntercept.method(),preIntercept.breakOnException()));
                        }
                    }
                    if(method.isAnnotationPresent(PostIntercept.class)){
                        final Annotation annotation = method.getAnnotation(PostIntercept.class);
                        final PostIntercept postIntercept = (PostIntercept) annotation;
                        if(postIntercept!=null){
                            final String key = getInterceptorKey(MethodType.POST_INTERCEPT,postIntercept.method().toString(),i);
                            if(interceptMap.containsKey(key)){
                                logger.error("duplicate post-handler for: {}; gonna override the previous postIntercept ... ",httpRequestHandler);
                            }
                            interceptMap.put(key,new InterceptorInfo(MethodType.POST_INTERCEPT,i,method,postIntercept.method(),postIntercept.breakOnException()));
                        }
                    }
                }


            }

        }



        logger.info("done loading #{} HttpRequest items of #{} HttpRequestHandler(s), #{} {Pre/Post}Interceptor(s) ...",requestMap.size(),httpRequestHandlers.size(), interceptMap.size());


    }





    AbstractHttpResponse process(final String reqId, final long startTime, final FullHttpRequest req, final ChannelHandlerContext ctx) {

        final HttpMethod method = req.method();
        final String uri = req.uri();
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        final String path = queryStringDecoder.path();


        final String key = getRequestKey(method.name(), path);
        if (!requestMap.containsKey(key)) {
            return new TextHttpResponse(HttpResponseStatus.NOT_FOUND, null, ConstValues.DEFAULT_CONTENT_TYPE);
        }

        final RequestInfo requestInfo = requestMap.get(key);
        final int requestHandlerIndex = requestInfo.getRequestHandlerIndex();


        try{

            final String preInterceptKey = getInterceptorKey(MethodType.PRE_INTERCEPT,method.toString(),requestHandlerIndex);
            if(interceptMap.containsKey(preInterceptKey)){
                final InterceptorInfo preInterceptorInfo = interceptMap.get(preInterceptKey);
                final InvokeResponse preInvokeResponse = invoke(reqId, startTime, requestInfo,preInterceptorInfo.getInvokationMethod(), req,ctx, queryStringDecoder,false);
                if(!preInvokeResponse.isOk() && preInterceptorInfo.isBreakOnException()){
                    return preInvokeResponse.getHttpResponse();
                }
            }



            // ------------------------------------
            final InvokeResponse invokeResponse = invoke(reqId, startTime, requestInfo,requestInfo.getInvokationMethod(), req,ctx, queryStringDecoder,true);
            if(!invokeResponse.isOk() && requestInfo.isBreakOnException()){
                return invokeResponse.getHttpResponse();
            }
            // ------------------------------------


            final String postInterceptKey = getInterceptorKey(MethodType.POST_INTERCEPT,method.toString(),requestHandlerIndex);
            if(interceptMap.containsKey(postInterceptKey)){
                final InterceptorInfo postInterceptorInfo = interceptMap.get(postInterceptKey);
                final InvokeResponse postInvokeResponse = invoke(reqId, startTime, requestInfo,postInterceptorInfo.getInvokationMethod(), req,ctx, queryStringDecoder,false);
                if(!postInvokeResponse.isOk() && postInterceptorInfo.isBreakOnException()){
                    return postInvokeResponse.getHttpResponse();
                }
            }

            return invokeResponse.getHttpResponse();

        } catch (Exception e){ // todo : catch each exception in each part separately ...
            logger.error("UnknownException occurred while processing request: " + requestInfo + " ; reason: " + e.getMessage());
        }


        return getResponse(requestInfo,null,HttpResponseStatus.INTERNAL_SERVER_ERROR);



    }




    private InvokeResponse invoke(final String reqId, final long startTime, final RequestInfo requestInfo, final Method method, final FullHttpRequest req, final ChannelHandlerContext ctx, final QueryStringDecoder queryStringDecoder, final boolean mainMethod){

        final int requestHandlerIndex = requestInfo.getRequestHandlerIndex();



        try{

            final Object[] params = getInvokeParams(reqId,startTime,method,req,ctx,queryStringDecoder);
            final Object response = method.invoke(httpRequestHandlers.get(requestHandlerIndex), params);
            if(mainMethod){
                final AbstractHttpResponse httpResponse =  getResponse(requestInfo,response,HttpResponseStatus.OK);
                return new InvokeResponse(true,httpResponse);
            } else{
                return new InvokeResponse(true,null); // todo : what if we want to process pre/post handler's repsonse ?
            }


        } catch (InvocationTargetException e){
            final Throwable throwable = e.getCause();
            if (throwable instanceof HttpRequestException) {
                final HttpRequestException httpRequestException = (HttpRequestException) throwable;
                logger.info("{{}} got HttpRequestException while invoking: {}",reqId,requestInfo);
                final AbstractHttpResponse httpResponse = getResponse(requestInfo,httpRequestException.getHttpResponse(),httpRequestException.getHttpResponse().getStatus());
                return new InvokeResponse(false,httpResponse);
            } else{
                logger.error("{{}} InvocationTargetException occurred while invoking: {}; reason: {}",reqId,requestInfo,e.getMessage());
            }
        } catch (Throwable e){
            logger.error("{{}} UnknownException occurred while invoking:{} ; reason: {}",reqId,requestInfo,e.getMessage());
        }


        final AbstractHttpResponse httpResponse = getResponse(requestInfo,null,HttpResponseStatus.INTERNAL_SERVER_ERROR);
        return new InvokeResponse(false,httpResponse);

    }


    /* deprecated ...
    private InvokeResponse _call(final String reqId, final long startTime, final RequestInfo requestInfo, final FullHttpRequest req, final ChannelHandlerContext ctx, final QueryStringDecoder queryStringDecoder){

        final int requestHandlerIndex = requestInfo.getRequestHandlerIndex();



        try{

            final Object[] params = getInvokeParams(reqId,startTime,requestInfo.getInvokationMethod(),req,ctx,queryStringDecoder);
            final Object response = requestInfo.getInvokationMethod().invoke(httpRequestHandlers.get(requestHandlerIndex), params);
            final AbstractHttpResponse httpResponse =  getResponse(requestInfo,response,HttpResponseStatus.OK);
            return new InvokeResponse(true,httpResponse);

        } catch (InvocationTargetException e){
            final Throwable throwable = e.getCause();
            if (throwable instanceof HttpRequestException) {
                final HttpRequestException httpRequestException = (HttpRequestException) throwable;
                logger.info("{{}} got HttpRequestException while invoking: {}; gonna return: {}",reqId,requestInfo,httpRequestException);
                final AbstractHttpResponse httpResponse = getResponse(requestInfo,httpRequestException.getHttpResponse(),httpRequestException.getHttpResponse().getStatus());
                return new InvokeResponse(false,httpResponse);
            } else{
                logger.error("{{}} InvocationTargetException occurred while invoking: {}; reason: {}",reqId,requestInfo,e.getMessage());
            }
        } catch (Throwable e){
            logger.error("{{}} UnknownException occurred while invoking:{} ; reason: {}",reqId,requestInfo,e.getMessage());
        }


        final AbstractHttpResponse httpResponse = getResponse(requestInfo,null,HttpResponseStatus.INTERNAL_SERVER_ERROR);
        return new InvokeResponse(false,httpResponse);

    }
    */


    // todo : create a private method called _invoke ...
    private Object[] getInvokeParams(final String reqId, final long startTime, final Method method, final FullHttpRequest req, final ChannelHandlerContext ctx, final QueryStringDecoder queryStringDecoder) throws Exception {

        final String path = queryStringDecoder.path();


        //final Annotation[][] annotationArr = requestInfo.getInvokationMethod().getParameterAnnotations();
        final Annotation[][] annotationArr = method.getParameterAnnotations();

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
                } else if(annotation instanceof RequestTime){
                    paramVal[i] = startTime;
                }
            }

        }

        return paramVal;

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

    public String getInterceptorKey(final MethodType type, final String method, final int requestHandlerIndex){
        return type + "-" + method + ":" + requestHandlerIndex;
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

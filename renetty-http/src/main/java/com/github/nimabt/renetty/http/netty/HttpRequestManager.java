package com.github.nimabt.renetty.http.netty;

import com.github.nimabt.renetty.http.annotation.*;
import com.github.nimabt.renetty.http.model.*;
import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;
import com.github.nimabt.renetty.http.model.response.TextHttpResponse;
import com.github.nimabt.renetty.http.util.ConstValues;
import com.github.nimabt.renetty.http.util.HttpUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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


    private final MethodInvokeImpl methodInvokeImpl = new MethodInvokeImpl();


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



        /* deprecated
        final String key = getRequestKey(method.name(), path);
        if (!requestMap.containsKey(key)) {
            return new TextHttpResponse(HttpResponseStatus.NOT_FOUND, null, ConstValues.DEFAULT_CONTENT_TYPE);
        }

        final RequestInfo requestInfo = requestMap.get(key);
        final int requestHandlerIndex = requestInfo.getRequestHandlerIndex();
        final HttpRequestHandler httpRequestHandler = httpRequestHandlers.get(requestHandlerIndex);
        */

        final IncomingRequestInfo incomingRequestInfo = getRequestInfo(method.name(), path);
        if(incomingRequestInfo == null){
            return new TextHttpResponse(HttpResponseStatus.NOT_FOUND, null, ConstValues.DEFAULT_CONTENT_TYPE);
        }

        final RequestInfo requestInfo = incomingRequestInfo.getRequestInfo();
        final Map<String,String> pathVariables = incomingRequestInfo.getPathVariables();
        final int requestHandlerIndex = requestInfo.getRequestHandlerIndex();
        final HttpRequestHandler httpRequestHandler = httpRequestHandlers.get(requestHandlerIndex);



        try{

            final String preInterceptKey = getInterceptorKey(MethodType.PRE_INTERCEPT,method.toString(),requestHandlerIndex);
            if(interceptMap.containsKey(preInterceptKey)){
                final InterceptorInfo preInterceptorInfo = interceptMap.get(preInterceptKey);
                final InvokeResponse preInvokeResponse = methodInvokeImpl.invoke(httpRequestHandler,preInterceptorInfo.getInvokationMethod(), req,ctx,queryStringDecoder,requestInfo,reqId, startTime,  MethodType.PRE_INTERCEPT,null, pathVariables);
                if(!preInvokeResponse.isOk() && preInterceptorInfo.isBreakOnException()){
                    return preInvokeResponse.getHttpResponse();
                }
            }



            final InvokeResponse invokeResponse = methodInvokeImpl.invoke(httpRequestHandler,requestInfo.getInvokationMethod(),req,ctx, queryStringDecoder,requestInfo, reqId, startTime, MethodType.MAIN_METHOD,null, pathVariables);
            if(!invokeResponse.isOk() && requestInfo.isBreakOnException()){
                return invokeResponse.getHttpResponse();
            }


            final String postInterceptKey = getInterceptorKey(MethodType.POST_INTERCEPT,method.toString(),requestHandlerIndex);
            if(interceptMap.containsKey(postInterceptKey)){
                final InterceptorInfo postInterceptorInfo = interceptMap.get(postInterceptKey);
                final InvokeResponse postInvokeResponse = methodInvokeImpl.invoke(httpRequestHandler,postInterceptorInfo.getInvokationMethod(),req,ctx, queryStringDecoder, requestInfo, reqId, startTime, MethodType.POST_INTERCEPT, invokeResponse.getHttpResponse(), pathVariables);
                if(!postInvokeResponse.isOk() && postInterceptorInfo.isBreakOnException()){
                    return postInvokeResponse.getHttpResponse();
                }
            }

            return invokeResponse.getHttpResponse();

        } catch (Exception e){ // todo : catch each exception in each part separately ...
            logger.error("UnknownException occurred while processing request: " + requestInfo + " ; reason: " + e.getMessage());
        }


        return HttpUtil.getResponse(requestInfo,null,HttpResponseStatus.INTERNAL_SERVER_ERROR);



    }









    // todo : rethink about the concept of key (and it's style)
    private String getRequestKey(final String method, final String uri) {
        return method + ":" + uri;
    }

    public String getInterceptorKey(final MethodType type, final String method, final int requestHandlerIndex){
        return type + "-" + method + ":" + requestHandlerIndex;
    }


    // todo : throw exception when not found ?
    private IncomingRequestInfo getRequestInfo(final String methodName, final String uri){

        for(final Map.Entry<String,RequestInfo> requestItem : requestMap.entrySet()){
            final Map<String, String> pathVariables = new HashMap<String,String>();
            // todo: ...
            if(
                    (methodName.equalsIgnoreCase(requestItem.getValue().getRequestMethod().toString())) &&
                    (requestItem.getValue().getUriTemplate().match(uri,pathVariables))
                ){
                return new IncomingRequestInfo(requestItem.getValue(),pathVariables);
            }
        }
        return null;

    }









}

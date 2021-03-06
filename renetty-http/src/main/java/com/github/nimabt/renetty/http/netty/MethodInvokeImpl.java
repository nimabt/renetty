package com.github.nimabt.renetty.http.netty;

import com.github.nimabt.renetty.http.annotation.*;
import com.github.nimabt.renetty.http.exception.HttpRequestException;
import com.github.nimabt.renetty.http.model.*;
import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;
import com.github.nimabt.renetty.http.model.response.BinaryHttpResponse;
import com.github.nimabt.renetty.http.model.response.RedirectHttpResponse;
import com.github.nimabt.renetty.http.model.response.TextHttpResponse;
import com.github.nimabt.renetty.http.util.HttpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author: nima.abt
 * @since: 5/17/17
 */
public class MethodInvokeImpl {

    private final Logger logger = LoggerFactory.getLogger(MethodInvokeImpl.class);


    public MethodInvokeImpl(){

    }




    public InvokeResponse invoke(
            final HttpRequestHandler httpRequestHandler,
            final Method method,
            final FullHttpRequest req,
            final ChannelHandlerContext ctx,
            final QueryStringDecoder queryStringDecoder,
            final RequestInfo requestInfo,
            final String reqId, final long startTime,
            final MethodType methodType, final AbstractHttpResponse mainResponse,
            final Map<String,String> pathVariables
    ){



        try{

            final Object[] params = getInvokeParams(method,req,ctx,queryStringDecoder,methodType,reqId,startTime,mainResponse, pathVariables);
            final Object response = method.invoke(httpRequestHandler, params);
            if(methodType.equals(MethodType.MAIN_METHOD)){
                final AbstractHttpResponse httpResponse =  HttpUtil.getResponse(requestInfo,response, HttpResponseStatus.OK);
                return new InvokeResponse(true,httpResponse);
            } else{
                return new InvokeResponse(true,null); // todo : what if we want to process pre/post handler's response ?
            }


        } catch (InvocationTargetException e){
            final Throwable throwable = e.getCause();
            if (throwable instanceof HttpRequestException) {
                final HttpRequestException httpRequestException = (HttpRequestException) throwable;
                logger.info("{{}} got HttpRequestException while invoking: {}",reqId,requestInfo);
                final AbstractHttpResponse httpResponse = HttpUtil.getResponse(requestInfo,httpRequestException.getHttpResponse(),httpRequestException.getHttpResponse().getStatus());
                return new InvokeResponse(false,httpResponse);
            } else{
                logger.error("{{}} InvocationTargetException occurred while invoking: {}; reason: {}",reqId,requestInfo,e.getMessage());
            }
        } catch (Throwable e){
            logger.error("{{}} UnknownException occurred while invoking:{} ; reason: {}",reqId,requestInfo,e.getMessage());
        }


        final AbstractHttpResponse httpResponse = HttpUtil.getResponse(requestInfo,null,HttpResponseStatus.INTERNAL_SERVER_ERROR);
        return new InvokeResponse(false,httpResponse);

    }



    protected Object[] getInvokeParams(
            final Method method,
            final FullHttpRequest req,
            final ChannelHandlerContext ctx,
            final QueryStringDecoder queryStringDecoder,
            final MethodType methodType,
            final String reqId, final long startTime,
            final AbstractHttpResponse mainResponse,
            final Map<String,String> pathVariables
    ) throws Exception {

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
                    paramVal[i] = HttpUtil.getIpAddr(ctx,req);
                } else if (annotation instanceof QueryParam) {
                    final QueryParam queryParam = (QueryParam) annotation;
                    //String value = null;
                    Object value = null;
                    if(queryStringDecoder.parameters()!=null && queryStringDecoder.parameters().size()>0){
                        if(queryStringDecoder.parameters().containsKey(queryParam.key())){
                            final List<String> values = queryStringDecoder.parameters().get(queryParam.key());
                            if(values.size()>0){
                                //value = values.get(values.size()-1);
                                value = ConvertUtils.convert(values.get(values.size()-1),method.getParameterTypes()[i]);
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
                } else if (annotation instanceof PathVariable) {
                    final PathVariable pathVariable = (PathVariable) annotation;
                    //String value = null;
                    Object value = null;
                    if(pathVariable!=null && pathVariables.size()>0){
                        if(pathVariables.containsKey(pathVariable.key())){
                            //value = pathVariables.get(pathVariable.key());
                            value = ConvertUtils.convert(pathVariables.get(pathVariable.key()),method.getParameterTypes()[i]);
                        }
                    }
                    paramVal[i] = value;
                }


                if(methodType.equals(MethodType.POST_INTERCEPT)){
                    if(mainResponse!=null){
                        if(annotation instanceof ResponseStatus){
                            paramVal[i] = mainResponse.getStatus().code();
                        } else if(annotation instanceof ResponseBody && mainResponse.getType().equals(DataType.TEXT)){
                            // todo: check this ...
                            if(mainResponse instanceof RedirectHttpResponse){
                                paramVal[i] = null;
                            } else{
                                paramVal[i] = ((TextHttpResponse) mainResponse).getBody();
                            }
                        } else if(annotation instanceof ResponseData && mainResponse.getType().equals(DataType.BINARY)){
                            paramVal[i] = ((BinaryHttpResponse) mainResponse).getData();
                        }
                    }
                }

            }

        }

        return paramVal;

    }













}

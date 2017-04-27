package com.github.nimabt.renetty.http.annotation;


import com.github.nimabt.renetty.http.model.DataType;
import com.github.nimabt.renetty.http.model.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpRequest {

    public RequestMethod method() default RequestMethod.GET;
    
    public String path();

    public DataType requestType() default DataType.TEXT;

    public DataType responseType() default DataType.TEXT;

    public String responseContentType() default "";



}

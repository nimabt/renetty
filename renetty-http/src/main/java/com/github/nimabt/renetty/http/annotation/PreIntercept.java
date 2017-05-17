package com.github.nimabt.renetty.http.annotation;

import com.github.nimabt.renetty.http.model.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: nima.abt
 * @since: 5/16/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreIntercept {

    public RequestMethod method() default RequestMethod.GET;

    boolean breakOnException() default false;


}

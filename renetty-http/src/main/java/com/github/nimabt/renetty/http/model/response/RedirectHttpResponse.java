package com.github.nimabt.renetty.http.model.response;

import com.github.nimabt.renetty.http.model.DataType;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 10/5/17
 */
public class RedirectHttpResponse extends AbstractHttpResponse {

    private final String url;

    public RedirectHttpResponse(final String url){
        super(HttpResponseStatus.MOVED_PERMANENTLY,"text/plain");
        this.url = url;
        addHeader("Location",url);
    }

    // todo: is it ok ? (or new type is required)
    public final DataType getType(){
        return DataType.TEXT;
    }

    @Override
    public String toString() {
        return "RedirectHttpResponse{" +
                super.toString() +
                ", url='" + url + '\'' +
                '}';
    }


}

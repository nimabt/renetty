package com.github.nimabt.renetty.http.model.response;

import com.github.nimabt.renetty.http.model.DataType;
import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;
import com.github.nimabt.renetty.http.util.ConstValues;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class TextHttpResponse extends AbstractHttpResponse {

    private final String body;

    public TextHttpResponse(final String body){
        this(HttpResponseStatus.OK, body, ConstValues.DEFAULT_CONTENT_TYPE);
    }


    public TextHttpResponse(final HttpResponseStatus status, final String body,  final String contentType){
        super(status,contentType);
        this.body = body;
    }


    public String getBody() {
        return body;
    }


    public final DataType getType() {
        return DataType.TEXT;
    }


    @Override
    public String toString() {
        return "TextHttpResponse{" +
                super.toString() +
                ", body='" + body + '\'' +
                '}';
    }

}

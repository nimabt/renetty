package com.github.nimabt.renetty.http.model;

import com.github.nimabt.renetty.http.util.ConstValues;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class BinaryHttpResponse extends AbstractHttpResponse {

    private final byte[] data;


    public BinaryHttpResponse(final HttpResponseStatus status, final String contentType){
        this(status,contentType, ConstValues.EMPTY_RESPONSE);
    }

    public BinaryHttpResponse(final HttpResponseStatus status, final String contentType, final byte[] data){
        super(status,contentType);
        this.data = data;
    }


    public byte[] getData() {
        return data;
    }


    public DataType getType() {
        return DataType.BINARY;
    }

    @Override
    public String toString() {
        return "BinaryHttpResponse{" +
                super.toString() +
                ", data='" + ((data!=null) ? "binary#" + data.length : null) + '\'' +
                '}';
    }

}

package com.github.nimabt.renetty.http.model.response;

import com.github.nimabt.renetty.http.model.DataType;
import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;
import com.github.nimabt.renetty.http.util.ConstValues;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class BinaryHttpResponse extends AbstractHttpResponse {

    private final byte[] data;

    public BinaryHttpResponse(final byte[] data){
        this(HttpResponseStatus.OK,data,ConstValues.DEFAULT_BIN_CONTENT_TYPE);
    }


    public BinaryHttpResponse(final HttpResponseStatus status, final byte[] data, final String contentType){
        super(status,contentType);
        this.data = data;
    }


    public byte[] getData() {
        return data;
    }


    public final DataType getType() {
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

package com.github.nimabt.renetty.example.http.simple;

import com.github.nimabt.renetty.http.annotation.*;
import com.github.nimabt.renetty.http.exception.HttpRequestException;
import com.github.nimabt.renetty.http.model.*;
import com.github.nimabt.renetty.http.model.response.AbstractHttpResponse;
import com.github.nimabt.renetty.http.model.response.BinaryHttpResponse;
import com.github.nimabt.renetty.http.model.response.TextHttpResponse;
import com.github.nimabt.renetty.http.netty.HttpRequestHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * @author: nima.abt
 * @since: 4/25/17
 */
public class TestHttpHandler implements HttpRequestHandler {

    public TestHttpHandler(){

    }

    private static final String TEST_BIN_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4QQbBh0DAbxVzQAABJtJREFUWMPtl1tsVFUUhr+9z5kpNAGjSecQZtpaDWXaQjRAxQLqA0EDauQeUwpBpC2h3CvR+GL0SY2FhhZ74aLGQiKgER+qaXwyGG7DC4G21HDrtCMzgw9Sy3Qu52wfhpHSmakDlCddyUnOOWtl77XWXuv/14b/uohMDfdVGVS2+gGoW5kzreeKKNR1cgUQjeGd8pS49M4R/0WAL6tyWNsazGhd/d8MdpcbbD/sp8+rytaUOGqL3WKZ0ymYXSqYMCFuMzAAfT7Fx8sMurqtY1e8og44VV/uYNvhwMM5YCnl2lDqOF7kljN2bJbY7RCLJds9M12g6xCJaMvbO6zl1Tcd54DFQN8DH8Gm53PW5LrkVxvWaWgaKJXhuQqImdB8wKTPp1Y3ngy0pbPV0ik2lDrenzdHa1hXoWFZ919cUsC8MsnAXyx1mNkRj2/wRMYObCpzrHmhTDYseU0SjT54hZsmlBQJbt9mvlPPvnzGO3h+1CPYXW6AUq5IVHi3bdSIRMam1ex22N1oMm685RJK9G8dVpgiRerPffKhPkPKse1304R3P4h5WjyB0pRdsK/KwOulrNgtZtxPwWUqug4vzZWzJhnG7FynOr2+NZ6Ff+KsbPVzrVfVLlwgx3xziAe06GXJ1auqNrH5PQ4AFLvFMrt99Cg0Lb1e0+I26SQrC4rcYsU93ZJ4qVuZM+3JPJESZAB+90N9k0XjPotQKFl/OwSNrRb1n1vc8KdeIxaD/FzBntWO4iQHeq6IQtdkkbaKv2iz2LlZsvFtSdNBC9uwSG06NB2wqKmU7NwiOdhmkpUmky6n4EInU5Ic0HVyE9g+UsIRqKmUhIbi32vLJTFzWGQmvLUqXjuhIaip1BgKp15r4gSw6eQlc4FKD8sCeGzi3WJ64vF7u2Tkv4RtWuwXd/eSw6Lw3hp49Pz/5wBEo/QmOVD4tLjU71OP3IE+n2KaW/UkOVD7jf/CtV41ahuNBRhd71VsORTsTIkDXd3qWCr8Vwr8gcyz4w9AKutwGLq61dGUOHCw2qAgn13tHRZCJHvedsQi+IdK0o2cA4I3FYeOmOhasq69w6KgQNXtrzbSk1H1LIfn04/0mSPJSNOg+YDFJAPeeFWiSTDvzAmajLfi8XaLQACq10lMMyUZnW3xBJ5LT8dvOhAS11BYeLfXJNOxrsPV64ofOyyiMbDbBKCIRMFmg4ULJAX5yWhqt8OuRpPx45TTQvp2HL4x+ki2eY6joqxUfr3kdZkSmqWMb5joeyEgGiXl5KTr8N0PFqfOmhUNJ4OHMpqIzngHz+eY2ZFQiPnTS0RSOpWKp3T4k4pB7Xb49rjFiZPWe3tPB5vuayb0+AZPTNayL1/sVEtnPitHZcFUxWia0NBi0tVjrdp7Otj8wBeT+vIcZ3eP+P7FuXLWolckWWnG8kS6w+F4tf/yq3XW7WbxtraA76HuBQLR3+wJlBqGMfunn2O1xVPFirw8gWuyYOId8ro1AH39iuteRdcldbQgX3zW7AmcqZ9qjN3VbH+Vg8Qks6fCUXyhiyk2nTwBRGL0lhTx29a2QCfA/mqD9S1+/pdM5G8Tg83HN0tCsQAAAABJRU5ErkJggg==";


    @HttpRequest(method = RequestMethod.GET, path = "/test")
    public String testGet(){
        return "test response";
    }


    @HttpRequest(method = RequestMethod.POST , path="/test/post/data")
    public String testPostData(final @RequestBody String body, final @IpAddress String ipAddress){
        return "post got value: " + body + " ,from: " + ipAddress;
    }


    @HttpRequest(method = RequestMethod.GET, path="/test/binary/download", responseContentType = "image/png")
    public byte[] testBinaryDownload() throws Exception{
        final byte[] data = Base64.decodeBase64(TEST_BIN_IMAGE_BASE64);
        return data;
    }


    @HttpRequest(method = RequestMethod.POST , path="/test/binary/upload" , requestType = DataType.BINARY)
    public String testUploadBinary(final @RequestData() byte[] data){
        return "got binary data of size: " + ((data!=null) ? data.length : "0");
    }


    @HttpRequest(method = RequestMethod.GET , path="/test/query-string")
    public String testQueryParam(final @QueryParam(key = "item1") String item1Val, final @QueryParam(key = "item2") Float item2Val){
        return "got queryString with param(String:item1) = " + item1Val + " , param(Float:item2) = " + item2Val;
    }



    @HttpRequest(method = RequestMethod.GET, path="/test/httpresp/500")
    public String testCustomHttpResponse() throws HttpRequestException {
        throw new HttpRequestException(HttpResponseStatus.INTERNAL_SERVER_ERROR,"this is the internal server error response !");
    }



    @HttpRequest(method = RequestMethod.GET, path="/test/header")
    public String testHeader(
            final @RequestHeader(key = "X-Auth-Token") String authToken,
            final @RequestHeader(key = "Content-Type") String contentType
    ){
        return "got request with header(X-Auth-Token): " + authToken + " , header(Content-Type): " + contentType;
    }



    @HttpRequest(method = RequestMethod.GET , path="/test/contenttype" , responseContentType = "application/json")
    public String testContentType(){
        return "{\"key1\": \"value1\" , \"key2\" : \"value2\"}";
    }





    @HttpRequest(method = RequestMethod.GET , path="/test/obj/text-http-response")
    public TextHttpResponse testTextHttpResponseObj(){
        return new TextHttpResponse("just a test");
    }

    @HttpRequest(method = RequestMethod.GET , path="/test/obj/binary-http-response")
    public BinaryHttpResponse testBinaryHttpResponseObj(){
        final byte[] data = Base64.decodeBase64(TEST_BIN_IMAGE_BASE64);
        return new BinaryHttpResponse(HttpResponseStatus.OK,data,"image/png");
    }

    @HttpRequest(method = RequestMethod.GET , path="/test/obj/complex")
    public AbstractHttpResponse testComplexResponse(final @QueryParam(key = "type") String type){
        if(StringUtils.isBlank(type) || type.equalsIgnoreCase("text")){
            return new TextHttpResponse("text response");
        } else{
            final byte[] data = Base64.decodeBase64(TEST_BIN_IMAGE_BASE64);
            return new BinaryHttpResponse(HttpResponseStatus.OK,data,"image/png");
        }
    }



    @HttpRequest(method = RequestMethod.GET , path="/test/path-variable/{pathVariable1}/{pathVariable2}")
    public String testPathVariable(@PathVariable(key="pathVariable1") String pathVariable1, @PathVariable(key="pathVariable2") int pathVariable2){
        return "pathVariables: { (String:pathVariable1: " + pathVariable1 + ") , (int:pathVariable2: " + pathVariable2 + ") }";
    }



}

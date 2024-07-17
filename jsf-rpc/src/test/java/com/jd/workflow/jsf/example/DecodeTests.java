package com.jd.workflow.jsf.example;

import com.jd.jsf.gd.codec.msgpack.MsgpackCodec;
import com.jd.jsf.gd.msg.Invocation;
import com.jd.jsf.gd.protocol.JSFProtocol;
import com.jd.jsf.gd.protocol.ProtocolFactory;
import com.jd.jsf.gd.util.Constants;
//import io.netty.buffer.Unpooled;

import java.util.Base64;

public class DecodeTests {
    public static void main(String[] args) {
        String reqBodyStr = "l5HC2gAvY29tLmpkLndvcmtmbG93LmpzZi5zZXJ2aWNlLnRlc3QuSVBlcnNvblNlcnZpY2WmY2VudGVyp21hcFR5cGWTsGphdmEubGFuZy5TdHJpbmeyamF2YS5sYW5nLlN0cmluZ1tdsmphdmEubGFuZy5PYmplY3RbXZOnbWFwVHlwZZGtamF2YS51dGlsLk1hcJGSojM1mcKSoTmmcGVyc29ukqIzNZnCkqE5pG5hbWWSoTmyemhhbmdnMjFnZW5lcmljb2JqkqE5omlkkqEzAZKhOaVjbGFzc5KhOdoAJ2NvbS5qZC53b3JrZmxvdy5qc2Yuc2VydmljZS50ZXN0LlBlcnNvbpKhOaNhZ2WSoTMMkqE5pG5hbWWSoTmyemhhbmdnMjFnZW5lcmljb2JqkqE5omlkkqEzAZKhOaNhZ2WSoTMMl8KSoTmuX2NvbnN1bWVyQWxpYXOSoTmpSlNGXzAuMC4xkqE5ql9yZXRyeVRpbWWSoTMAkqE5p2dlbmVyaWOSoTDD";

        byte[] msgBody = Base64.getDecoder().decode(reqBodyStr);

        JSFProtocol jsfProtocol = (JSFProtocol) ProtocolFactory.getProtocol(1,10);//new JSFProtocol(Constants.CodecType.msgpack);
        MsgpackCodec codec = new MsgpackCodec();
        String invocationClassName = Constants.INVOCATION_CLASS_NAME;
        Object decode1 = codec.decode(msgBody, invocationClassName);
//        Object decode = jsfProtocol.decode(Unpooled.wrappedBuffer(msgBody), invocationClassName);
//        Invocation invocation = (Invocation) decode;
    }
}

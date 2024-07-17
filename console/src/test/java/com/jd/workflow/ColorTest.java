package com.jd.workflow;

import com.jd.workflow.console.utils.RpcUtils;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.output.HttpOutput;

import java.util.HashMap;
import java.util.Map;

//color接口测试
public class ColorTest {

    public static void main(String[] args) {
        HttpInput input=new HttpInput();
        input.setUrl("/queryMenu");
        Map<String, Object> ss = new HashMap<>();
        ss.put("functionId", "queryMenu");
        ss.put("appid", "japi-demo");
        ss.put("sign", "b71f119f9015c955b21af00b1c4479963a76b6a256cdbd3610711aa6aeb40b82");
        ss.put("t",System.currentTimeMillis());
        input.setParams(ss);
        input.setMethod("get");
        input.setReqType(ReqType.json);
        String targetAddress="http://beta-api.m.jd.com/queryMenu";
        String host="";
        HttpOutput out= RpcUtils.callHttp(input, targetAddress, host);
        String hostw="";
    }
}

package com.jd.workflow.soap.demo;

import com.jd.workflow.soap.wsdl.HttpDefinition;
import lombok.Data;

import java.util.List;

@Data
public class HttpResponse {
     HttpHeaders headers;
    Resp body;
    @Data
   public static class Resp {
        int code;
        String msg;
    }
    @Data
    public static class HttpHeaders{
        List<ParamItem> headers;
    }
}

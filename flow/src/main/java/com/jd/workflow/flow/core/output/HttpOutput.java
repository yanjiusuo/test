package com.jd.workflow.flow.core.output;

import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.input.HttpInput;
import lombok.Data;
import org.apache.http.entity.ContentType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class HttpOutput extends BaseOutput{
    //HttpInput input;

    int status;

    int time;

    int size;

    Map<String,Object> headers = new HashMap<>();

    // json or xml or string
    String contentType;
    HttpResponse response;
    public void copyFromResponse(){
        headers = response.getHeaders();
        body = response.body;
        status = response.status;
        time = response.time;
        size = response.size;
    }
    @Data
    public static class HttpResponse {
        int status;
        Map<String,Object> headers = new HashMap<>();
        Object body;
        int time;
        int size;
    }
}

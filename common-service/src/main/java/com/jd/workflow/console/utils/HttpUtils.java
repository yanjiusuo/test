package com.jd.workflow.console.utils;

import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class HttpUtils {
    public static void sendResponse(HttpOutput output,HttpServletResponse response) throws IOException {
        response.setStatus(output.getStatus());
        if(output.getHeaders() != null){
            for (Map.Entry<String, Object> entry : output.getHeaders().entrySet()) {
                if(entry.getKey().equalsIgnoreCase("Transfer-Encoding") || "Content-length".equalsIgnoreCase(entry.getKey())) continue;
                response.addHeader(entry.getKey(), Variant.valueOf(entry.getValue()).toString(""));
            }
        }
        OutputStream outputStream = response.getOutputStream();

        String contentType = output.getContentType();
        if(StringUtils.isBlank(contentType)){
            contentType = "application/json; charset=utf-8";
        }
        String body = null;
        if(output.getBody() != null){
            if(output.getBody() instanceof Map ||
                    output.getBody() instanceof Collection
            ){
                body = JsonUtils.toJSONString(output.getBody());
                contentType = "application/json; charset=utf-8";
            }else{
                if(StringUtils.isBlank(contentType)){
                    contentType = "text/plain; charset=utf-8";
                }
                body = Variant.valueOf(output.getBody()).toString();
            }
            response.setContentType(contentType);
            outputStream.write(body.getBytes("utf-8"));
            outputStream.flush();
        }else{
            response.setContentType("text/plain; charset=utf-8");
        }
    }

    public static void sendHttpJsonBody(Object data, HttpServletResponse response)  {
        response.setContentType("application/json; charset=utf-8");
        OutputStream writer = null;
        try {
            writer = response.getOutputStream();
            String str = JsonUtils.toJSONString(data);
            writer.write(str.getBytes("utf-8"));
            writer.flush();
            writer.close();
            response.flushBuffer();
        } catch (IOException e) {
            throw StdException.adapt(e);
        }

    }
}

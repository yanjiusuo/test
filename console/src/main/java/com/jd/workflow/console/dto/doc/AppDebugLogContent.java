package com.jd.workflow.console.dto.doc;

import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;

import java.util.Map;

@Data
public class AppDebugLogContent {
    Object responseBody;
    Object requestBody;
    Integer status;
    String method;
    String url;
    String reason;
    Map<String,Object> requestHeaders;
    Map<String,Object> responseHeaders;
    Map<String,Object> params;

    /**
     * 限制一下出入参的最大长度，防止内容过多
     */
    public void limitSize(){

        requestBody = truncateOrOriginal(requestBody);
        responseBody = truncateOrOriginal(responseBody);
    }
    private static Object truncateOrOriginal(Object obj){
        String req = Variant.valueOf(obj).toString();
        String truncateStr = StringHelper.truncateStr(req, 10000);
        if(req.length() == truncateStr.length()) return obj;
        return truncateStr;

    }
}

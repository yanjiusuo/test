package com.jd.workflow.console.dto.doc;

import com.jd.workflow.console.dto.manage.FilterRuleConfig;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Data
@Slf4j
public class ErrorReportDto {
    FilterRuleConfig config;
    String url;
    String op;
    Object responseBody;
    Object requestBody;
    String reqType;// json|form|text|xml|或者其他
    Integer status;
    String method;
    Map<String,Object> requestHeaders;
    Map<String,Object> responseHeaders;
    String ip;
    Map<String,Object> params;
    /**
     * @hidden
     */
    String domain;
    /**
     * @hidden
     */
    String urlPrefix;
    /**
     *错误原因
     */
    String reason;
    /**
     * @hidden
     */
    String path;
    public static String parseContentType(Map<String,Object> headers){
        if(headers == null){
            return null;
        }
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if("content-type".equalsIgnoreCase(entry.getKey())){
                if(entry.getValue()==null || !(entry.getValue() instanceof String)) return null;
                ContentType contentType = ContentType.create((String) entry.getValue());
                if(contentType.getMimeType().contains("json")) return "json";
                else if(contentType.getMimeType().contains("form")) return "form";
                else if(contentType.getMimeType().contains("xml")) return "xml";
                else if(contentType.getMimeType().contains("text")) return "text";
            }
        }
        return null;
    }
    public void init(){
        if(StringUtils.isNotEmpty(url)){
            if(url.startsWith("//")){
                url = "http:"+url;
            }
            try {
                URL urlInfo = new URL(url);
              domain =  urlInfo.getHost();
              path = urlInfo.getPath();
              reqType = parseContentType(requestHeaders);
              if("form".equals(reqType) && requestBody instanceof String){
                  this.requestBody = StringHelper.parseQuery((String)requestBody,"utf-8");
              }
              urlPrefix = urlInfo.getProtocol()+"//"+domain;
              if(!(urlInfo.getPort()==80 || urlInfo.getPort() == 443 || urlInfo.getPort() == -1)){
                  urlPrefix +=":"+urlInfo.getPort();
              }
              if(StringUtils.isNotEmpty(urlInfo.getQuery())){
                  this.params = StringHelper.parseQuery(urlInfo.getQuery(), "utf-8");
              }
            } catch (MalformedURLException e) {
                log.error("report.err_parse_url:url={}",url,e);
            }
        }
    }
}

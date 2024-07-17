package com.jd.workflow.console.filter;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dto.constant.SystemConstants;
import com.jd.workflow.console.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 项目名称：example
 * 类 名 称：RequestStreamFilter
 * 类 描 述：requeststream重复读取处理
 * 创建时间：2022-05-25 10:31
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@WebFilter(filterName = "requestStreamFilter" , urlPatterns = "/*")
public class RequestStreamFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            //为每一个请求创建一个ID，方便查找日志时可以根据ID查找出一个http请求所有相关日志
            MDC.put(SystemConstants.TRACE_ID_KEY, UUIDUtil.getUUID());
            ServletRequest requestWrapper = null;
            if (request instanceof HttpServletRequest) {// 这里不能调用getInputstream方法，调用该方法会使getParameterMap无法映射
                requestWrapper = new HttpServletRequestStreamWrapper((HttpServletRequest) request);
           /* String url = ((HttpServletRequest) request).getRequestURI();
            if(StringUtils.isNotBlank(((HttpServletRequest) request).getQueryString())){
                url +="?"+((HttpServletRequest) request).getQueryString();
            }
            log.info("request:uri={}",url);*/
            }


            if (requestWrapper == null) {
                chain.doFilter(request, response);

            } else {
                String url = ((HttpServletRequest) request).getRequestURI();
                if (StringUtils.isNotBlank(((HttpServletRequest) request).getQueryString())) {
                    url += "?" + ((HttpServletRequest) request).getQueryString();
                }
                log.info("request:uri={}", url);
                chain.doFilter(requestWrapper, response);
            }
        } finally {
            MDC.clear();
        }
    }
    /* *
     * 获取请求体内容
     * @return
     * @throws IOException
     */
    private String getParamsFromRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader br = null;
        String listString = "";
        try {
            br = request.getReader();
            String str = "";
            while ((str = br.readLine()) != null) {
                listString += str;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listString;
    }

    @Override
    public void destroy() {
    }
}
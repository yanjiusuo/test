package com.jd.workflow.console.filter;


import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.UserSessionLocal;
import lombok.extern.slf4j.Slf4j;

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
 * clearLoginStateFilter
 */
@Slf4j
@WebFilter(filterName = "clearLoginStateFilter", urlPatterns = "/*")
public class ClearLoginStateFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
        UserSessionLocal.removeUser();

    }


    @Override
    public void destroy() {
    }
}
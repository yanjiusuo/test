package com.jd.workflow.console.service.plugin.jdos;

import org.springframework.http.HttpMethod;

/**
 * <p>
 * jdos 调用
 * </p>
 *
 * @author sunchao81
 * @date 2022-09-19
 */
public interface JdosRest {

    /**
     *
     * @param url
     * @param method get post
     */
    public String invoke(String url, HttpMethod method, String jsonParams);

    /**
     *
     * @param url
     */
    public String invokeGet(String url);

    /**
     *
     * @param url
     */
    public String invokePost(String url,String jsonParams);

    /**
     *
     * @param url
     * @param tenant
     * @return
     */
    public String invokeGet(String url,String tenant);

    /**
     *
     * @param url
     * @param jsonParams
     * @param tenant
     * @return
     */
    public String invokePost(String url,String jsonParams,String tenant);
}

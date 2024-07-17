package com.jd.workflow.server.service;

import com.jd.workflow.server.dto.app.JsfAppInfo;
import com.jd.workflow.server.dto.*;

/**
 * 应用管理接口
 * @Api
 */
public interface AppRpcService {
    /**
     * 添加应用
     * @param info
     * @return
     */
    public QueryResult<Long> addApp(JsfAppInfo info);

    /**
     * 修改app
     * @param info
     * @return
     */
    public QueryResult<Boolean> modifyApp(JsfAppInfo info);



    /**
     * 查询app详情
     */
    public QueryResult<JsfAppInfo> findApp(String appCode);

}

package com.jd.workflow.console.service.doc;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.doc.SyncJsfDocLog;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/7
 */
public interface SyncJsfDocLogService extends IService<SyncJsfDocLog> {
    /**
     * 获取最近一条记录
     * @param codingAddress
     * @return
     */
    SyncJsfDocLog getLastJsfDocLogInfo(String codingAddress);
}

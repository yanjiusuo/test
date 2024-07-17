package com.jd.workflow.console.service.doc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.doc.SyncJsfDocLogMapper;
import com.jd.workflow.console.entity.doc.SyncJsfDocLog;
import com.jd.workflow.console.service.doc.SyncJsfDocLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 同步jsf文档日志 服务实现类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-06-07
 */
@Service
public class SyncJsfDocLogServiceImpl extends ServiceImpl<SyncJsfDocLogMapper, SyncJsfDocLog> implements SyncJsfDocLogService {

    public SyncJsfDocLog getLastJsfDocLogInfo(String codingAddress) {
        LambdaQueryWrapper<SyncJsfDocLog> lqw = new LambdaQueryWrapper();
        lqw.likeLeft(SyncJsfDocLog::getCodePath, codingAddress)
                .eq(SyncJsfDocLog::getYn, 1)
                .orderByDesc(SyncJsfDocLog::getCreated)
                .last("limit 1");
        SyncJsfDocLog syncJsfDocLog = getOne(lqw);
        return syncJsfDocLog;
    }
}

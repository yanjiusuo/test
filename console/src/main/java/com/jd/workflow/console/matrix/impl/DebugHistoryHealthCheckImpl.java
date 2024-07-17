package com.jd.workflow.console.matrix.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.common.util.StringUtils;
import com.jd.matrix.generic.annotation.GenericExtensionImpl;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.service.debug.FlowDebugLogService;
import com.jd.workflow.matrix.ext.spi.HealthCheckSPI;
import com.jd.workflow.method.MethodInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GenericExtensionImpl(bizCode = "healthCheck", bizName = "healthCheck", group = "jd")
public class DebugHistoryHealthCheckImpl implements HealthCheckSPI {
    public DebugHistoryHealthCheckImpl(){
        //System.out.println(123);
    }
    @Autowired
    FlowDebugLogService flowDebugLogService;

    @Override
    public Boolean isTypeValid(MethodInfo content) {
        Page<FlowDebugLog> result = flowDebugLogService.pageList(content.getId() + "", 1, 1);
        return result.getTotal() > 0L;
    }

    @Override
    public String getType() {
        return "debugHistory";
    }
}

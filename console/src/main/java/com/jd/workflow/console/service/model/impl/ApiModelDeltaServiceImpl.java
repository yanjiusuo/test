package com.jd.workflow.console.service.model.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.ApiModelDeltaMapper;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.ApiModelDelta;
import com.jd.workflow.console.service.model.IApiModelDeltaService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ApiModelDeltaServiceImpl extends ServiceImpl<ApiModelDeltaMapper, ApiModelDelta> implements IApiModelDeltaService {
    @Override
    public ApiModelDelta getByModelId(Long apiModelId) {
        LambdaQueryWrapper<ApiModelDelta> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ApiModelDelta::getApiModelId,apiModelId);
        lqw.eq(ApiModelDelta::getYn,1);
        return getOne(lqw);
    }

    @Override
    public List<ApiModelDelta> getByModelIds(List<Long> apiModelIds) {
        if(apiModelIds.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<ApiModelDelta> lqw = new LambdaQueryWrapper<>();
        lqw.in(ApiModelDelta::getApiModelId,apiModelIds);
        lqw.eq(ApiModelDelta::getYn,1);
        return list(lqw);
    }
}

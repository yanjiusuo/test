package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.CodingAppRelation;

public interface CodingAppRealtionService extends IService<CodingAppRelation> {
    String getByCodePath(String codePath);
}

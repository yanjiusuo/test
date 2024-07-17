package com.jd.workflow.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.server.dao.CamelStepLogMapper;
import com.jd.workflow.server.entity.CamelStepLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CamelStepLogService extends ServiceImpl<CamelStepLogMapper, CamelStepLogEntity> {
}

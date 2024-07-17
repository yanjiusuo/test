package com.jd.workflow.console.service.env.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.env.EnvConfigItemInterFaceMapper;
import com.jd.workflow.console.entity.env.EnvConfigItemInterface;
import com.jd.workflow.console.service.env.IEnvConfigItemInterfaceService;
import org.springframework.stereotype.Service;

/**
 * @author wufagang
 * @description
 * @date 2023年06月05日 20:53
 */
@Service
public class EnvConfigItemInterfaceServiceImpl extends ServiceImpl<EnvConfigItemInterFaceMapper, EnvConfigItemInterface> implements IEnvConfigItemInterfaceService {

    @Override
    public boolean updateYnByConfigId(Long envConfigId) {
        LambdaQueryWrapper<EnvConfigItemInterface> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnvConfigItemInterface::getEnvConfigId, envConfigId);
        EnvConfigItemInterface envConfigItemInterface = new EnvConfigItemInterface();
        envConfigItemInterface.setYn(DataYnEnum.INVALID.getCode());
        update(envConfigItemInterface,queryWrapper);
        return true;
    }
}

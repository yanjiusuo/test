package com.jd.workflow.console.service.env;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItemInterface;

/**
 * @author wufagang
 * @description
 * @date 2023年06月05日 20:46
 */
public interface IEnvConfigItemInterfaceService extends IService<EnvConfigItemInterface> {

    boolean updateYnByConfigId(Long envConfigId);
}

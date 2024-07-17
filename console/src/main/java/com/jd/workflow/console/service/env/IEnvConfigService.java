package com.jd.workflow.console.service.env;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.env.EnvConfigDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItem;

import java.util.List;

/**
 * @author wufagang
 * @description
 * @date 2023年06月05日 20:46
 */
public interface IEnvConfigService extends IService<EnvConfig> {
    public List<EnvConfig> getEnvConfigList(Long appId, Long demandSpaceId,String site);

    boolean saveConfig(List<EnvConfigDto> envConfigDto);

    boolean delConfig(Long envConfigId);
    //需求初始化默认环境
    public void initDemandConfig(Long requirementId);

    //应用初始化默认环境
    public void initAppConfig(String appCode, Long appId);

    List<EnvConfigItem> getEnvConfigItem(EnvConfigDto envConfigDto);

    void urlChange(EnvConfigDto envConfigDto);

    void initEnv(String appCode);


    void batchSaveItem(List<EnvModel> envModel, Long appId);
}

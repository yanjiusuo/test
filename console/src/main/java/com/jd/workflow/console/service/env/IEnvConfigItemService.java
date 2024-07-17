package com.jd.workflow.console.service.env;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItem;

import java.util.List;

/**
 * @author wufagang
 * @description
 * @date 2023年06月05日 20:46
 */
public interface IEnvConfigItemService extends IService<EnvConfigItem> {
    List<EnvConfigItem> getEnvConfigItemList(Long envConfigId);
    List<EnvConfigItem> getEnvConfigItemListByServiceName(Long envConfigId,String envServiceName);
    List<EnvConfigItem> getEnvConfigItemListByName(Long appId,Long requirementId,String env,String envConfigName);

    Boolean saveEnvConfigItem(List<EnvConfigItem> envConfigItemList, Long envConfigId);
    Boolean mergeEnvConfigList(List<EnvConfigItem> newConfigItems,List<EnvConfigItem> oldConfigItems, Long envConfigId);

    boolean updateYnByConfigId(Long envConfigId);

    public List<EnvConfigItem> listAppConfigItems(Long appId);
}

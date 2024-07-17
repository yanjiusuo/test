package com.jd.workflow.console.service.ducc;

import com.jd.workflow.console.entity.Menu;
import com.jd.workflow.soap.common.ducc.Item;

import java.util.List;
import java.util.Properties;

/**
 * DuccManagerServiceAdapter
 *
 * @author wangxianghui6
 * @date 2022/2/28 10:08 AM
 */
public interface DuccConfigServiceAdapter {

    /**
     * 创建配置
     */
    Long createConfig(String code, String name, String description);

    void deleteConfigItem(Long templateId, Integer site, String duccConfigItemId);

    void deleteConfig(String duccConfigCode);

    boolean isConfigExist(String duccConfigCode);

    boolean isConfigExist(Long templateId, Integer site, String duccConfigItemId);


    /**
     * 更新配置，即批量修改配置项
     * @param duccConfigId
     * @param properties
     * @return
     */
    Long updateProperties(Long duccConfigId, Properties properties, Integer site);

    public List<Item> getItems(String duccConfigId, String profile);

    public Properties queryItemByConfigId(String templateId, String profile, String duccConfigItemId);

    /**
     *
     * @param templateId
     * @param site
     * @param duccConfigItemId
     * @return
     */
    public Properties queryItemByConfigId(Long templateId, Integer site, String duccConfigItemId);



    public void releaseProfile(Long tempId, String profile);

    /**
     * 发布配置
     */
    void releaseProfile(Long duccConfigId, Integer site);

    /**
     * 通过ducc配置id获取配置下属性
     * @param duccConfigId
     * @return
     */
    Properties queryPropertiesByConfigId(Long duccConfigId);

    Properties queryPropertiesByConfigCode(String duccConfigCode);

    List<Menu> getItemsByConfigCode(String duccConfigCode);

    public Boolean createProfile(String duccConfigId, String profile);

}

package com.jd.workflow.console.service.env.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.env.EnvConfigItemMapper;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItem;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.env.IEnvConfigItemService;
import com.jd.workflow.console.service.env.IEnvConfigService;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.soap.common.exception.BizException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author wufagang
 * @description
 * @date 2023年06月05日 20:53
 */
@Service
public class EnvConfigItemServiceImpl extends ServiceImpl<EnvConfigItemMapper, EnvConfigItem> implements IEnvConfigItemService {
    public static String TEST_EASYMOCK_URL = "http://test.easymock.jd.com"+EasyMockRemoteService.NEW_METHOD_PREFIX;
    public static String ONLINE_EASYMOCK_URL = "http://easymock.jd.com"+EasyMockRemoteService.NEW_METHOD_PREFIX;
    @Autowired
    private IEnvConfigService envConfigService;

    @Autowired
    EasyMockRemoteService testEasyMockRemoteService;
    @Autowired
    private IAppInfoService appInfoService;
    @Autowired
    private EnvConfigItemMapper envConfigItemMapper;
    @Override
    public List<EnvConfigItem> getEnvConfigItemList(Long envConfigId) {
        if(Objects.isNull(envConfigId)) throw new BizException("环境信息参数不能为空");
        EnvConfig byId = envConfigService.getById(envConfigId);
        if(Objects.isNull(byId)) throw new BizException("未查询到环境信息");
        LambdaQueryWrapper<EnvConfigItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvConfigItem::getYn, DataYnEnum.VALID.getCode());
        wrapper.eq(EnvConfigItem::getEnvConfigId,envConfigId);
        List<EnvConfigItem> envConfigItems = envConfigItemMapper.selectList(wrapper);
        if(CollectionUtils.isEmpty(envConfigItems)){
            LambdaQueryWrapper<EnvConfigItem> wrapperApp = new LambdaQueryWrapper<>();
            wrapperApp.eq(EnvConfigItem::getYn, DataYnEnum.VALID.getCode());
            wrapperApp.eq(EnvConfigItem::getEnvConfigId,byId.getAppEnvId());
            envConfigItems=envConfigItemMapper.selectList(wrapperApp);
        }
        Long appId = byId.getAppId();
        AppInfo appInfo = appInfoService.getById(appId);
        EnvConfigItem configItem = new EnvConfigItem();
        configItem.setEnvConfigId(envConfigId);
        configItem.setServiceName("默认");
        if("测试MOCK".equals(byId.getEnvName())){

              configItem.setUrl(TEST_EASYMOCK_URL+testEasyMockRemoteService.buildAppUrlPrefix(appInfo));
              if(envConfigItems.isEmpty()){
                  save(configItem);
                  envConfigItems.add(configItem);
              }
         }else if("线上MOCK".equals(byId.getEnvName())){
            configItem.setUrl(ONLINE_EASYMOCK_URL+testEasyMockRemoteService.buildAppUrlPrefix(appInfo)); //
            if(envConfigItems.isEmpty()){
                save(configItem);
                envConfigItems.add(configItem);
            }
         }

        return envConfigItems;
    }

    @Override
    public List<EnvConfigItem> getEnvConfigItemListByServiceName(Long envConfigId,String serviceName) {
        LambdaQueryWrapper<EnvConfigItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(EnvConfigItem::getYn, DataYnEnum.VALID.getCode());
        itemWrapper.eq(EnvConfigItem::getEnvConfigId,envConfigId);
        itemWrapper.eq(EnvConfigItem::getServiceName,serviceName);
        return envConfigItemMapper.selectList(itemWrapper);
    }
    @Override
    public List<EnvConfigItem> getEnvConfigItemListByName(Long appId,Long requirementId,String env,String envConfigName) {
        if(appId == null) return Collections.emptyList();
        if(Objects.isNull(envConfigName)) throw new BizException("环境信息参数不能为空");
        LambdaQueryWrapper<EnvConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvConfig::getYn, DataYnEnum.VALID.getCode());
        if(Objects.nonNull(requirementId)) {
            wrapper.eq(EnvConfig::getRequirementId, requirementId);
        }
        else {
            wrapper.isNull(EnvConfig::getRequirementId);
        }
        wrapper.eq(EnvConfig::getAppId, appId);
        wrapper.eq(EnvConfig::getSite, env);
        wrapper.eq(EnvConfig::getEnvName, envConfigName);
        final List<EnvConfig> list = envConfigService.list(wrapper);
        if(list.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<EnvConfigItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(EnvConfigItem::getYn, DataYnEnum.VALID.getCode());
        itemWrapper.eq(EnvConfigItem::getEnvConfigId,list.get(0).getId());
        return envConfigItemMapper.selectList(itemWrapper);
    }

    @Override
    public Boolean saveEnvConfigItem(List<EnvConfigItem> envConfigItemList, Long envConfigId) {
        if(CollectionUtils.isEmpty(envConfigItemList)) return true;
        try {
            envConfigItemList.stream().forEach(item -> {
                item.setYn(1);
                item.setEnvConfigId(envConfigId);
                if(Objects.isNull(item.getId())){
                    envConfigItemMapper.insert(item);
                }else {
                    envConfigItemMapper.updateById(item);
                }
            });
            return true;
        }catch (Exception e){
            throw new BizException("保存配置明细错误",e);
        }
    }
    private EnvConfigItem findById(List<EnvConfigItem> list,Long id){
        for (EnvConfigItem configItem : list) {
            if(id.equals(configItem.getId())){
                return configItem;
            }
        }
        return null;
    }
    @Override
    public Boolean mergeEnvConfigList(List<EnvConfigItem> newConfigItems, List<EnvConfigItem> oldConfigItems, Long envConfigId) {
        List<EnvConfigItem> addList = new ArrayList<>();
        List<Long> removeIds = new ArrayList<>();
        for (EnvConfigItem newConfigItem : newConfigItems) {
            if(newConfigItem.getId() == null){
                newConfigItem.setEnvConfigId(envConfigId);
                newConfigItem.setYn(1);
                addList.add(newConfigItem);
            }else{
                newConfigItem.setYn(1);
                updateById(newConfigItem);
            }
        }
        for (EnvConfigItem oldConfigItem : oldConfigItems) {
            final EnvConfigItem exist = findById(newConfigItems, oldConfigItem.getId());
            if(exist == null){
                removeIds.add(oldConfigItem.getId());
            }
        }
        if(!addList.isEmpty()){
            saveBatch(addList);
        }
        if(!removeIds.isEmpty()){
            LambdaQueryWrapper<EnvConfigItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(EnvConfigItem::getId,removeIds);
            remove(wrapper);
        }

        return true;
    }

    @Override
    public boolean updateYnByConfigId(Long envConfigId) {
        LambdaQueryWrapper<EnvConfigItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnvConfigItem::getEnvConfigId,envConfigId);
        EnvConfigItem envConfigItem = new EnvConfigItem();
        envConfigItem.setYn(DataYnEnum.INVALID.getCode());
        update(envConfigItem,queryWrapper);
        return true;
    }
    @Override
    public List<EnvConfigItem> listAppConfigItems(Long appId){
        LambdaQueryWrapper<EnvConfigItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(EnvConfigItem::getYn,1);
        lqw.inSql(EnvConfigItem::getEnvConfigId,"select id from env_config where yn=1 and app_id ="+appId);
        return list(lqw);
    }
}

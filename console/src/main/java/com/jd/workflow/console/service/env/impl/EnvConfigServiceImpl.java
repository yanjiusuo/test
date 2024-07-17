package com.jd.workflow.console.service.env.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.dao.mapper.env.EnvConfigMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.env.EnvConfigDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItem;
import com.jd.workflow.console.entity.env.EnvConfigItemInterface;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.env.IEnvConfigItemInterfaceService;
import com.jd.workflow.console.service.env.IEnvConfigItemService;
import com.jd.workflow.console.service.env.IEnvConfigService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wufagang
 * @description
 * @date 2023年06月05日 20:48
 */
@Service
public class EnvConfigServiceImpl extends ServiceImpl<EnvConfigMapper, EnvConfig>  implements IEnvConfigService {
    public static final String TEST_MOCK_URL = "";
    @Autowired
    private EnvConfigMapper envConfigMapper;
    @Autowired
    private IAppInfoService appInfoService;
    @Autowired
    private IEnvConfigItemService envConfigItemService;
    @Autowired
    private IEnvConfigItemInterfaceService envConfigItemInterfaceService;
    @Autowired
    private RequirementInterfaceGroupService requirementInterfaceGroupService;

    @Override
    public List<EnvConfig> getEnvConfigList(Long appId, Long requirementId,String site) {
        if(appId == null && Objects.isNull(requirementId)){
            throw new BizException("应用id和需求空间id不能同时为空");
        }
        if(Objects.nonNull(requirementId)){
            /*RequirementInfo byId = requirementInfoService.getFlowRequirementId(requirementId);
            if(Objects.isNull(byId)) {
                throw new BizException("未找到相应的需求空间信息");
            }*/
            EnvConfig build = EnvConfig.builder().requirementId(requirementId).envType(ConfigEnvTypeEnum.DEMAND.getCode()).build();
            build.setYn(DataYnEnum.VALID.getCode());
            List<EnvConfig> configInfo = getConfigInfo(build, false);
           // if(CollectionUtils.isEmpty(configInfo) || configInfo.size() == 0) {
                initDemandConfig(requirementId);
          //  }
            if(StringUtils.isNotEmpty(site)){
                build.setSite(site);
            }
            configInfo = getConfigInfo(build, false);
            return configInfo;
//            if(CollectionUtils.isEmpty(configInfo)) return configInfo;
//            Map<String, EnvConfig> configInfoMap = new HashMap<>();
//            configInfo.forEach(item -> {
//                configInfoMap.putIfAbsent(item.getSite()+item.getEnvName(),item);
//            });
//            return new ArrayList<>( configInfoMap.values());
        }else if(appId != null ){
            AppInfo appByCode = appInfoService.getById(appId);
            if(Objects.isNull(appByCode) || Objects.isNull(appByCode.getId())) {
                throw new BizException("未找到相应的应用信息");
            }
            EnvConfig build = EnvConfig.builder().appId(appId).envType(ConfigEnvTypeEnum.APP.getCode()).build();
            build.setYn(DataYnEnum.VALID.getCode());
            List<EnvConfig> configInfo = getConfigInfo(build, false);
            if(CollectionUtils.isEmpty(configInfo) || configInfo.size() == 0) {
                initAppConfig(appByCode.getAppCode(),appByCode.getId());
            }
            if(StringUtils.isNotEmpty(site)){
                build.setSite(site);
            }
            configInfo = getConfigInfo(build, false);
            return configInfo;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean saveConfig(List<EnvConfigDto> envConfigDtos) {
        try {
            for (EnvConfigDto envConfigDto : envConfigDtos) {
                AppInfo appByCode = appInfoService.getById(envConfigDto.getAppId());
                if(Objects.isNull(appByCode)) {
                    throw new BizException("未查询到应用信息");
                }
                int envType = ConfigEnvTypeEnum.APP.getCode();
                Long requirementId = envConfigDto.getEnvConfig().getRequirementId();
                if(Objects.nonNull(requirementId)){
                    envType = ConfigEnvTypeEnum.DEMAND.getCode();
                }
                envConfigDto.getEnvConfig().setEnvType(envType);
                if(envConfigDto.getEnvConfig().getId() == null ){
                    if(envConfigDto.getEnvConfig().getDefaultFlag() == null){
                        envConfigDto.getEnvConfig().setDefaultFlag(1); // 手动维护
                    }
                    if( envConfigDto.getEnvConfig().getMockFlag() == null){
                        envConfigDto.getEnvConfig().setMockFlag(0); // 默认不mock
                    }
                    envConfigDto.getEnvConfig().setAppCode(appByCode.getAppCode());
                }

                EnvConfig build = EnvConfig.builder().appId(envConfigDto.getAppId()).envName(envConfigDto.getEnvConfig().getEnvName()).requirementId(requirementId).envType(envType).build();
                build.setYn(DataYnEnum.INVALID.getCode());
                List<EnvConfig> configInfoListDB = getConfigInfo(build, false);
                if(CollectionUtils.isEmpty(configInfoListDB)){
                    save(envConfigDto.getEnvConfig());
                }else if(configInfoListDB.size()>19){
                    envConfigDto.getEnvConfig().setId(configInfoListDB.get(0).getId());
                }
                saveOrUpdate(envConfigDto.getEnvConfig());
                final List<EnvConfigItem> oldConfigItems = envConfigItemService.getEnvConfigItemList(envConfigDto.getEnvConfig().getId());
                envConfigItemService.mergeEnvConfigList(envConfigDto.getEnvConfigItemList(),oldConfigItems,envConfigDto.getEnvConfig().getId());
            }

            return true;
        }catch (Exception e){
            throw new BizException("保存环境信息失败",e);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delConfig(Long envConfigId) {
        EnvConfig envConfig = getById(envConfigId);
        if(Objects.isNull(envConfig)){
            throw new BizException("未找到相关的环境信息");
        }else if (envConfig.getDefaultFlag().equals(DefaultFlagEnum.AUTO.getCode())){
            throw new BizException("系统自动生成的环境不运行删除");
        }
        envConfig.setYn(DataYnEnum.INVALID.getCode());
        updateById(envConfig);
        envConfigItemService.updateYnByConfigId(envConfigId);
        envConfigItemInterfaceService.updateYnByConfigId(envConfigId);
        return true;
    }

    public EnvConfig initLocalEnvConfig(AppInfo appinfo,String ip,String envName){
        EnvConfig envConfig = new EnvConfig();
        envConfig.setEnvName(envName);
        envConfig.setAppId(appinfo.getId());
        envConfig.setEnvType(ConfigEnvTypeEnum.APP.getCode());
        envConfig.setYn(1);
        envConfig.setSite("test");
        envConfig.setAppCode(appinfo.getAppCode());
        envConfig.setDefaultFlag(1);
        envConfig.setMockFlag(0);
        save(envConfig);
        EnvConfigItem envConfigItem = new EnvConfigItem();
        envConfigItem.setEnvConfigId(envConfig.getId());
        envConfigItem.setServiceName("默认");
        envConfigItem.setUrl("http://test-local-debug.jd.com/"+ip+":[port]");
        envConfigItem.setDefaultFlag(1);
        envConfigItemService.save(envConfigItem);
        return envConfig;
    }

    public void updateMockUrl(){
        LambdaQueryWrapper<EnvConfigItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(EnvConfigItem::getYn,1);
        lqw.inSql(EnvConfigItem::getEnvConfigId,"select id from env_config where yn = 1 and mock_flag = 1");
        List<EnvConfigItem> items = envConfigItemService.list(lqw);
        for (EnvConfigItem item : items) {
            if(StringUtils.isNotBlank(item.getUrl())){
                if(item.getUrl().endsWith("/PAAS")){

                    EnvConfig config = getById(item.getEnvConfigId());
                    if(config == null){
                        continue;
                    }
                    AppInfo appInfo = appInfoService.getById(config.getAppId());

                    item.setUrl(item.getUrl().replace("/PAAS","/PAAS_APP"));
                    item.setUrl(item.getUrl() +"/"+ appInfo.getAppCode());
                    LambdaUpdateWrapper<EnvConfigItem> luw = new LambdaUpdateWrapper<>();
                    luw.eq(EnvConfigItem::getId,item.getId());
                    luw.set(EnvConfigItem::getUrl,item.getUrl());
                    envConfigItemService.update(luw);
                }else if(item.getUrl().contains("/PAAS/")){
                    EnvConfig config = getById(item.getEnvConfigId());
                    if(config == null){
                        continue;
                    }
                    AppInfo appInfo = appInfoService.getById(config.getAppId());
                    String prefix = item.getUrl().substring(0,item.getUrl().indexOf("/PAAS"));

                    item.setUrl(prefix+"/PAAS_APP/"+appInfo.getAppCode());
                    LambdaUpdateWrapper<EnvConfigItem> luw = new LambdaUpdateWrapper<>();
                    luw.eq(EnvConfigItem::getId,item.getId());
                    luw.set(EnvConfigItem::getUrl,item.getUrl());
                    envConfigItemService.update(luw);
                }

            }
        }
    }

    public void initAppConfig(String appCode, Long appId){
        initAppConfig(appCode,appId,null);
    }
    public void initDemandConfig(Long requirementId) {
        synchronized (requirementId){
            //需求关联应用code
            List<AppInfoDTO> appInfos = requirementInterfaceGroupService.getAppCodeByRequirementId(requirementId);
            List<Long> appCodeByRequirementId = appInfos.stream().map(AppInfoDTO::getId).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(appCodeByRequirementId)) {
                return;
            }
            EnvConfig envConfigCon = EnvConfig.builder().requirementId(requirementId).envType(ConfigEnvTypeEnum.DEMAND.getCode()).build();
            envConfigCon.setYn(1);
            List<EnvConfig> inited = getConfigInfo(envConfigCon, true);
            Set<Long> initedappCode = inited.stream().map(EnvConfig::getAppId).collect(Collectors.toSet());
            appCodeByRequirementId.removeAll(initedappCode);
            if(CollectionUtils.isEmpty(appCodeByRequirementId)) return;
            List<EnvConfig> envConfigTodoList = new ArrayList<>();
            for (Long appId: appCodeByRequirementId) {
                AppInfo app = appInfoService.getById(appId);
                if(Objects.isNull(app)) continue;
                EnvConfig build = EnvConfig.builder().appId(appId).envType(ConfigEnvTypeEnum.APP.getCode()).build();
                build.setYn(1);
                List<EnvConfig> configInfo = getConfigInfo(build, true);
                if(CollectionUtils.isEmpty(configInfo)) {
                    initAppConfig(app.getAppCode(), app.getId());
                    configInfo = getConfigInfo(build, true);
                }
                envConfigTodoList.addAll(configInfo);
            }
            if(CollectionUtils.isEmpty(envConfigTodoList)) {
                return;
            }
            envConfigTodoList.forEach(envConfig -> {
                envConfig.setEnvType(ConfigEnvTypeEnum.DEMAND.getCode());
                envConfig.setAppEnvId(envConfig.getId());
                envConfig.setRequirementId(requirementId);
                envConfig.setId(null);
                saveOrUpdate(envConfig);
            });
        }
    }

    public List<EnvConfig> getConfigInfo(EnvConfig envConfig, boolean isOnlyDefault) {
        if(Objects.isNull(envConfig.getEnvType())){
            throw new BizException("配置类型不能为空");
        }
        if(envConfig.getAppId() ==null && Objects.isNull(envConfig.getRequirementId())){
            throw new BizException("应用id和需求空间id不能同时为空");
        }
        LambdaQueryWrapper<EnvConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnvConfig::getYn, DataYnEnum.VALID.getCode());
        queryWrapper.eq(StringUtils.isNotBlank(envConfig.getEnvName()),EnvConfig::getEnvName, envConfig.getEnvName());
        queryWrapper.eq(EnvConfig::getEnvType,envConfig.getEnvType());
        if(envConfig.getAppId() != null){
            queryWrapper.eq(EnvConfig::getAppId,envConfig.getAppId());
        }
        if(Objects.nonNull(envConfig.getRequirementId())) {
            queryWrapper.eq(EnvConfig::getRequirementId, envConfig.getRequirementId());
        }
        if(StringUtils.isNotEmpty(envConfig.getSite())){
            queryWrapper.eq(EnvConfig::getSite,envConfig.getSite());
        }
        if(isOnlyDefault) {
            queryWrapper.eq(EnvConfig::getDefaultFlag, DefaultFlagEnum.AUTO.getCode());
        }
        return envConfigMapper.selectList(queryWrapper);
    }

    public void initAppConfig(String appCode, Long appId,Long requirement) {
        Map<String, List<String>> map = initData();
        synchronized (appCode){
            map.keySet().forEach(site -> {
                List<String> list = map.get(site);
                list.forEach(env -> {
                    EnvConfig envConfig = EnvConfig.builder().appCode(appCode)
                            .site(site).envName(env).appId(appId).build();
//                envConfig.setDemandSpaceId(demandSpaceId);
                    envConfig.setDefaultFlag(DefaultFlagEnum.AUTO.getCode());
                    if(env.endsWith("MOCK")){
                        envConfig.setMockFlag(MockFlagEnum.MOCK.getCode());
                    }else {
                        envConfig.setMockFlag(MockFlagEnum.NOTMOCK.getCode());
                    }
                    if(requirement != null) {
                        envConfig.setRequirementId(requirement);
                        envConfig.setEnvType(ConfigEnvTypeEnum.DEMAND.getCode());
                    }else{
                        envConfig.setEnvType(ConfigEnvTypeEnum.APP.getCode());
                    }

                    envConfigMapper.insert(envConfig);
                });
            });
        }
    }
    public Map<String,List<String>> initData(){
        Map<String,List<String>> map = new HashMap<>();
        map.put("China",Arrays.asList("线上环境,预发环境,线上MOCK".split(",")));
        map.put("test",Arrays.asList("测试环境,本地环境,测试MOCK".split(",")));
        return map;
    }
    public List<EnvConfigItem> getEnvConfigItems(Long envConfigId){
        LambdaQueryWrapper<EnvConfigItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(EnvConfigItem::getEnvConfigId,envConfigId);
        lqw.eq(EnvConfigItem::getYn,1);
        return envConfigItemService.list(lqw);
    }
    @Override
    public List<EnvConfigItem> getEnvConfigItem(EnvConfigDto envConfigDto) {
        //appCode 非空判断
        if(envConfigDto.getAppId() == null){
            throw new BizException("应用code不能为空");
        }
        if(StringUtils.isEmpty(envConfigDto.getEnvName())){
            throw new BizException("环境名称不能为空");
        }
        if(Objects.isNull(envConfigDto.getInterfaceManageId())) {
            throw new BizException("接口管理id不能为空");
        }
        LambdaQueryWrapper<EnvConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnvConfig::getYn, DataYnEnum.VALID.getCode());
        queryWrapper.eq(EnvConfig::getEnvName, envConfigDto.getEnvName());
        queryWrapper.eq(EnvConfig::getAppCode, envConfigDto.getAppId());
        if(Objects.nonNull(envConfigDto.getRequirementId())){ //需求页面
            queryWrapper.eq(EnvConfig::getEnvType,ConfigEnvTypeEnum.DEMAND.getCode());
            queryWrapper.eq(EnvConfig::getRequirementId, envConfigDto.getRequirementId());
       }else { //应用页面
            queryWrapper.eq(EnvConfig::getEnvType,ConfigEnvTypeEnum.APP.getCode());
        }
        EnvConfig envConfig;
        try {
            envConfig = envConfigMapper.selectOne(queryWrapper);
        }catch (Exception e){
            throw new BizException("未能正确获取唯一的环境信息",e);
        }
        if(Objects.isNull(envConfig)){
            return new ArrayList<>();
        }
        List<EnvConfigItem> envConfigItemList = envConfigItemService.getEnvConfigItemList(envConfig.getId());
        EnvConfig finalEnvConfig = envConfig;
        envConfigItemList.forEach(item ->{
            //处理是否选中
            LambdaQueryWrapper<EnvConfigItemInterface> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(EnvConfigItemInterface::getEnvConfigItemId, item.getId());
            lambdaQueryWrapper.eq(EnvConfigItemInterface::getEnvConfigId, finalEnvConfig.getId());
            lambdaQueryWrapper.eq(EnvConfigItemInterface::getInterfaceManageId,envConfigDto.getInterfaceManageId());
            EnvConfigItemInterface one = envConfigItemInterfaceService.getOne(lambdaQueryWrapper);
            if(!Objects.isNull(one)){
                item.setSelected(true);
            }
        });

        return envConfigItemList;
    }

    @Override
    public void urlChange(EnvConfigDto envConfigDto) {
        //appCode 非空判断
        if(envConfigDto.getAppId() == null){
            throw new BizException("应用id不能为空");
        }
        if(StringUtils.isEmpty(envConfigDto.getEnvName())){
            throw new BizException("环境名称不能为空");
        }
        if(Objects.isNull(envConfigDto.getInterfaceManageId())){
            throw new BizException("接口管理id不能为空");
        }
        if (Objects.isNull(envConfigDto.getEnvConfigItemId())){
            throw new BizException("环境配置详情id不能为空");
        }
        if (Objects.isNull(envConfigDto.getEnvConfigId())){
            throw new BizException("环境配置id不能为空");
        }
        EnvConfigItem envConfigItem = envConfigItemService.getById(envConfigDto.getEnvConfigItemId());
        if(Objects.isNull(envConfigItem)){
            throw new BizException("未找到相应到环境详情信息");
        }
        if(!envConfigItem.getEnvConfigId().equals(envConfigDto.getEnvConfigId())){
            throw new BizException("环境配置详情id与环境配置id不一致");
        }
        LambdaQueryWrapper<EnvConfigItemInterface> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EnvConfigItemInterface::getEnvConfigItemId, envConfigDto.getEnvConfigItemId());
        envConfigItemInterfaceService.remove(lambdaQueryWrapper);
        EnvConfigItemInterface envConfigItemInterface = EnvConfigItemInterface.builder().
                envConfigItemId(envConfigDto.getEnvConfigItemId()).
                envConfigId(envConfigDto.getEnvConfigId()).
                interfaceManageId(envConfigDto.getInterfaceManageId()).
                build();
        envConfigItemInterfaceService.save(envConfigItemInterface);
    }
    @Autowired
    private IInterfaceManageService interfaceManageService;
    @Override
    public void initEnv(String appCode) {
        LambdaQueryWrapper<InterfaceManage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        wrapper.eq(InterfaceManage::getType, InterfaceTypeEnum.HTTP.getCode());
        wrapper.isNotNull(InterfaceManage::getEnv);
        if(StringUtils.isNotEmpty(appCode)){
            AppInfo app = appInfoService.findApp(appCode);
            if(Objects.isNull(app)){
                return;
            }
            wrapper.eq(InterfaceManage::getAppId, app.getId());
        }
        List<InterfaceManage> list = interfaceManageService.list(wrapper);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.stream().filter(item -> Objects.nonNull(item.getAppId())).collect(Collectors.groupingBy(InterfaceManage::getAppId)).forEach((k,v) ->{
            AppInfo byId = appInfoService.getById(k);
            if(Objects.isNull(byId) || byId.getYn().equals(DataYnEnum.INVALID.getCode())){
                return;
            }
            List<EnvModel> modelList = str2Dto(v);
            dealAppEvn(byId,modelList);
        });
    }

    private void dealAppEvn(AppInfo byId, List<EnvModel> modelList) {
        //1。根据应用删除环境数据
        EnvConfig envConfig = new EnvConfig();
        envConfig.setEnvType(ConfigEnvTypeEnum.APP.getCode());
        envConfig.setAppCode(byId.getAppCode());
        List<EnvConfig> configInfo = getConfigInfo(envConfig, false);
        List<Long> envIds = configInfo.stream().map(EnvConfig::getId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(envIds)){
            removeByIds(envIds);
            //2。根据应用删除环境明细数据
            LambdaQueryWrapper<EnvConfigItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(EnvConfigItem::getEnvConfigId, envIds);
            envConfigItemService.remove(itemWrapper);
        }
        initAppConfig(byId.getAppCode(), byId.getId());
        //3。保存环境数据和环境明细数据
        modelList.forEach(envModel ->
            saveItem(envModel, byId,null)
        );

    }


    public void saveItem(EnvModel envModel, AppInfo appInfo,Long envConfigId) {
        List<String> urls = envModel.getUrl();
        if(CollectionUtils.isEmpty(urls)){
            return;
        }
        EnvConfig envConfig =  EnvConfig.builder().envType(ConfigEnvTypeEnum.APP.getCode())
                .id(envConfigId)
                .appCode(appInfo.getAppCode())
                .appId(appInfo.getId())
                .envName(envModel.getEnvName())
                .defaultFlag(1)
                .mockFlag(0)
                .site(envModel.getType().name().equals(EnvTypeEnum.TEST.name())?"test":"China").build();
        saveOrUpdate(envConfig);
        Long id = envConfig.getId();
        List<EnvConfigItem> urlList = new ArrayList<>(urls.size());
        //获取环境配置项
        List<EnvConfigItem> envItems= envConfigItemService.getEnvConfigItemListByServiceName(envConfigId,envModel.getEnvName());
        Map<String, List<EnvConfigItem>> envName2Item=new HashMap<String, List<EnvConfigItem>>();
        if(CollectionUtils.isNotEmpty(envItems)){
            envName2Item = envItems.stream().collect(Collectors.groupingBy(EnvConfigItem::getServiceName));
        }
        Map<String, List<EnvConfigItem>> finalName2Item = envName2Item;

        urls.forEach(url ->{
            EnvConfigItem envConfigItem = EnvConfigItem.builder().envConfigId(id)
                    .serviceName(envModel.getEnvName()).url(url)
                    .configJson(CollectionUtils.isEmpty(envModel.getHeaders())?null: JSON.toJSONString(envModel.getHeaders())).build();
            if(MapUtils.isNotEmpty(finalName2Item)){
                envConfigItem.setId(finalName2Item.get(envModel.getEnvName()).get(0).getId());
            }
            urlList.add(envConfigItem);
        });



        envConfigItemService.saveEnvConfigItem(urlList,id);
    }


    public Long queryConfigIdByName(EnvModel envModel, AppInfo appInfo){
        if (StringUtils.isNotEmpty(envModel.getEnvName())) {
            LambdaQueryWrapper<EnvConfig> wrapper = new LambdaQueryWrapper();
            wrapper.eq(EnvConfig::getEnvName, envModel.getEnvName());
            wrapper.eq(EnvConfig::getYn, 1);
            wrapper.eq(EnvConfig::getAppId, appInfo.getId());
            List<EnvConfig> configs = list(wrapper);
            if (configs.size() > 0) {
                return configs.get(0).getId();
            }
        }
        return null;
    }

    @Override
   public void batchSaveItem(List<EnvModel> envModel, Long appId) {
        AppInfo appInfo = new AppInfo();
        appInfo.setId(appId);
        try {
            AppInfoDTO dto = appInfoService.findApp(appId);
            appInfo.setAppCode(dto.getAppCode());
            for (EnvModel model : envModel) {
                Long id=queryConfigIdByName(model,appInfo);
                saveItem(model, appInfo,id);
            }
        } catch (Exception e) {
            log.error("保存环境信息异常",e);
        }
    }


    private List<EnvModel> str2Dto(List<InterfaceManage> stringList){
        List<EnvModel> envModelList = new ArrayList<>();
        if(CollectionUtils.isEmpty(stringList)){
            return envModelList;
        }
        stringList.forEach(item ->{
            String env = item.getEnv();
            if(StringUtils.isEmpty(env)){return;}
            List<EnvModel> envModelItem = JsonUtils.parseArray(env, EnvModel.class);
            envModelList.addAll(envModelItem);
        });
        return envModelList;
    }

    public static void main(String[] args) {
        String url = "http://test.easymock.jd.com/PAAS/模型";
        String prefix = url.substring(0,url.indexOf("/PAAS"));

        System.out.println(prefix+"/PAAS_APP/123");
    }
}

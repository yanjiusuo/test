package com.jd.workflow.console.service.ratelimiting;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.cjg.config.model.ducc.DuccResponse;
import com.jd.cjg.config.utils.DuccApiUtils;
import com.jd.workflow.console.dao.mapper.RateLimitingRulesMapper;
import com.jd.workflow.console.dto.ratelimiting.ClientRateLimitingConfigDTO;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingChangeStatusDTO;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingQueryDTO;
import com.jd.workflow.console.dto.ratelimiting.TotalRateLimitingConfigDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.AppInfoMembers;
import com.jd.workflow.console.entity.RateLimitingRules;
import com.jd.workflow.console.entity.RateLimitingRulesConfig;
import com.jd.workflow.console.service.IAppInfoMembersService;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.RateLimitingRulesConfigService;
import com.jd.workflow.console.service.RateLimitingRulesService;
import com.jd.workflow.console.service.ratelimiting.annotation.AddGlobalLog;
import com.jd.workflow.console.service.ratelimiting.annotation.AddLog;
import com.jd.workflow.console.service.ratelimiting.annotation.DeleteLog;
import com.jd.workflow.console.service.ratelimiting.annotation.UpdateLog;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务总线限流规则表 服务实现类
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-01-23
 */
@Service
@Slf4j
public class RateLimitingRulesServiceImpl extends ServiceImpl<RateLimitingRulesMapper, RateLimitingRules> implements RateLimitingRulesService {

    @Autowired
    RateLimitingRulesMapper rateLimitingRulesMapper;

    @Autowired
    IAppInfoMembersService appInfoMembersService;

    @Autowired
    RateLimitingRulesConfigService configService;

    @Autowired
    IAppInfoService appInfoService;

    @Autowired
    DuccApiNewUtils duccApiNewUtils;

    private static final String SITE_CODE = "China";
    private static final String SITE_NAME = "中国";
    private static final String NAMESPACE = "cjg_http_limit_namespace";
    private static final String CLIENT_PREFIX = "limit.client.";
    private static final String SEPERATOR = "##";


    @Override
    public Page<RateLimitingRules> listRules(RateLimitingQueryDTO queryDto) {
        if (StringUtils.isBlank(queryDto.getAppProvider())){
            throw newBizException("服务提供方code不能为空");
        }
        if (queryDto.getRuleType()==null || (queryDto.getRuleType()!=0 && queryDto.getRuleType()!=1)){
            throw newBizException("规则类型不正确：ruleType为0或者1");
        }
        checkUserPermission(queryDto.getAppProvider(), queryDto.getErp());
        LambdaQueryWrapper<RateLimitingRules> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RateLimitingRules::getAppProvider, queryDto.getAppProvider());
        lambdaQueryWrapper.eq(RateLimitingRules::getRuleType, queryDto.getRuleType());
        Page<RateLimitingRules> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        return rateLimitingRulesMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AddLog
    /**
     * 此方法有日志切面，修改方法签名时需要同步修改日志切面
     */
    public List<Long> addRules(List<RateLimitingRules> list, String erp) {
        //检查添加规则
        String appProvider = addRulesCheck(list, erp);

        //设置默认信息
        supplyExtraInfo(list, erp);

        //写入数据库，并记录操作日志
        saveBatch(list);

        //推送到ducc，发生错误重试三次
        publishTry3Times(appProvider);

        return list.stream().map(RateLimitingRules::getId).collect(Collectors.toList());
    }

    private void supplyExtraInfo(List<RateLimitingRules> list, String erp) {
        Date date = new Date();
        for (RateLimitingRules rateLimitingRules : list){
            rateLimitingRules.setInterfacePath(StringUtils.trim(rateLimitingRules.getInterfacePath()));
            rateLimitingRules.setAppProvider(StringUtils.trim(rateLimitingRules.getAppProvider()));
            rateLimitingRules.setAppConsumer(StringUtils.trim(rateLimitingRules.getAppConsumer()));
            rateLimitingRules.setCreateTime(date);
            rateLimitingRules.setUpdateTime(date);
            rateLimitingRules.setErp(erp);
        }
    }

    private void pushToDuccAndPublish(String appProvider) throws Exception {
        LambdaQueryWrapper<RateLimitingRulesConfig> configLambdaQueryWrapper = new LambdaQueryWrapper<>();
        configLambdaQueryWrapper.eq(RateLimitingRulesConfig::getAppProvider, appProvider);
        RateLimitingRulesConfig limitingRulesConfig = configService.getOne(configLambdaQueryWrapper);
        if (limitingRulesConfig==null){
            //没有查到全局限流配置，说明没开启，直接返回
            return;
        }

        TotalRateLimitingConfigDTO totalRateLimitingConfigDto = new TotalRateLimitingConfigDTO();
        totalRateLimitingConfigDto.setAppName(appProvider);
        totalRateLimitingConfigDto.setEnableRateLimit(limitingRulesConfig.getGlobalSwitch()!=null && limitingRulesConfig.getGlobalSwitch()==1);

        //这个地方有个前端是拒绝appid为空的时候为0，为0的时候在ducc中应该是false
        totalRateLimitingConfigDto.setRejectAllEmptyAppIdRequest(limitingRulesConfig.getAppidAllowEmpty()!=null && limitingRulesConfig.getAppidAllowEmpty()==0);


        if (limitingRulesConfig.getGlobalRateLimitingValueSwitch()!=null && limitingRulesConfig.getGlobalRateLimitingValueSwitch()==1){
            totalRateLimitingConfigDto.setTotalRate(limitingRulesConfig.getGlobalRateLimitingValue()!=null && limitingRulesConfig.getGlobalRateLimitingValue()>0 ? limitingRulesConfig.getGlobalRateLimitingValue() : null);
        }else {
            totalRateLimitingConfigDto.setTotalRate(null);
        }


        //查询库中的数据根据生成保存到ducc中，将规则生成两组
        LambdaQueryWrapper<RateLimitingRules> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RateLimitingRules::getAppProvider, appProvider);
        lambdaQueryWrapper.eq(RateLimitingRules::getStatus, 1);
        List<RateLimitingRules> effectiveRuleList= list(lambdaQueryWrapper);
        Map<Integer, List<RateLimitingRules>> ruleGroupMap = effectiveRuleList.stream().collect(Collectors.groupingBy(RateLimitingRules::getRuleType));

        //根据数据库中的规则生成服务端限流规则
        List<String> totalLimitList = new ArrayList<>();
        totalRateLimitingConfigDto.setLimitList(totalLimitList);
        if (ruleGroupMap.containsKey(0)){
            List<RateLimitingRules> rateLimitingRules = ruleGroupMap.get(0);
            for (RateLimitingRules rule : rateLimitingRules){
                if (StringUtils.isNotBlank(rule.getInterfacePath())){
                    String temp = rule.getInterfacePath()+":"+rule.getThreshold();
                    totalLimitList.add(temp);
                }
            }
        }

        //根据数据库中的规则生成客户端限流规则
        List<ClientRateLimitingConfigDTO> clientRateLimitingConfigDtoList = new ArrayList<>();
        if (ruleGroupMap.containsKey(1)){
            Map<String, List<RateLimitingRules>> singleAppConsumerMap = ruleGroupMap.get(1).stream().collect(Collectors.groupingBy(RateLimitingRules::getAppConsumer));
            for (String appConsumer : singleAppConsumerMap.keySet()){
                List<RateLimitingRules> rateLimitingRules = singleAppConsumerMap.get(appConsumer);
                ClientRateLimitingConfigDTO clientRateLimitingConfigDto = new ClientRateLimitingConfigDTO();
                clientRateLimitingConfigDto.setName(CLIENT_PREFIX+appConsumer);
                List<String> limitList = new ArrayList<>();
                for (RateLimitingRules temp : rateLimitingRules){
                    if ("*".equals(temp.getInterfacePath())){
                        if (StringUtils.isNotBlank(temp.getAppConsumer())){
                            String rule = temp.getAppConsumer()+":"+temp.getThreshold();
                            totalLimitList.add(rule);
                        }
                    }else {
                        String rule = temp.getInterfacePath() + ":" + temp.getThreshold();
                        limitList.add(rule);
                    }
                }
                clientRateLimitingConfigDto.setLimitList(limitList);
                clientRateLimitingConfigDtoList.add(clientRateLimitingConfigDto);
            }
        }


        //将总限流规则以及客户端限流写入ducc，写入失败时应该报警
        LambdaQueryWrapper<AppInfo> appInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        appInfoLambdaQueryWrapper.eq(AppInfo::getAppCode, appProvider);
        List<AppInfo> list = appInfoService.list(appInfoLambdaQueryWrapper);
        String configName = appProvider;
        if (list.size()>0){
            AppInfo appInfo = list.get(0);
            configName = appInfo.getAppName();
        }

        DuccApiUtils.createConfigAndProfileIfNotExist(SITE_CODE, NAMESPACE, appProvider, configName, SITE_CODE, SITE_NAME);
        Map<String, String> map = new HashMap<>();
        map.put("limit", JsonUtils.toJSONString(totalRateLimitingConfigDto));
        for (ClientRateLimitingConfigDTO clientRateLimitingConfigDTO : clientRateLimitingConfigDtoList){
            map.put(clientRateLimitingConfigDTO.getName(), JsonUtils.toJSONString(clientRateLimitingConfigDTO));
        }
        DuccResponse duccResponse = duccApiNewUtils.modifyDuccItems(SITE_CODE, NAMESPACE, appProvider, SITE_CODE, map);
        if (duccResponse!=null){
            if (!Objects.equals(200, duccResponse.getCode())){
                throw newBizException("推送ducc报错");
            }
        }
        DuccApiUtils.publishConfig(SITE_CODE, NAMESPACE, appProvider, SITE_CODE);
    }

    private String addRulesCheck(List<RateLimitingRules> list, String erp) {
        //获取用户操作的应用列表
        List<String> cnt = list.stream().map(RateLimitingRules::getAppProvider).distinct().collect(Collectors.toList());
        if (cnt.size()!=1){
            throw new BizException("每次只能操作一个应用");
        }

        //操作的应用列表必须在用户有权限的应用列表里面
        assertUserHasAppPermission(erp, cnt);

        //校验为空和重复的情况
        LambdaQueryWrapper<RateLimitingRules> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RateLimitingRules::getAppProvider, cnt.get(0));
        Set<String> dbRulesSet = rateLimitingRulesMapper.selectList(lambdaQueryWrapper).stream().map(item->item.getAppProvider()+SEPERATOR+item.getAppConsumer()+SEPERATOR+item.getRuleType()+SEPERATOR+item.getInterfacePath()).collect(Collectors.toSet());

        for (RateLimitingRules rateLimitingRules : list){
            if (rateLimitingRules.getRuleType() == null || (rateLimitingRules.getRuleType()!=0 && rateLimitingRules.getRuleType()!=1)){
                throw newBizException("限流规则类型不能为空");
            }
            if (StringUtils.isBlank(rateLimitingRules.getAppProvider())){
                throw new BizException("应用提供方不能为空");
            }
            if (rateLimitingRules.getRuleType()==1){
                if (StringUtils.isBlank(rateLimitingRules.getAppConsumer())){
                    throw new BizException("应用使用方不能为空");
                }
            }
            if (StringUtils.isBlank(rateLimitingRules.getInterfacePath())){
                throw new BizException("接口地址不能为空");
            }
            if (rateLimitingRules.getThreshold()==null){
                throw new BizException("限流阈值不能为空");
            }
            if (rateLimitingRules.getStatus()==null){
                throw new BizException("状态不能为空");
            }
            if (StringUtils.isBlank(rateLimitingRules.getRuleName())){
                throw new BizException("限流规则名称不能为空");
            }
            if (dbRulesSet.contains(rateLimitingRules.getAppProvider()+SEPERATOR+rateLimitingRules.getAppConsumer()+SEPERATOR+rateLimitingRules.getRuleType()+SEPERATOR+rateLimitingRules.getInterfacePath())){
                throw newBizException("不能重复添加限流规则");
            }
            dbRulesSet.add(rateLimitingRules.getAppProvider()+SEPERATOR+rateLimitingRules.getAppConsumer()+SEPERATOR+rateLimitingRules.getRuleType()+SEPERATOR+rateLimitingRules.getInterfacePath());
        }

        return cnt.get(0);
    }

    private void assertUserHasAppPermission(String erp, List<String> cnt) {
        LambdaQueryWrapper<AppInfoMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppInfoMembers::getErp, erp);
        List<AppInfoMembers> list = appInfoMembersService.list(lambdaQueryWrapper);
        List<String> authedAppCodeList = list.stream().map(AppInfoMembers::getAppCode).collect(Collectors.toList());
        for (String appCode : cnt){
            if (!authedAppCodeList.contains(appCode)){
                throw new BizException("没有操作项目" + appCode + "的权限");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @UpdateLog
    /**
     * 此方法有日志切面，修改方法签名时需要同步修改日志切面
     */
    public void updateRules(List<RateLimitingRules> list, String erp) {

        //更新规则检查
        String appProvider = updateRulesCheck(list, erp);

        //批量更新
        for (RateLimitingRules rule : list){
            LambdaUpdateWrapper<RateLimitingRules> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(RateLimitingRules::getRuleName, rule.getRuleName());
            lambdaUpdateWrapper.set(RateLimitingRules::getThreshold, rule.getThreshold());
            lambdaUpdateWrapper.set(RateLimitingRules::getUpdateTime, new Date());
            lambdaUpdateWrapper.eq(RateLimitingRules::getId, rule.getId());
            rateLimitingRulesMapper.update(null, lambdaUpdateWrapper);
        }

        //发布到ducc
        publishTry3Times(appProvider);
    }

    private String updateRulesCheck(List<RateLimitingRules> list, String erp) {

        //校验用户权限
        List<Long> ruleIdList = list.stream().map(RateLimitingRules::getId).collect(Collectors.toList());
        List<String> appCodeList = rateLimitingRulesMapper.selectBatchIds(ruleIdList).stream().map(item->item.getAppProvider()).collect(Collectors.toList());
        if (appCodeList.size()!=1){
            throw newBizException("每次只能操作一个应用");
        }

        assertUserHasAppPermission(erp, appCodeList);

        return appCodeList.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DeleteLog
    /**
     * 此方法有日志切面，修改方法签名时需要同步修改日志切面
     */
    public void deleteRules(List<Long> ids, String erp) {

        //检查是否是下线状态，检查是否有权限删除
        List<RateLimitingRules> rateLimitingRules = listByIds(ids);
        List<String> collect = rateLimitingRules.stream().map(RateLimitingRules::getAppProvider).distinct().collect(Collectors.toList());
        if (collect.size()!=1){
            throw newBizException("每次只能操作一个应用");
        }

        for (String appCode : collect){
            checkUserPermission(appCode, erp);
        }

        //直接执行删除操作即可
        removeByIds(ids);

        //推送到DUCC
        publishTry3Times(collect.get(0));
    }

    /**
     * 更改状态接口，更改后要及时生效
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 此方法有日志切面，修改方法签名时需要同步修改日志切面
     */
    public void changeStatus(RateLimitingChangeStatusDTO statusDTO, String erp) {
        //校验要填写的数据是否都写了
        if (statusDTO.getStatus()==null || statusDTO.getIdList()==null || statusDTO.getIdList().size()==0){
            throw newBizException("规则列表与要设置的状态不能为空");
        }

        List<String> appProviderList = rateLimitingRulesMapper.selectBatchIds(statusDTO.getIdList()).stream().map(RateLimitingRules::getAppProvider).distinct().collect(Collectors.toList());
        if (appProviderList.size()!=1){
            throw newBizException("每次只能操作一个应用的限流规则");
        }
        String appProvider = appProviderList.get(0);
        checkUserPermission(appProvider, erp);

        for (Long id : statusDTO.getIdList()){
            LambdaUpdateWrapper<RateLimitingRules> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(RateLimitingRules::getId, id);
            lambdaUpdateWrapper.set(RateLimitingRules::getStatus, statusDTO.getStatus());
            rateLimitingRulesMapper.update(null, lambdaUpdateWrapper);
        }

        //发送到ducc
        publishTry3Times(appProvider);
    }

    private void publishTry3Times(String appProvider) {
        //推送到ducc
        int retryTimes = 3;
        while (true){
            try {
                pushToDuccAndPublish(appProvider);
                log.info("推送ducc成功");
                break;
            }catch (Exception e){
                if (retryTimes>1){
                    retryTimes--;
                }else {
                    e.printStackTrace();
                    log.error("推送到ducc失败");
                    throw new BizException("发布失败："+e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AddGlobalLog
    public void globalSettings(RateLimitingRulesConfig config, String erp) {
        if (StringUtils.isBlank(config.getAppProvider())){
            throw newBizException("提供方appCode不能为空");
        }
        checkUserPermission(config.getAppProvider(), erp);
        LambdaQueryWrapper<RateLimitingRulesConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RateLimitingRulesConfig::getAppProvider, config.getAppProvider());
        RateLimitingRulesConfig dbConfig = configService.getOne(lambdaQueryWrapper);

        //发布标识
        boolean publishFlag = false;

        if (dbConfig==null){
            //插入新纪录
            config.setAppidAllowEmpty(1);//不让设置此值
            configService.save(config);
            publishFlag = true;
        }else {
            //更新
            LambdaUpdateWrapper<RateLimitingRulesConfig> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(RateLimitingRulesConfig::getId, dbConfig.getId());
            boolean updateFlag = false;
            if (!Objects.equals(config.getGlobalSwitch(), dbConfig.getGlobalSwitch())){
                lambdaUpdateWrapper.set(RateLimitingRulesConfig::getGlobalSwitch, config.getGlobalSwitch());
                updateFlag = true;
            }
            //不让设置此值
            //暂时不让更新
//            if (!Objects.equals(config.getAppidAllowEmpty(), dbConfig.getAppidAllowEmpty())){
//                lambdaUpdateWrapper.set(RateLimitingRulesConfig::getAppidAllowEmpty, config.getAppidAllowEmpty());
//                updateFlag = true;
//            }
            if (!Objects.equals(config.getGlobalRateLimitingValueSwitch(), dbConfig.getGlobalRateLimitingValueSwitch())){
                lambdaUpdateWrapper.set(RateLimitingRulesConfig::getGlobalRateLimitingValueSwitch, config.getGlobalRateLimitingValueSwitch());
                updateFlag = true;
            }
            if(!Objects.equals(config.getGlobalRateLimitingValue(), dbConfig.getGlobalRateLimitingValue())){
                if (config.getGlobalRateLimitingValueSwitch()!=null && config.getGlobalRateLimitingValueSwitch()==1){
                    lambdaUpdateWrapper.set(RateLimitingRulesConfig::getGlobalRateLimitingValue, config.getGlobalRateLimitingValue());
                    updateFlag = true;
                }
            }
            if (updateFlag){
                configService.update(lambdaUpdateWrapper);
                publishFlag = true;
            }
        }

        //发布到ducc
        if (publishFlag){
            try {
                publishTry3Times(config.getAppProvider());
            } catch (Exception e) {
                e.printStackTrace();
                throw newBizException("推送DUCC出错");
            }
        }
    }

    @Override
    public RateLimitingRulesConfig getGlobalSettings(String appProvider, String erp) {
        checkUserPermission(appProvider, erp);
        LambdaQueryWrapper<RateLimitingRulesConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RateLimitingRulesConfig::getAppProvider, appProvider);
        RateLimitingRulesConfig limitingRulesConfig = configService.getOne(lambdaQueryWrapper);
        if (limitingRulesConfig==null){
            RateLimitingRulesConfig config = new RateLimitingRulesConfig();
            config.setAppProvider(appProvider);
            config.setGlobalSwitch(0);
            config.setAppidAllowEmpty(1);
            config.setGlobalRateLimitingValueSwitch(0);
            return config;
        }
        return limitingRulesConfig;
    }

    private BizException newBizException(String msg){
        BizException bizException = new BizException(msg);
        bizException.setFormatPrams(false);
        return bizException;
    }

    private void checkUserPermission(String appProvider, String erp) {
        //获取有权限的应用code列表
        LambdaQueryWrapper<AppInfoMembers> appInfoMembersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        appInfoMembersLambdaQueryWrapper.eq(AppInfoMembers::getErp, erp);
        List<AppInfoMembers> list = appInfoMembersService.list(appInfoMembersLambdaQueryWrapper);
        List<String> authedAppCodeList = list.stream().map(AppInfoMembers::getAppCode).distinct().collect(Collectors.toList());

        //根据appCode过滤出应用规则
        if (!authedAppCodeList.contains(appProvider)){
            throw newBizException("没有操作项目"+appProvider+"的权限");
        }
    }


}

package com.jd.workflow.console.service.manage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.manage.AppDebugErrorConfigMapper;
import com.jd.workflow.console.dao.mapper.manage.AppDebugErrorLogMapper;
import com.jd.workflow.console.dto.doc.AppDebugLogContent;
import com.jd.workflow.console.dto.doc.ErrorReportDto;
import com.jd.workflow.console.dto.manage.AppDebugErrorLogDto;
import com.jd.workflow.console.dto.manage.ErrorLogFilterParam;
import com.jd.workflow.console.dto.manage.FilterRuleConfig;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.env.EnvConfigItem;
import com.jd.workflow.console.entity.manage.AppDebugErrorConfig;
import com.jd.workflow.console.entity.manage.AppDebugErrorLog;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.env.IEnvConfigItemService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AppDebugErrorLogService extends ServiceImpl<AppDebugErrorLogMapper, AppDebugErrorLog> {
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    AppDebugErrorConfigService appDebugErrorConfigService;

    @Autowired
    ScheduledExecutorService         defaultScheduledExecutor;

    @Autowired
    IEnvConfigItemService envConfigItemService;

    Map<Long,FilterRuleConfig> config = new ConcurrentHashMap<>();
    @PostConstruct
    public void init(){
        defaultScheduledExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<Long, FilterRuleConfig> entry : config.entrySet()) {
                   try{
                       FilterRuleConfig config = AppDebugErrorLogService.this.config.remove(entry.getKey());
                       if(config != null){
                           AppDebugErrorConfig errorConfig =  appDebugErrorConfigService.getConfigByAppId(entry.getKey());
                           if(errorConfig != null){
                               errorConfig.setConfig(config);
                               appDebugErrorConfigService.updateById(errorConfig);
                           }else{
                               errorConfig = new AppDebugErrorConfig();
                               errorConfig.setAppId(entry.getKey());
                               errorConfig.setAppCode(appInfoService.findApp(entry.getKey()).getAppCode());
                               appDebugErrorConfigService.save(errorConfig);
                           }
                       }
                   }catch (Exception e){
                       log.error("app.err_update_debug_log:appId={}",entry.getKey(),e);
                   }
                }
            }
        },20,10, TimeUnit.SECONDS);
    }

    public IPage<AppDebugErrorLogDto> pageListErrorLog(ErrorLogFilterParam param){
        Guard.notEmpty(param.getAppId(),"应用id不可为空");
        LambdaQueryWrapper<AppDebugErrorLog> lqw = new LambdaQueryWrapper<>();

        lqw.eq(AppDebugErrorLog::getYn,1);
        List<String> domainAndIps = new ArrayList<>();
        List<EnvConfigItem> envConfigItems = envConfigItemService.listAppConfigItems(param.getAppId());
        for (EnvConfigItem envConfigItem : envConfigItems) {
            try{
                URL urlInfo = new URL(envConfigItem.getUrl());
                domainAndIps.add(urlInfo.getHost());
            }catch (Exception e){
            }
        }
        if(StringUtils.isNotBlank(param.getDomains())){
            domainAndIps.addAll(StringHelper.split(param.getDomains(),","));
        }
        lqw.and(childWrapper->{
           childWrapper.eq(AppDebugErrorLog::getAppId,param.getAppId());
           if(!domainAndIps.isEmpty()){
            childWrapper.or().isNull(AppDebugErrorLog::getAppId)
                    .and(childChildWrapper->{
                   childChildWrapper.in(AppDebugErrorLog::getDomain,domainAndIps)
                           .or().in(AppDebugErrorLog::getIp,domainAndIps);
               });
           }

        });
        lqw.like(StringUtils.isNotBlank(param.getPath()),AppDebugErrorLog::getPath,param.getPath());
        lqw.eq(param.getStatus() != null,AppDebugErrorLog::getStatus,param.getStatus());
        Page<AppDebugErrorLog> page = page(new Page<>(param.getCurrent(), param.getSize()), lqw);

        return page.convert(pageItem->{
            AppDebugErrorLogDto dto = new AppDebugErrorLogDto();
            BeanUtils.copyProperties(pageItem,dto);
            dto.init();
            return dto;
        });
    }

    public void markErrorLogResolved(List<Long> ids){
        Guard.notEmpty(ids,"ids不可为空");
        List<AppDebugErrorLog> logs = listByIds(ids);
        for (AppDebugErrorLog log : logs) {
            log.setStatus(1);
            log.setOp(UserSessionLocal.getUser().getUserId());
            updateById(log);
        }
    }
    public void deleteBatch(List<Long> ids){
        Guard.notEmpty(ids,"ids不可为空");
        List<AppDebugErrorLog> logs = listByIds(ids);
        for (AppDebugErrorLog log : logs) {
            log.setYn(0);
            updateById(log);
        }
    }
    public void updateFilterRuleConfig(FilterRuleConfig rule){
        if(StringUtils.isNotBlank(rule.getAppCode())){
            AppInfo app = appInfoService.findApp(rule.getAppCode());
            if(app!=null){
                config.put(app.getId(),rule);
            }


        }
    }
    public void reportErrorLog(ErrorReportDto dto){
        dto.init();
        AppDebugErrorLog log = new AppDebugErrorLog();
        log.setIp(dto.getIp());
        log.setPath(dto.getPath());
        log.setUrlPrefix(dto.getUrlPrefix());
        log.setDomain(dto.getDomain());
        log.setReporter(dto.getOp());
        log.setCreator(dto.getOp());
        if(StringUtils.isNotBlank(dto.getConfig().getAppCode())){
            log.setAppCode(dto.getConfig().getAppCode());
            AppInfo app = appInfoService.findApp(dto.getConfig().getAppCode());
            if(app!=null){
                log.setAppId(app.getId());
                config.put(app.getId(),dto.getConfig());
            }


        }
        log.setAppCode(dto.getConfig().getAppCode());

        AppDebugLogContent content = new AppDebugLogContent();
        BeanUtils.copyProperties(dto,content);
        content.limitSize();
        log.setStatus(0);
        log.setContent(content);
        log.setYn(1);
        save(log);

    }
}


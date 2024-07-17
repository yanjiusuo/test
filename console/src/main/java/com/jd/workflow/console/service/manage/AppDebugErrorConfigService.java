package com.jd.workflow.console.service.manage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.manage.AppDebugErrorConfigMapper;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.doc.AppDocReportRecord;
import com.jd.workflow.console.entity.manage.AppDebugErrorConfig;
import com.jd.workflow.console.entity.manage.AppDebugErrorLog;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.doc.impl.DocReportServiceImpl;
import com.jd.workflow.console.utils.JfsUtils;
import com.jd.workflow.console.utils.SafeUtil;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Date;
import java.util.List;

@Service
public class AppDebugErrorConfigService extends ServiceImpl<AppDebugErrorConfigMapper, AppDebugErrorConfig> {
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    DocReportServiceImpl docReportService;
    @Autowired
    JfsUtils jfsUtils;
    public Long saveFilterRule(AppDebugErrorConfig config){
        Guard.notEmpty(config.getAppId(),"应用id不可为空");
        AppInfo appInfo = appInfoService.getById(config.getAppId());
        Guard.notEmpty(appInfo,"无效的应用");
        config.setAppCode(appInfo.getAppCode());
        config.setYn(1);
        Guard.notEmpty(config.getConfig(),"配置不可为空");
        Guard.notEmpty(config.getConfig().getErrorExprs(),"无效的错误表达式");

        AppDebugErrorConfig debugErrorConfig = getConfigByAppId(config.getAppId());
        if(debugErrorConfig!=null){
            throw new BizException("重复的错误配置");
        }

        save(config);
        return config.getId();
    }
    public AppDebugErrorConfig getConfigByAppId(Long appId){
        LambdaQueryWrapper<AppDebugErrorConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppDebugErrorConfig::getYn,1);
        lqw.eq(AppDebugErrorConfig::getAppId,appId);
        List<AppDebugErrorConfig> list = list(lqw);
        if(list.isEmpty()) return null;
        return list.get(0);
    }

    public AppDebugErrorConfig getConfigByAppCode(String appCode){
        LambdaQueryWrapper<AppDebugErrorConfig> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppDebugErrorConfig::getYn,1);
        lqw.eq(AppDebugErrorConfig::getAppCode,appCode);
        List<AppDebugErrorConfig> list = list(lqw);
        if(list.isEmpty()) return null;
        return list.get(0);
    }
    public AppDebugErrorConfig updateFilterRule(AppDebugErrorConfig config){
        Guard.notEmpty(config.getId(),"id不可为空");
        updateById(config);
        return getById(config);
    }

    public List<AppDebugErrorConfig> loadUserErrorConfigList(String erp){
        LambdaQueryWrapper<AppDebugErrorConfig> lqw = new LambdaQueryWrapper<>();
        try {
            //使用校验方法校验用户输入的合法性
            if (SafeUtil.sqlValidate(erp)) {
                throw new Exception("您发送请求中的参数中含有非法字符");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lqw.apply("app_id in (select app_id from app_info_members where yn = 1 and erp={0} )",erp);
        lqw.eq(AppDebugErrorConfig::getYn,1);
        return list(lqw);
    }
    public URI getChromePluginDownloadUrl(){
            AppDocReportRecord record = new AppDocReportRecord();
            record.setReportTime(new Date());
            record.setAppCode("__chrome_plugin");
            record.setHttpAppCode(null);
            record.setCreator(UserSessionLocal.getUser().getUserId());
            record.setDigest("1");
            docReportService.save(record);
        return jfsUtils.getChromePluginDownloadUrl("chrome_plugins/");
    }
}

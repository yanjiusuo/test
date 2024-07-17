package com.jd.workflow.console.rpc;

import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.app.JsfAppInfo;
import com.jd.workflow.server.service.AppRpcService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class AppRpcServiceImpl implements AppRpcService {

    @Resource
    private IAppInfoService appInfoService;

    @Override
    public QueryResult<Long> addApp(JsfAppInfo info) {
        log.info("AppRpcService#addApp 入参--{}",info);
        try {
            Long appId = appInfoService.addApp(toAppInfoDto(info));
            return QueryResult.buildSuccessResult(appId);
        } catch (Exception e) {
            log.error("添加app失败", e);
            return QueryResult.error("添加app失败" + e.getMessage());
        }
    }

    @Override
    public QueryResult<Boolean> modifyApp(JsfAppInfo info) {
        log.info("AppRpcService#modifyApp 入参--{}",info);
        try {
            Boolean res= appInfoService.modifyApp(toAppInfoDto(info));
            return QueryResult.buildSuccessResult((res));
        } catch (Exception e) {
            log.error("修改app异常", e);
            return QueryResult.error("修改app异常" + e.getMessage());
        }
    }

    @Override
    public QueryResult<JsfAppInfo> findApp(String appCode) {
        log.info("AppRpcService#findApp 入参--{}",appCode);
        try {
            AppInfo info = appInfoService.findApp(appCode);
            if(info == null){
                return QueryResult.buildSuccessResult(null);
            }
            return QueryResult.buildSuccessResult(toJsfAppInfo(info));
        } catch (Exception e) {
            log.error("查找app异常", e);
            return QueryResult.error("查找app失败" + e.getMessage());
        }
    }

    private JsfAppInfo toJsfAppInfo(AppInfo info) {
        JsfAppInfo dto = new JsfAppInfo();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }


    public AppInfoDTO toAppInfoDto(JsfAppInfo info) {
        String appCode = info.getAppCode();
        Guard.notEmpty(appCode, "应用编码不可为空");
        AppInfoDTO dto = new AppInfoDTO();
        BeanUtils.copyProperties(info, dto);
        return dto;
    }


}

package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.manage.AppDebugErrorLogDto;
import com.jd.workflow.console.dto.manage.ErrorLogFilterParam;
import com.jd.workflow.console.dto.manage.FilterRuleConfig;
import com.jd.workflow.console.entity.manage.AppDebugErrorConfig;
import com.jd.workflow.console.entity.manage.AppDebugErrorLog;
import com.jd.workflow.console.service.manage.AppDebugErrorConfigService;
import com.jd.workflow.console.service.manage.AppDebugErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * @menu 应用错误管理
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/app")
@UmpMonitor
public class AppDebugErrorManageController {
    @Autowired
    AppDebugErrorConfigService appDebugErrorConfigService;

    @Autowired
    AppDebugErrorLogService appDebugErrorLogService;
    /**
     * 保存错误过滤规则
     * @param config
     * @return 保存后的id
     */
    @RequestMapping("saveAppErrorFilterRule")
    public CommonResult<Long> saveAppErrorFilterRule(@RequestBody AppDebugErrorConfig config){
        return CommonResult.buildSuccessResult(appDebugErrorConfigService.saveFilterRule(config));
    }

    /**
     * 修改应用错误过滤规则
     * @param config
     * @return
     */
    @RequestMapping("updateAppErrorFilterRule")
    public CommonResult<AppDebugErrorConfig> updateAppErrorFilterRule(@RequestBody AppDebugErrorConfig config){
        return CommonResult.buildSuccessResult(appDebugErrorConfigService.updateFilterRule(config));
    }
    /**
     * 获取应用过滤规则
     * @param appId 应用id
     * @return
     */
    @RequestMapping("getAppDebugErrorConfig")
    public CommonResult<AppDebugErrorConfig> getAppDebugErrorConfig(Long appId){
        return CommonResult.buildSuccessResult(appDebugErrorConfigService.getConfigByAppId(appId));
    }
    /**
     * 分页获取应用错误过滤规则
     * @param
     * @return
     */
    @RequestMapping("pageListErrorLog")
    public CommonResult<IPage<AppDebugErrorLogDto>> pageListErrorLog(ErrorLogFilterParam param){
        return CommonResult.buildSuccessResult(appDebugErrorLogService.pageListErrorLog(param));
    }

    /**
     * 根据id获取错误日志
     * @param id
     * @return
     */
    @RequestMapping("getAppErrorLogById")
    public CommonResult<AppDebugErrorLogDto> getAppErrorLogById(Long id){
        AppDebugErrorLog log = appDebugErrorLogService.getById(id);
        AppDebugErrorLogDto dto = new AppDebugErrorLogDto();
        BeanUtils.copyProperties(log,dto);
        dto.init();
        return CommonResult.buildSuccessResult(dto);
    }

    /**
     * 批量标记错误日志为已解决
     * @param ids 要标记的错误日志列表
     * @return
     */
    @RequestMapping("markErrorLogResolved")
    public CommonResult<Boolean> markErrorLogResolved(@RequestBody List<Long> ids){
        appDebugErrorLogService.markErrorLogResolved(ids);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 批量删除日志记录
     * @param ids 要标记的错误日志列表
     * @return
     */
    @RequestMapping("removeBatch")
    public CommonResult<Boolean> removeBatch(@RequestBody List<Long> ids){
        appDebugErrorLogService.deleteBatch(ids);
        return CommonResult.buildSuccessResult(true);
    }

    @RequestMapping("getChromePluginDownloadUrl")
    public CommonResult<String> getChromePluginDownloadList(){
        URI uri = appDebugErrorConfigService.getChromePluginDownloadUrl();
        return CommonResult.buildSuccessResult(uri.toString());
    }

}

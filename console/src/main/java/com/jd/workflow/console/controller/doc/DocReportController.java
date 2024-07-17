package com.jd.workflow.console.controller.doc;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.app.AppAllInfo;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.dto.doc.EnumsReportDTO;
import com.jd.workflow.console.dto.doc.ErrorReportDto;
import com.jd.workflow.console.dto.doc.JavaBeanReportDto;
import com.jd.workflow.console.dto.manage.FilterRuleConfig;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.manage.AppDebugErrorConfig;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.service.manage.AppDebugErrorConfigService;
import com.jd.workflow.console.service.manage.AppDebugErrorLogService;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 用来上报文档
 */
@Slf4j
@RestController
@RequestMapping("/doc")
@UmpMonitor
@Api(tags="文档上报")
public class DocReportController {
    @Autowired
    private IDocReportService reportService;
    @Autowired
    AppDebugErrorLogService appDebugErrorLogService;
    @Autowired
    AppDebugErrorConfigService appDebugErrorConfigService;

    /**
     * 上报接口文档
     * @param dto
     * @return com.jd.workflow.console.base.CommonResult<java.lang.Boolean>
     */
    @PostMapping(value = "/reportDoc")
    @ResponseBody
    public CommonResult<Long> reportDoc(@RequestBody DocReportDto dto) {
        Long spaceId= reportService.reportDoc(dto);
        return CommonResult.buildSuccessResult(spaceId);
    }

    /**
     * 上报接口文档
     * @param dto
     * @return com.jd.workflow.console.base.CommonResult<java.lang.Boolean>
     */
    @PostMapping(value = "/reportDocFromJsfPlatform")
    @ResponseBody
    public CommonResult<Long> reportDocFromJsfPlatform(@RequestBody DocReportDto dto) {
        Long spaceId= reportService.reportDocFromJsfPlatform(dto);
        return CommonResult.buildSuccessResult(spaceId);
    }


    @GetMapping("loadAppInfo")
    public CommonResult<AppAllInfo> loadAppInfo(String appCode,String appSecret,String ip){
        return CommonResult.buildSuccessResult(reportService.loadAppInfo(appCode,appSecret,ip));
    }

    @PostMapping(value = "/reportJavaBean")
    @ResponseBody
    public CommonResult<Boolean> reportDoc(@RequestBody JavaBeanReportDto dto) {
        reportService.reportJavaBean(dto);
        return CommonResult.buildSuccessResult(true);
    }
    @PostMapping(value = "/reportEnums")
    @ResponseBody
    public CommonResult<Boolean> reportEnums(@RequestBody EnumsReportDTO dto) {
        reportService.reportEnums(dto);
        return CommonResult.buildSuccessResult(true);
    }

    @PostMapping(value="reportErrorData")
    public CommonResult<Boolean> reportErrorData(@RequestBody ErrorReportDto dto ){
        appDebugErrorLogService.reportErrorLog(dto);
        return  CommonResult.buildSuccessResult(true);
    }

    @PostMapping(value="updateAppFilterRule")
    public CommonResult<Boolean> updateAppFilterRule(@RequestBody FilterRuleConfig config ){
        appDebugErrorLogService.updateFilterRuleConfig(config);
        return  CommonResult.buildSuccessResult(true);
    }

    @RequestMapping("getAppErrorFileConfig")
    public CommonResult<AppDebugErrorConfig> loadAppConfig(String appCode){
        Guard.notEmpty(appCode,"无效的app编码");
        AppDebugErrorConfig errorConfig = appDebugErrorConfigService.getConfigByAppCode(appCode);
        return CommonResult.buildSuccessResult(errorConfig);
    }
    @RequestMapping("loadUserErrorConfigList")
    public CommonResult<List<AppDebugErrorConfig>> loadUserErrorConfigList(String erp){
        Guard.notEmpty(erp,"无效的erp");
         return CommonResult.buildSuccessResult(appDebugErrorConfigService.loadUserErrorConfigList(erp));
    }

}

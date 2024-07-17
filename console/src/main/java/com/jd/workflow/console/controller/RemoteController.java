package com.jd.workflow.console.controller;

import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.up.standardserve.api.client.StandardInfoResponse;
import com.jd.up.standardserve.api.client.service.StandServeAppInfoService;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import io.swagger.annotations.ApiOperation;

@Slf4j
@Validated
@RestController
@RequestMapping("/remote")
//@Profile("erpLogin,selfLogin")
@UmpMonitor
@Api(value="控制相关",tags="控制相关")
public class RemoteController {

    @Value("${interceptor.loginType}")
    Integer loginType;

   @Resource
   private StandServeAppInfoService standServeAppInfoService;

    @GetMapping("/allowSetDemo")
    @ApiOperation(value = "是否允许设置demo")
    public CommonResult<Boolean> allowSetDemo() {
        //查询角色信息
        Boolean rest= false;
        try {
            StandardInfoResponse<Boolean> apiResponse = standServeAppInfoService.isCheckAdmin(UpLoginContextHelper.getUserPin(),UpLoginContextHelper.getTenantId());
            rest = apiResponse.getData();
        } catch (Exception e) {
            log.error("查询角色权限失败",e);
            rest = false;
        }
        return CommonResult.buildSuccessResult(rest);
    }

    /**
     * 是否独立部署版本,独立部署版本很多功能不需要
     *
     * @return
     */
    @GetMapping("/isDependent")
    @ApiOperation(value = "是否独立部署判断")
    public CommonResult<Boolean> isDependent() {
        boolean isDependent = LoginTypeEnum.SELF.getCode().equals(loginType);
        return CommonResult.buildSuccessResult(isDependent);
    }
}

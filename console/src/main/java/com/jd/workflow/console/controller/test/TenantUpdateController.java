package com.jd.workflow.console.controller.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jd.cjg.bus.BusInterfaceRpcService;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.config.dao.MetaContextHelper;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.UpdateTenantDto;
import com.jd.workflow.console.dto.importer.ImportDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.importer.JapiDataSyncService;
import com.jd.workflow.console.service.doc.importer.JapiHttpDataImporter;
import com.jd.workflow.console.service.doc.importer.JddjApiImporter;
import com.jd.workflow.console.service.doc.importer.UpstandardSyncService;
import com.jd.workflow.console.service.doc.importer.dto.JApiProjectInfo;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.impl.InterfaceManageServiceImpl;
import com.jd.workflow.console.service.impl.UserInfoServiceImpl;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.service.sync.AppType;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 每次上线后需要执行一些更新的sql语句，放到这里面执行
 */
@Slf4j
@RestController
@UmpMonitor
@RequestMapping("/tenantUpdate")
@Api(hidden = true,value = "更新租户信息")
public class TenantUpdateController {

    @Autowired
    InterfaceManageServiceImpl interfaceManageService;
    @Autowired
    UserInfoServiceImpl userInfoService;

    @Autowired
    AppInfoServiceImpl appInfoService;

    @Autowired
    private IMeasureDataService measureDataService;

    @PostMapping("/updateInterfaceDept")
    @ApiOperation(value="更新部门信息",hidden = true)
    public CommonResult updateTenantId(){
         userInfoService.updateAllUserDept();
         interfaceManageService.updateInterfaceDept();
         // 刷新measure_data表部门数据
         measureDataService.refreshMeasureDataDept();
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }
    @PostMapping("/updateAppDepts")
    @ApiOperation(value="更新应用部门信息",hidden = true)
    public CommonResult updateAppDepts(){
        appInfoService.updateAppDepts();

        //4.出参
        return CommonResult.buildSuccessResult(1);
    }


    @RequestMapping("/updateInterfaceTenant")
    @ApiOperation(value="更新租户信息",hidden = true)
    public CommonResult updateInterfaceTenant(String deptName,String tenantId ){
        interfaceManageService.updateInterfaceTenant(deptName, tenantId);

        //4.出参
        return CommonResult.buildSuccessResult(1);
    }

    @RequestMapping("/updateAppTenant")
    @ApiOperation(value="更新租户信息",hidden = true)
    public CommonResult updateAppTenant(String deptName,String tenantId ){
        interfaceManageService.updateAppTenantId(deptName, tenantId);

        //4.出参
        return CommonResult.buildSuccessResult(1);
    }
}

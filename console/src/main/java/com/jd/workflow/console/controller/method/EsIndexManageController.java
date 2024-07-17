package com.jd.workflow.console.controller.method;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.manage.HealthCheckResultDto;
import com.jd.workflow.console.elastic.entity.InterfaceManageDoc;
import com.jd.workflow.console.elastic.entity.MethodManageDoc;
import com.jd.workflow.console.elastic.repository.InterfaceManageRepository;
import com.jd.workflow.console.elastic.service.EsInterfaceService;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.impl.ScoreManageService;
import com.jd.workflow.console.service.manage.RankScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/esIndex")
@UmpMonitor
public class EsIndexManageController {
    @Autowired
    EsInterfaceService esIndexService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    InterfaceManageRepository interfaceManageRepository;

    @Autowired
    IAppInfoService appInfoService;

    @GetMapping(value = "/saveUpdateInterface")
    public CommonResult<Boolean> saveUpdateInterface() {
        InterfaceManageDoc interfaceManageDoc = new InterfaceManageDoc();
        interfaceManageDoc.setId("12842");
        interfaceManageDoc.setAppName("test");
        interfaceManageDoc.setCloudFileTags("haohao");
        interfaceManageRepository.save(interfaceManageDoc);interfaceManageDoc.setAppId(12777L);
//        interfaceManageRepository.deleteById("0");
        return CommonResult.buildSuccessResult(true);
    }


    @GetMapping(value = "/removeInterfaceDoc")
    public CommonResult<Boolean> healthCheck(Long interfaceId) {
        esIndexService.removeInterfaceDoc(Collections.singletonList(interfaceId));
        esIndexService.removeInterfaceMethodDoc(Collections.singletonList(interfaceId));
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping(value = "/rebuildInterfaceMethodIndex")
    public CommonResult<Boolean> rebuildInterfaceMethodIndex(Long interfaceId) {
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        esIndexService.rebuildInterfaceMethodIndex(interfaceManage);

        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping(value = "/saveAppDoc")
    public CommonResult<Boolean> saveAppDoc(Long appId) {
        AppInfo appInfo = appInfoService.getById(appId);
        List<InterfaceManage> interfaceManages = interfaceManageService.getAppInterfaces(appId);

        esIndexService.saveInterfaceDoc(interfaceManages,appInfo);
        for (InterfaceManage interfaceManage : interfaceManages) {
            esIndexService.rebuildInterfaceMethodIndex(interfaceManage);
        }
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping(value = "/rebuildAllIndex")
    public CommonResult<Boolean> rebuildAllIndex() {
        esIndexService.initAllAppIndex();
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping(value = "/removeAppDoc")
    public CommonResult<Boolean> removeAppDoc(Long appId) {

        esIndexService.removeAppDoc(appId);

        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping(value = "/listAllInterfaces")
    public CommonResult<List<InterfaceManageDoc>> listAllInterfaces() {



        return CommonResult.buildSuccessResult(esIndexService.listAllInterfaces());
    }
    @GetMapping(value = "/listAllMethods")
    public CommonResult<List<MethodManageDoc>> listAllMethods() {



        return CommonResult.buildSuccessResult(esIndexService.listAllMethods());
    }
}

package com.jd.workflow.console.controller.debug;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.debug.JsfMavenInfo;
import com.jd.workflow.console.dto.debug.MavenLoadStatus;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.debug.JsfJarRecord;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.debug.JsfJarRecordService;
import com.jd.workflow.console.service.debug.impl.DefaultJsfCallService;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.StringHelper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * jsf jar包调试服务
 */
@RestController
@Slf4j
@RequestMapping("/jsfJarDebug")
public class JsfJarDebugController {
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    JsfJarRecordService jsfJarRecordService;
    @Autowired
    DefaultJsfCallService defaultJsfCallService;
    @RequestMapping("/listDefaultMavenInfo")
    public CommonResult<JsfMavenInfo> listDefaultMavenInfo(Long interfaceId){
        InterfaceManage manage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(manage,"接口不存在");
        if(!InterfaceTypeEnum.JSF.getCode().equals(manage.getType())){
            throw new BizException("必须为jsf接口");
        }
        if(StringUtils.isBlank(manage.getPath())){
            return CommonResult.buildSuccessResult(new JsfMavenInfo());
        }
        List<String> strs = StringHelper.split(manage.getPath(), ":");

        JsfMavenInfo info = new JsfMavenInfo();
        info.setGroupId(strs.get(0));
        info.setArtifactId(strs.get(1));
        List<JsfJarRecord> records = jsfJarRecordService.getNonFailRecords(strs.get(0), strs.get(1));
        Set<String> versions = records.stream().map(item -> item.getVersion()).collect(Collectors.toSet());
        versions.add(strs.get(2));
        info.setVersions(versions.stream().collect(Collectors.toList()));
        return CommonResult.buildSuccessResult(info);
    }
    private void validateLocation(MavenJarLocation location){
        Guard.notNull(location,"location不能为空");
        Guard.notEmpty(location.getGroupId(),"groupId不能为空");
        Guard.notEmpty(location.getArtifactId(),"artifactId不能为空");
        Guard.notEmpty(location.getVersion(),"version不能为空");
    }
    @RequestMapping("mavenIsLoaded")
    public CommonResult<MavenLoadStatus>  mavenIsLoaded(@RequestBody  MavenJarLocation location){
        validateLocation(location);

        return CommonResult.buildSuccessResult(defaultJsfCallService.mavenIsLoaded(location));
    }
    @RequestMapping("downloadMavenJarToLocal")
    public CommonResult<Boolean>  downloadMavenJar(@RequestBody  MavenJarLocation location){
        validateLocation(location);
        defaultJsfCallService.downloadMavenJar(location);
        return CommonResult.buildSuccessResult(true);
    }
    @RequestMapping("removeById")
    public CommonResult<Boolean>  removeByIdInfo(  Long id){
        boolean result = jsfJarRecordService.removeById(id);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * maven是否可以重新加载
     * @param location maven坐标
     * @return maven是佛可以重新加载
     */
    @RequestMapping("mavenCanReload")
    public CommonResult<Boolean>  mavenCanReload(@RequestBody  MavenJarLocation location){
        validateLocation(location);

        return CommonResult.buildSuccessResult(defaultJsfCallService.mavenCanReload(location));
    }
    @RequestMapping("parseJsfJar")
    public CommonResult<Boolean> parseJsfJar(@RequestBody  MavenJarLocation location){
        validateLocation(location);
        return CommonResult.buildSuccessResult(defaultJsfCallService.parseJsfJar(location));
    }
    @RequestMapping("reParseJsfJar")
    public CommonResult<Boolean> reParseJsfJar(@RequestBody  MavenJarLocation location){
        validateLocation(location);
        return CommonResult.buildSuccessResult(defaultJsfCallService.reParseJsfJar(location));
    }

    @RequestMapping("removeJsfJarRecord")
    public CommonResult<Boolean> removeJsfJarRecord(@RequestBody  MavenJarLocation location){
        validateLocation(location);
        return CommonResult.buildSuccessResult(defaultJsfCallService.removeRecord(location));
    }
    @RequestMapping("moveJarDoc")
    public CommonResult<Boolean> moveJarDoc(String srcIp){
        defaultJsfCallService.moveJarDoc(srcIp);
        return CommonResult.buildSuccessResult(true);
    }
}

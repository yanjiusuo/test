package com.jd.workflow.console.controller;

import com.jd.workflow.codegen.FileCode;
import com.jd.workflow.codegen.SingleClassGenerator;
import com.jd.workflow.codegen.dto.GeneratedCodeDto;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.RefJsonTypeService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/code")
@UmpMonitor
@Api(value = "代码管理",tags="代码管理")
public class CodeManageController {
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    RefJsonTypeService refJsonTypeService;

    /**
     * 生成方法代码
     * @param methodId 方法id
     * @param type 方法类型
     * @return 生成后的代码
     */
    @RequestMapping("/generateMethodCode")
    public CommonResult<String> generateCode(Long methodId, String type){
        CommonResult<List<FileCode>> result = generatePartMethodCode(methodId, type);
        StringBuilder sb = new StringBuilder();
        for (FileCode fileCode : result.getData()) {
            sb.append(fileCode.getCode()+"\n");
        }
        return CommonResult.buildSuccessResult(sb.toString());
    }
    @RequestMapping("/generatePartMethodCode")
    public CommonResult<List<FileCode>> generatePartMethodCode(Long methodId, String type){
        Guard.notEmpty(methodId,"methodId不可为空");
        MethodManage methodMange = methodManageService.getById(methodId);
        if(!InterfaceTypeEnum.HTTP.getCode().equals(methodMange.getType())&&!InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(methodMange.getType())){
            throw new BizException("只有http接口才能生成代码");
        }
        Guard.notNull(methodMange,"无效的methodId");
        InterfaceManage interfaceManage = interfaceManageService.getById(methodMange.getInterfaceId());
        methodManageService.initContentObject(methodMange);
        refJsonTypeService.initMethodRefInfos(methodMange,interfaceManage.getAppId());
        methodManageService.initMethodDeltaInfos(Collections.singletonList(methodMange));
        SingleClassGenerator codeGenerator = new SingleClassGenerator();
        List<FileCode> codes = codeGenerator.generateSingleMethodCode((HttpMethodModel) methodMange.getContentObject(), type);

        return CommonResult.buildSuccessResult(codes);
    }
}

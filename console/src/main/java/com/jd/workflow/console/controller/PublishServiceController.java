package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.dto.PublishManageDTO;
import com.jd.workflow.console.dto.WorkFlowPublishReqDTO;
import com.jd.workflow.console.service.IPublishManageService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import io.swagger.annotations.ApiOperation;

/**
 * 项目名称：example
 * 类 名 称：PublishServiceController
 * 类 描 述：发布controller
 * 创建时间：2022-06-01 09:47
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/publishService")
@UmpMonitor
@Api(value = "发布管理",tags="发布管理")
public class PublishServiceController {

    @Resource
    private IPublishManageService publishManageService;

    @GetMapping("/publishConvertWebService")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "转换后webservice方法发布")
    public CommonResult<Boolean> publishConvertWebService(Long methodId, Long interfaceId,Long clusterId) {
        log.info("PublishServiceController publishConvertWebService methodId={} , interfaceId={} , clusterId={}", methodId, interfaceId, clusterId);
        Guard.notNull(methodId, "发布http转webservice接口服务时methodId不能为空");
        Guard.notNull(interfaceId, "发布http转webservice接口服务时interfaceId不能为空");
        Guard.notNull(clusterId, "发布http转webservice接口服务时clusterId不能为空");
        //service层
        Boolean ref = publishManageService.publishConvertWebService(methodId, interfaceId, clusterId);
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/republishService")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "发布回滚或老版本发布")
    public CommonResult<Boolean> republishService(Long id, Long methodId, Long interfaceId) {
        log.info("PublishServiceController republishService id={} ,  methodId={} , interfaceId={}", id, methodId, interfaceId);
        Guard.notNull(methodId, "重新发布服务时id不能为空");
        Guard.notNull(methodId, "重新发布服务时methodId不能为空");
        Guard.notNull(interfaceId, "重新发布服务时interfaceId不能为空");
        //service层
        Boolean ref = publishManageService.republishService(id, methodId, interfaceId);
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/publishVersionDetail")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "发布版本查看详情")
    public CommonResult<PublishManageDTO> findPublishVersionDetail(Long id, Long methodId, Long interfaceId) {
        log.info("PublishServiceController findPublishVersionDetail id={} ,  methodId={} , interfaceId={}", id, methodId, interfaceId);
        Guard.notNull(methodId, "查看发布版本详情时id不能为空");
        Guard.notNull(methodId, "查看发布版本详情时methodId不能为空");
        Guard.notNull(interfaceId, "查看发布版本详情时interfaceId不能为空");
        //service层
        PublishManageDTO ref = publishManageService.findPublishVersionDetail(id, methodId, interfaceId);
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/publishVersonList")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "发布列表查询")
    public CommonResult<List<PublishManageDTO>> findPublicVersionList(Long methodId, Long interfaceId) {
        log.info("PublishServiceController findPublicVersionList methodId={} , interfaceId={}", methodId, interfaceId);
        Guard.notNull(methodId, "查看发布版本列表时methodId不能为空");
        Guard.notNull(interfaceId, "查看发布版本列表时interfaceId不能为空");
        //service层
        List<PublishManageDTO> ref = publishManageService.findPublicVersionList(methodId, interfaceId);
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/publishWorkFlowService")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "流程编排服务发布")
    public CommonResult<Boolean> publishWorkFlowService(@RequestBody WorkFlowPublishReqDTO workFlowPublishReqDTO) {
        log.info("PublishServiceController publishWorkFlowService workFlowPublishReqDTO={} ", JsonUtils.toJSONString(workFlowPublishReqDTO));
        Guard.notNull(workFlowPublishReqDTO.getMethodId(), "发布服务编排时methodId不能为空");
        Guard.notNull(workFlowPublishReqDTO.getInterfaceId(), "发布服务编排时interfaceId不能为空");
        Guard.notNull(workFlowPublishReqDTO.getClusterId(), "发布服务编排时clusterId不能为空");
        //service层
        Boolean ref = publishManageService.publishWorkflow(workFlowPublishReqDTO);
        return CommonResult.buildSuccessResult(ref);
    }
}

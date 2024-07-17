package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.version.CompareMethodVersionDTO;
import com.jd.workflow.console.dto.version.CompareVersionDTO;
import com.jd.workflow.console.dto.version.InterfaceInfo;
import com.jd.workflow.console.dto.version.InterfaceInfoReq;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.impl.MethodModifyLogServiceImpl;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import io.swagger.annotations.ApiOperation;

/**
 * 项目名称：parent
 * 类 名 称：InterfaceVersionController
 * 类 描 述：接口版本管理
 * 创建时间：2022-11-18 16:44
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@RestController
@RequestMapping("/interfaceVersion")
@UmpMonitor
@Api(value = "方法版本管理",tags="方法版本管理")
public class InterfaceVersionController {

    @Resource
    private IInterfaceVersionService interfaceVersionService;
    @Autowired
    MethodModifyLogServiceImpl methodModifyLogService;

    /**
     * 接口版本基本信息查询
     *
     * @param req
     * @return
     */
    @PostMapping("/viewBaseInfo")
    @ApiOperation(value = "版本接口基本信息查询")
    public //@Authorization(key="interfaceId",parseType = ParseType.BODY)
    CommonResult<InterfaceInfo> viewInterfaceBaseInfo(@RequestBody InterfaceInfoReq req) {
        log.info("InterfaceVersionController viewInterfaceBaseInfo req={} ", JSON.toJSONString(req));
        Guard.notNull(req, "接口版本基本信息查询入参不能为空");
        Guard.notNull(req.getInterfaceId(), "接口版本基本信息查询接口ID入参不能为空");
        InterfaceInfo interfaceBaseInfo = interfaceVersionService.findInterfaceBaseInfo(req);
        return CommonResult.buildSuccessResult(interfaceBaseInfo);
    }

    /**
     * 接口方法树查询、指定版本查询
     *
     * @param req
     * @return
     */
    @PostMapping("/viewGroupTree")
    @ApiOperation(value = "版本方法分组树查询")
    public CommonResult<MethodGroupTreeDTO> viewInterfaceGroupTree(@RequestBody InterfaceInfoReq req) {
        log.info("InterfaceVersionController viewInterfaceGroupTree req={} ", JSON.toJSONString(req));
        Guard.notNull(req, "接口版本方法树信息查询入参不能为空");
        Guard.notNull(req.getInterfaceId(), "接口版本方法树信息查询接口ID入参不能为空");
        MethodGroupTreeDTO result = interfaceVersionService.findMethodGroupTree(req);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 版本列表查询
     *
     * @param req
     * @return
     */
    @PostMapping("/viewVersionList")
    @ApiOperation(value = "接口版本下拉列表")
    public CommonResult<List<InterfaceVersion>> viewVersionList(@RequestBody InterfaceInfoReq req) {
        log.info("InterfaceVersionController viewVersionList req={} ", JSON.toJSONString(req));
        Guard.notNull(req, "接口版本列表查询入参不能为空");
        Guard.notNull(req.getInterfaceId(), "接口版本列表查询接口ID入参不能为空");
        List<InterfaceVersion> result = interfaceVersionService.findInterfaceVersion(req);
        return CommonResult.buildSuccessResult(result);
    }
    /**
     * 保存版本
     * @param interfaceVersion
     * @return
     */
    @PostMapping("/saveInterfaceVersion")
    @ApiOperation(value = "保存版本")
    public CommonResult<Boolean> saveInterfaceVersion(@RequestBody InterfaceVersion interfaceVersion) {
        log.info("InterfaceVersionController viewVersionList req={} ", JSON.toJSONString(interfaceVersion));
        Guard.notNull(interfaceVersion, "接口版本入参不能为空");
        Guard.notNull(interfaceVersion.getId(), "接口版本列ID入参不能为空");
        interfaceVersionService.updateInterfaceVersion(interfaceVersion);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 版本方法树比对
     *
     * @param req
     * @return
     */
    @PostMapping("/versionGroupTreeCompare")
    @ApiOperation(value = "版本方法树比对")
    public CommonResult<CompareVersionDTO> versionGroupTreeCompare(@RequestBody InterfaceInfoReq req) {
        log.info("InterfaceVersionController versionGroupTreeCompare req={} ", JSON.toJSONString(req));
        Guard.notNull(req, "接口版本树比对入参不能为空");
        Guard.notNull(req.getInterfaceId(), "接口版本树比对接口ID入参不能为空");
        Guard.notNull(req.getVersion(), "接口版本树比对基础版本version入参不能为空");
        CompareVersionDTO result = interfaceVersionService.compareInterfaceVersion(req);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 版本方法比对 或 demon比对
     *
     * @param req
     * @return
     */
    @PostMapping("/versionMethodCompare")
    @ApiOperation(value = "版本方法内容比对")
    public CommonResult<CompareMethodVersionDTO> versionMethodCompare(@RequestBody InterfaceInfoReq req) {
        log.info("InterfaceVersionController versionMethodCompare req={} ", JSON.toJSONString(req));
        Guard.notNull(req, "接口版本方法比对入参不能为空");
        Guard.notNull(req.getInterfaceId(), "接口版本方法比对接口ID入参不能为空");
        Guard.notNull(req.getVersion(), "接口版本方法比对基础版本version入参不能为空");
        Guard.notNull(req.getMethodId(), "接口版本方法比对methodId入参不能为空");
        CompareMethodVersionDTO result = interfaceVersionService.compareMethodVersion(req);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 预览迭代版本
     *
     * @param req
     * @return
     */
    @PostMapping("/viewNextVersion")
    @ApiOperation(value = "新建迭代版本预览")
    public CommonResult<String> viewNextInterfaceVersion(@RequestBody InterfaceInfoReq req) {
        log.info("InterfaceVersionController viewNextInterfaceVersion req={} ", JSON.toJSONString(req));
        Guard.notNull(req, "预览迭代版本入参不能为空");
        Guard.notNull(req.getInterfaceId(), "预览迭代版本接口ID入参不能为空");
        Guard.notNull(req.getAddVersionType(), "预览迭代版本大、小迭代入参不能为空");
        if (req.getAddVersionType().intValue() != 1 && req.getAddVersionType().intValue() != 0) {
            throw new BizException("大小版本类型参数不合法");
        }
        String result = interfaceVersionService.viewNextInterfaceVersion(req);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 新建迭代版本
     *
     * @param req
     * @return
     */
    @PostMapping("/createInterfaceVersion")
    @ApiOperation(value = "新建版本迭代保存")
    public CommonResult<String> createInterfaceVersion(@RequestBody InterfaceInfoReq req) {
        log.info("InterfaceVersionController createInterfaceVersion req={} ", JSON.toJSONString(req));
        Guard.notNull(req, "新建迭代版本入参不能为空");
        Guard.notNull(req.getInterfaceId(), "新建迭代版本接口ID入参不能为空");
        Guard.notNull(req.getAddVersionDesc(), "新建迭代版本描述不能为空");
        Guard.notNull(req.getAddVersionType(), "新建迭代版本大、小迭代入参不能为空");
        if (req.getAddVersionType().intValue() != 1 && req.getAddVersionType().intValue() != 0) {
            throw new BizException("大小版本类型参数不合法");
        }
        String result = interfaceVersionService.createInterfaceVersion(req);
        return CommonResult.buildSuccessResult(result);
    }

    @PostMapping("/recoveryMethod")
    public CommonResult<Boolean> recoveryMethod(@RequestBody List<Long> modifyLogIds){
        for (Long modifyLogId : modifyLogIds) {
            methodModifyLogService.recoveryMethod(modifyLogId);
        }
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 获取指定版本的方法
     * @param version 版本号
     * @param id 方法id
     * @return
     */
    @GetMapping("/getVersionMethod")
    public CommonResult<MethodManageDTO> getVersionMethod(String version, Long id){
        return CommonResult.buildSuccessResult(interfaceVersionService.getVersionMethod(version, id));
    }
}

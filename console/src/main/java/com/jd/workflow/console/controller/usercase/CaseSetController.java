package com.jd.workflow.console.controller.usercase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.usecase.CaseSetDTO;
import com.jd.workflow.console.dto.usecase.RequiremenUnderInterfacesDTO;
import com.jd.workflow.console.dto.usecase.TreeItem;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.usecase.CaseSet;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.console.service.usecase.CaseSetService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 用例集表 前端控制器
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@RestController
@RequestMapping("/caseSet")
public class CaseSetController {

    @Resource
    private CaseSetService caseSetService;
    @Resource
    private IAppInfoService appInfoService;
    @Resource
    private RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Resource
    private RequirementInfoService requirementInfoService;
    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;

    /**
     * 获取用例集列表
     */
    @GetMapping("/list")
    public CommonResult<IPage<CaseSetDTO>> pageList(Long current, Long pageSize, String name, Long requirementId) {

        if (!caseSetService.checkAuth(requirementId, UserSessionLocal.getUser().getUserId())) {
            throw new BizException("无权查看");
        }

        Guard.notNull(requirementId, "需求空间不能为null");
        IPage<CaseSetDTO> ret = caseSetService.pageList(current, pageSize, name, requirementId);

        return CommonResult.buildSuccessResult(ret);
    }

    /**
     * 根据需求空间ID对应的应用列表
     */
    @GetMapping("/requiremenUnderAppList")
    public CommonResult<List<AppInfo>> requiremenUnderAppList(Long requirementId) {

        Guard.notNull(requirementId, "需求空间不能为null");
        List<AppInfo> ret = appInfoService.getAppByRequirementId(requirementId);

        return CommonResult.buildSuccessResult(ret);
    }

    /**
     * 根据需求空间ID、appId 获取对应的接口列表
     */
    @GetMapping("/requiremenUnderInterfaceList")
    public CommonResult<RequiremenUnderInterfacesDTO> requiremenUnderAppList(Long requirementId, Long appId) {

        Guard.notNull(requirementId, "需求空间不能为null");
        Guard.notNull(appId, "appId不能为null");
        RequiremenUnderInterfacesDTO ret = caseSetService.getRequiremenUnderInterfaces(requirementId, appId);

        return CommonResult.buildSuccessResult(ret);
    }

    /**
     * 添加用例集
     */
    @PostMapping("/add")
    public CommonResult<String> add(@RequestBody CaseSet caseSet) {
        caseSetService.add(caseSet);
        return CommonResult.buildSuccessResult("ok");
    }
    /**
     * 通过id删除用例集相关
     */
    @GetMapping("/delById")
    public CommonResult<String> delById(Long id) {

        caseSetService.delById(id);
        return CommonResult.buildSuccessResult("ok");
    }
    /**
     * 通过id删除用例集相关
     */
    @GetMapping("/detail")
    public CommonResult<CaseSetDTO> detailById(Long id) {
        CaseSetDTO caseSet = caseSetService.detailById(id);
        return CommonResult.buildSuccessResult(caseSet);
    }
}
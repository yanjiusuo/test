package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.entity.ApproveManage;
import com.jd.workflow.console.service.IApproveService;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.console.base.CommonResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * 贡献文档管理 前端控制器
 * </p>
 *
 *
 * @author jiaxiaofang7
 * @since 2024-05-11
 */
@Slf4j
@RestController
@UmpMonitor
@RequestMapping("/approveManage")
@Api(tags = "贡献文档管理", value = "贡献文档管理")
public class ApproveManageController {

    @Resource
    IApproveService approveManageService;

    @Resource
    AccRoleServiceAdapter accRoleServiceAdapter;
    /**
     * 贡献文档分页查询
     * 入参: 搜索条件有 接口名称(模糊),贡献人(模糊),状态
     * 出参: Page<InterfaceManage>
     *
     * @param reqDTO
     * @return
     * @date: 2024/5/11 16:59
     * @author jiaxiaofang7
     */
    @PostMapping("/pageList")
    @ApiOperation(value = "贡献文档列表")
    public CommonResult<Page<ApproveManage>> pageList(@RequestBody ApprovalPageQuery reqDTO) {
        log.info("ApprovalManageController pageList query={}", JsonUtils.toJSONString(reqDTO));
        //service层
        Page<ApproveManage> approveManagePage = approveManageService.pageList(reqDTO);

        return CommonResult.buildSuccessResult(approveManagePage);
    }

    @PostMapping("/create")
    @ApiOperation(value = "创建贡献文档")
    public CommonResult createApprove(@RequestBody ApproveCreateDTO manage) {
        log.info("ApproveManageController --param{}", JSONObject.toJSONString(manage));
        //service层
        approveManageService.createApprove(manage);
        //4出参
        return CommonResult.buildSuccessResult(null);
    }

    @GetMapping("/accept")
    @ApiOperation(value = "审批通过")
    public CommonResult<Boolean> acceptApprove(Long id) {
        log.info("ApproveManageController edit query={}", JsonUtils.toJSONString(id));
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
        if (!userRoleDTO.getJapiAdmin()&&!userRoleDTO.getConsoleAdmin()) {
            //japi管理员可以审批
            return CommonResult.buildErrorCodeMsg(9999  ,"没有权限");

        }
        Boolean res=approveManageService.acceptApprove(id);
        return CommonResult.buildSuccessResult(res);
        //4.出参
    }

    @GetMapping("/reject")
    @ApiOperation(value = "审批驳回")
    public CommonResult<Boolean> rejectApprove(Long id) {

        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
        if (!userRoleDTO.getJapiAdmin()&&!userRoleDTO.getConsoleAdmin()) {
            //japi管理员可以审批
            return CommonResult.buildErrorCodeMsg(9999  ,"没有权限");

        }
        log.info("ApproveManageController edit query={}", JsonUtils.toJSONString(id));
        Boolean res= approveManageService.rejectApprove(id);
        return CommonResult.buildSuccessResult(res);
    }

    @GetMapping("/remove")
    @ApiOperation(hidden = true,value = "删除贡献文档")
    public CommonResult removeApprove(Long id) {

        String operator = UserSessionLocal.getUser().getUserId();
        log.info("ApproveManageController removeApprove id={},operator={}", JsonUtils.toJSONString(id), operator);
        //service层
        Boolean ref = approveManageService.removeApprove(id);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }
}
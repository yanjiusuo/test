package com.jd.workflow.console.controller.flow.param;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.InterfaceManageDTO;
import com.jd.workflow.console.dto.MemberRelationWithUser;
import com.jd.workflow.console.dto.UserForAddDTO;
import com.jd.workflow.console.dto.flow.param.*;
import com.jd.workflow.console.entity.FlowParam;
import com.jd.workflow.console.entity.FlowParamGroup;
import com.jd.workflow.console.service.IFlowParamGroupService;
import com.jd.workflow.console.service.IFlowParamService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 15:35
 * @Description: 公共参数controller
 */

@RestController
@Slf4j
@RequestMapping("/flowParam")
@UmpMonitor
@Api(tags = "管理公共参数")
public class FlowParamController {

    @Resource
    private IFlowParamService flowParamService;
    @Resource
    private IFlowParamGroupService flowParamGroupService;

    /**
     * 添加分组
     *
     * @param dto
     * @return
     */
    @PostMapping("/addGroup")
    @ApiOperation("添加分组")
    public CommonResult<Long> addGroup(@RequestBody FlowParamGroupDTO dto) {
        log.info("FlowParamController addGroup requestBody={} ", JSON.toJSONString(dto));
        Guard.notEmpty(dto.getGroupName(), "分组名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamGroupService.addGroup(dto));
    }

    /**
     * 修改分组
     *
     * @param dto
     * @return
     */
    @PostMapping("/editGroup")
    @ApiOperation("修改分组")
    public CommonResult<Boolean> editGroup(@RequestBody FlowParamGroupDTO dto) {
        log.info("FlowParamController editGroup requestBody={} ", JSON.toJSONString(dto));
        Guard.notEmpty(dto.getGroupName(), "分组名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamGroupService.editGroup(dto));
    }

    /**
     * 删除分组
     *
     * @param id
     * @return
     */
    @GetMapping("/removeGroup")
    @ApiOperation("删除分组")
    public CommonResult<Boolean> removeGroup(Long id) {
        log.info("FlowParamController removeGroup id={} ", id);
        Guard.notNull(id, "删除分组时id不能为空");
        return CommonResult.buildSuccessResult(flowParamGroupService.removeGroup(id));
    }


    /**
     * 查询分组
     *
     * @param dto 入参
     * @return CommonResult<QueryParamGroupResultDTO>
     */
    @PostMapping("/queryGroups")
    @ApiOperation("查询分组信息")
    public CommonResult<QueryParamGroupResultDTO> queryGroups(@RequestBody QueryParamGroupReqDTO dto) {
        log.info("FlowParamController queryGroups RequestBody={} ", dto);
        return CommonResult.buildSuccessResult(flowParamGroupService.queryGroup(dto));
    }


    /**
     * 根据分组Id查询分组信息
     *
     * @param groupId groupId
     * @return CommonResult<FlowParam>
     */
    @GetMapping("/getGroupByGroupId")
    @ApiOperation("根据参数Id查询分组信息")
    public CommonResult<FlowParamGroup> getGroupByGroupId(Long groupId) {
        log.info("FlowParamController getParamGroupByGroupId groupId={} ", groupId);
        Guard.notNull(groupId, "删除分组时id不能为空");
        return CommonResult.buildSuccessResult(flowParamGroupService.getGroupByGroupId(groupId));
    }


    /**
     * 添加参数
     *
     * @param dto
     * @return
     */
    @PostMapping("/addParam")
    @ApiOperation("添加参数")
    public CommonResult<Long> addParam(@RequestBody FlowParamDTO dto) {
        log.info("FlowParamController addParam requestBody={} ", JSON.toJSONString(dto));
        Guard.notEmpty(dto.getName(), "参数名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(dto.getValue(), "参数值不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamService.addParam(dto));
    }

    /**
     * 修改参数
     *
     * @param dto
     * @return
     */
    @PostMapping("/editParam")
    @ApiOperation("修改参数")
    public CommonResult<Boolean> editParam(@RequestBody FlowParamDTO dto) {
        log.info("FlowParamController editParam requestBody={} ", JSON.toJSONString(dto));
        Guard.notEmpty(dto.getName(), "参数名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(dto.getValue(), "参数值不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(dto.getGroupId(), "分组id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        return CommonResult.buildSuccessResult(flowParamService.editParam(dto));
    }

    /**
     * 删除参数
     *
     * @param id
     * @return
     */
    @GetMapping("/removeParam")
    @ApiOperation("删除参数")
    public CommonResult<Boolean> removeParam(Long id) {
        log.info("FlowParamController removeParam id={} ", id);
        Guard.notNull(id, "删除参数时id不能为空");
        return CommonResult.buildSuccessResult(flowParamService.removeParam(id));
    }


    /**
     * 查询参数列表
     *
     * @param dto 入参
     * @return CommonResult<QueryParamGroupResultDTO>
     */
    @PostMapping("/queryParams")
    @ApiOperation("查询参数列表")
    public CommonResult<QueryParamResultDTO> queryParams(@RequestBody QueryParamReqDTO dto) {
        log.info("FlowParamController queryParams RequestBody={} ", JSON.toJSONString(dto));
        return CommonResult.buildSuccessResult(flowParamService.queryParams(dto));
    }


    /**
     * 根据参数Id查询参数信息
     *
     * @param paramId paramId
     * @return CommonResult<FlowParam>
     */
    @GetMapping("/getParamById")
    @ApiOperation("根据参数Id查询参数信息")
    public CommonResult<FlowParam> getParamById(Long paramId) {
        log.info("FlowParamController getParamById paramId={} ", paramId);
        return CommonResult.buildSuccessResult(flowParamService.getParamById(paramId));
    }


    /**
     * 成员列表
     *
     * @param groupId
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @GetMapping("/listMemberForAdd")
    @ApiOperation(value = "分组下搜索成员")
    public CommonResult<List<UserForAddDTO>> listMemberForAdd(Long groupId, String userCode) {
        log.info("FlowParamController listMember groupId={}", groupId);
        Guard.notNull(groupId, "分组时id不能为空");
        Guard.notNull(userCode, "用户编码不能为空");
        //3.service层
        List<UserForAddDTO> result = flowParamGroupService.listMemberForAdd(groupId, userCode);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }


    /**
     * 成员列表
     *
     * @param groupId
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @GetMapping("/listMember")
    @ApiOperation(value = "分组list 查看成员")
    public CommonResult<List<MemberRelationWithUser>> listMember(Long groupId) {
        log.info("FlowParamController listMember groupId={}", groupId);
        Guard.notNull(groupId, "分组时id不能为空");
        //3.service层
        List<MemberRelationWithUser> result = flowParamGroupService.listMember(groupId);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }


    /**
     * 添加成员
     *
     * @param memberDTO
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @PostMapping("/addMember")
    @ApiOperation(value = "添加分组成员")
    public CommonResult<Boolean> addMember(@RequestBody GroupMemberDTO memberDTO) {
        log.info("FlowParamController addMember GroupMemberDTO={}", memberDTO);
        //1.判空
        Guard.notNull(memberDTO.getGroupId(), "分组时id不能为空");
        Guard.notNull(memberDTO.getUserCode(), "用户编码不能为空");
        //3.service层
        Boolean result = flowParamGroupService.addMember(memberDTO.getGroupId(), memberDTO.getUserCode());
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

}

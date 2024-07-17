package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.requirement.*;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInfoLog;
import com.jd.workflow.console.service.requirement.InterfaceSpaceService;
import com.jd.workflow.console.service.requirement.RequirementInfoLogService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


import com.jd.workflow.console.base.annotation.UmpMonitor;
import io.swagger.annotations.Api;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29
 */
@Slf4j
@RestController
@UmpMonitor
@RequestMapping("/interfaceSpace")
@Api(tags = "接口空间管理", value = "接口空间管理")
public class InterfaceSpaceController {


    @Autowired
    private InterfaceSpaceService interfaceSpaceService;

    @Autowired
    private RequirementInfoService requirementInfoService;

    @Autowired
    private RequirementInfoLogService requirementInfoLogService;


    /**
     * @param interfaceSpaceDTO
     * @return
     */
    @PostMapping("/addInterfaceSpace")
    @ApiOperation(value = "新建需求空间")
    public CommonResult<Long> addInterfaceSpace(@RequestBody InterfaceSpaceDTO interfaceSpaceDTO) {
        log.info("InterfaceSpaceController addInterfaceSpace query={}", JsonUtils.toJSONString(interfaceSpaceDTO));
        //1.判空
        Guard.notNull(interfaceSpaceDTO.getSpaceName(), "空间名称不能为空");
        if(CollectionUtils.isNotEmpty(interfaceSpaceDTO.getMembers())){
            Guard.assertTrue(interfaceSpaceDTO.getMembers().size()<50,"成员太多了，不要超过50人");
        }
        if(StringUtils.isNotBlank(interfaceSpaceDTO.getCode())){
            List<RequirementInfo> infos=interfaceSpaceService.getRequirementByDemandCode(interfaceSpaceDTO.getCode());
            Guard.assertTrue(CollectionUtils.isEmpty(infos), "已经关联过该需求，请勿重复关联");

        }
        //3.service层
        Long ref = interfaceSpaceService.createSpace(interfaceSpaceDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param interfaceSpaceDTO
     * @return
     */
    @PostMapping("/editInterfaceSpace")
    @ApiOperation(value = "编辑需求空间")
    public CommonResult<Long> editInterfaceSpace(@RequestBody InterfaceSpaceDTO interfaceSpaceDTO) {
        log.info("InterfaceSpaceController editInterfaceSpace query={}", JsonUtils.toJSONString(interfaceSpaceDTO));
        //1.判空
        Guard.notNull(interfaceSpaceDTO.getId(), "id不能为空");
        RequirementInfo exist= requirementInfoService.getById(interfaceSpaceDTO.getId());
        if (StringUtils.isBlank(exist.getRelatedRequirementCode()) && StringUtils.isNotBlank(interfaceSpaceDTO.getCode())) {
            List<RequirementInfo> infos = interfaceSpaceService.getRequirementByDemandCode(interfaceSpaceDTO.getCode());
            Guard.assertTrue(CollectionUtils.isEmpty(infos), "已经关联过该需求，请勿重复关联");
        }
        //3.service层
        Long ref = interfaceSpaceService.editSpace(interfaceSpaceDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/openInterfaceSpace")
    @ApiOperation(value = "开放需求空间")
    public CommonResult<Boolean> openInterfaceSpace(@RequestBody InterfaceSpaceDTO interfaceSpaceDTO) {
        log.info("InterfaceSpaceController openInterfaceSpace query={}", JsonUtils.toJSONString(interfaceSpaceDTO));
        //1.判空
        if(1==interfaceSpaceDTO.getShelve()){
            Guard.notNull(interfaceSpaceDTO.getId(), "id不能为空");
            Guard.notNull(interfaceSpaceDTO.getOpenSolutionName(), "开放名称不能为空");
            Guard.notNull(interfaceSpaceDTO.getOpenType(), "开放类型不能为空");
        }
        Boolean ref = interfaceSpaceService.openSpace(interfaceSpaceDTO);
        return CommonResult.buildSuccessResult(ref);
    }


    @PostMapping("/listInterfaceSpace")
    @ApiOperation(value = "开放空间查询")
    public CommonResult<IPage<RequirementInfoDto>> listInterfaceSpace(@RequestBody InterfaceSpaceParam interfaceSpaceDTO) {
        IPage<RequirementInfoDto> rest= interfaceSpaceService.queryOpenSpaceList(interfaceSpaceDTO);
        return CommonResult.buildSuccessResult(rest);
    }

    /**
     * @param interfaceSpaceDTO
     * @return
     */
    @PostMapping("/removeInterfaceSpace")
    @ApiOperation(value = "删除接口空间")
    public CommonResult<Long> removeInterfaceSpace(@RequestBody InterfaceSpaceDTO interfaceSpaceDTO) {
        log.info("InterfaceSpaceController removeInterfaceSpace query={}", JsonUtils.toJSONString(interfaceSpaceDTO));
        //1.判空
        Guard.notNull(interfaceSpaceDTO.getId(), "id不能为空");

        //3.service层
        Long ref = interfaceSpaceService.deleteSpace(interfaceSpaceDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param spaceId 空间id
     * @return
     */
    @GetMapping("/getById")
    @ApiOperation(value = "需求空间详情")
    public CommonResult<InterfaceSpaceDetailDTO> getById(@RequestParam Long spaceId) {
        log.info("InterfaceSpaceController getById query={}", spaceId);
        //1.判空
        Guard.notNull(spaceId, "id不能为空");

        //3.service层
        InterfaceSpaceDetailDTO ref = interfaceSpaceService.getSpaceInfo(spaceId);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param spaceId 空间id
     * @return
     */
    @GetMapping("/getStaticById")
    @ApiOperation(value = "接口空间统计信息")
    public CommonResult<InterfaceSpaceStaticDTO> getStaticById(@RequestParam Long spaceId) {
        log.info("InterfaceSpaceController getStaticById query={}", spaceId);
        //1.判空
        Guard.notNull(spaceId, "id不能为空");

        //3.service层
        InterfaceSpaceStaticDTO ref = interfaceSpaceService.getSpaceInfoStatic(spaceId);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param spaceId 空间id
     * @param current 当前页
     * @param size 分页大小
     * @return
     */
    @GetMapping("/getSpaceUsers")
    @ApiOperation(value = "需求空间成员列表")
    public CommonResult<InterfaceSpaceUser> getSpaceUsers(@RequestParam Long spaceId,Long current,Long size,@RequestParam(required = false) String search) {
        log.info("InterfaceSpaceController getSpaceUsers query={}", spaceId);
        //1.判空
        Guard.notNull(spaceId, "id不能为空");
        Guard.notNull(current, "current当前页不可为空");
        Guard.notNull(size, "size不能为空");


        //3.service层
        InterfaceSpaceUser ref = interfaceSpaceService.pageSpaceUser(spaceId,search,current,size);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param removeSpaceUser
     * @return
     */
    @PostMapping("/removeInterfaceSpaceUsers")
    @ApiOperation(value = "删除需求空间成员")
    public CommonResult<Boolean> removeInterfaceSpaceUsers(@RequestBody RemoveSpaceUserDTO removeSpaceUser) {
        log.info("InterfaceSpaceController removeInterfaceSpace query={}", JsonUtils.toJSONString(removeSpaceUser));
        //1.判空
        Guard.notNull(removeSpaceUser.getSpaceId(), "id不能为空");

        //3.service层
        Boolean ref = interfaceSpaceService.removeUser(removeSpaceUser);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param addSpaceUserDTO
     * @return
     */
    @PostMapping("/addInterfaceSpaceUsers")
    @ApiOperation(value = "修改需求空间成员")
    public CommonResult<Boolean> addInterfaceSpaceUsers(@RequestBody AddSpaceUserDTO addSpaceUserDTO) {
        log.info("InterfaceSpaceController addInterfaceSpaceUsers query={}", JsonUtils.toJSONString(addSpaceUserDTO));
        //1.判空
        Guard.notNull(addSpaceUserDTO.getSpaceId(), "id不能为空");

        //3.service层
        Boolean ref = interfaceSpaceService.addUser(addSpaceUserDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param spaceId
     * @return
     */
    @GetMapping("/checkInterfaceSpaceUsers")
    @ApiOperation(value = "判断当前登陆人员是否为需求空间成员")
    public CommonResult<InterfaceSpaceUser> checkInterfaceSpaceUsers(@RequestParam Long spaceId) {
        log.info("InterfaceSpaceController checkInterfaceSpaceUsers query={}", JsonUtils.toJSONString(spaceId));
        //1.判空
        Guard.notNull(spaceId, "id不能为空");

        //3.service层

        InterfaceSpaceUser ref = interfaceSpaceService.checkUser(spaceId, UserSessionLocal.getUser().getUserId());
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param interfaceSpaceLogParam
     * @return
     */
    @PostMapping("/queryInterfaceSpaceLog")
    @ApiOperation(value = "需求空间修改日志")
    public CommonResult<Page<RequirementInfoLog>> queryInterfaceSpaceLog(@RequestBody InterfaceSpaceLogParam interfaceSpaceLogParam) {
        log.info("InterfaceSpaceController queryInterfaceSpaceLog query={}", JsonUtils.toJSONString(interfaceSpaceLogParam));
        //1.判空
        Guard.notNull(interfaceSpaceLogParam.getSpaceId(), "id不能为空");

        //3.service层
        Page<RequirementInfoLog> ref = requirementInfoLogService.queryLog(interfaceSpaceLogParam);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param interfaceSpaceParam
     * @return
     */
    @PostMapping("/queryInterfaceSpaceList")
    @ApiOperation(value = "需求空间列表")
    public CommonResult<Page<InterfaceSpaceDetailDTO>> queryInterfaceSpaceList(@RequestBody InterfaceSpaceParam interfaceSpaceParam) {
        log.info("InterfaceSpaceController queryInterfaceSpaceList query={}", JsonUtils.toJSONString(interfaceSpaceParam));
        //1.判空
//        Guard.notNull(interfaceSpaceLogParam.getSpaceId(), "id不能为空");

        //3.service层
        Page<InterfaceSpaceDetailDTO> ref = interfaceSpaceService.querySpaceList(interfaceSpaceParam);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/updateOwner")
    @ApiOperation(value = "修改负责人接口")
    public CommonResult<Boolean> updateOwner(@RequestBody AddSpaceUserDTO addSpaceUserDTO) {
        log.info("InterfaceSpaceController updateOwner query={}", JsonUtils.toJSONString(addSpaceUserDTO));
        //1.判空
        Guard.notNull(addSpaceUserDTO.getSpaceId(), "id不能为空");

        //3.service层
        Boolean ref = interfaceSpaceService.updateOwner(addSpaceUserDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }


}

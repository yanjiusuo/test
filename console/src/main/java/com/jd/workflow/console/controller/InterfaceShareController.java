package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.InterfaceShareTreeDTO;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.share.*;
import com.jd.workflow.console.entity.share.InterfaceShareGroup;
import com.jd.workflow.console.service.share.IInterfaceShareGroupService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 接口管理 前端控制器
 * </p>
 *
 * @author xinwengang
 * @since 2023/3/25
 */
@Slf4j
@RestController
@UmpMonitor
@RequestMapping("/interfaceShare")
@Api(tags = "接口分享", value = "接口分享")
public class InterfaceShareController {

    @Resource
    IInterfaceShareGroupService interfaceShareService;


    /**
     * @param interfaceShareDTO
     * @return
     */
    @PostMapping("/addInterfaceShare")
    @ApiOperation(value = "新建分享")
    public CommonResult<Long> addInterfaceShare(@RequestBody InterfaceShareDTO interfaceShareDTO) {
        log.info("InterfaceShareController addInterfaceShare query={}", JsonUtils.toJSONString(interfaceShareDTO));
        //1.判空
        Guard.notNull(interfaceShareDTO.getAcrossApp(), "是否跨应用不能为空");
        Guard.notNull(interfaceShareDTO.getInterfaceShareTreeDTO(), "分享接口树不能为空");
        Guard.notEmpty(interfaceShareDTO.getShareGroupName(), "分享名称不能为空");
        //3.service层
        Long ref = interfaceShareService.addInterfaceShare(interfaceShareDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }
    @PostMapping("/appendInterfaceShare")
    @ApiOperation(value = "追加新分享")
    public CommonResult<Boolean> appendInterfaceShare(@RequestBody AppendShareDTO interfaceShareDTO) {
        interfaceShareService.appendInterfaceShare(interfaceShareDTO);
        //4.出参
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @param shareName 分享名称
     * @return
     */
    @GetMapping("/validShareName")
    @ApiOperation(value = "校验分享名称是否重复")
    public CommonResult<Boolean> validShareName(String shareName) {
        log.info("InterfaceShareController validShareName shareName={}", shareName);
        //1.判空
        Guard.notNull(shareName, "分享名称不能为空");
        //2.出参
        return CommonResult.buildSuccessResult(interfaceShareService.validShareName(shareName));
    }


    /**
     * @param dto
     * @return
     */
    @PostMapping("/removeInterfaceShare")
    @ApiOperation(value = "删除分享")
    public CommonResult<Boolean> removeInterfaceShare(@RequestBody RemoveShareGroupDTO dto) {
        log.info("InterfaceShareController removeInterfaceShare dto={}", dto);
        //1.判空
        Guard.notNull(dto.getShareGroupId(), "分享分组id不能为空");
        Guard.notNull(dto.getType(), "操作type不能为空");
        //4.出参
        return CommonResult.buildSuccessResult(interfaceShareService.removeInterfaceShare(dto));
    }

    /**
     * @param shareGroupId 分享分组id
     * @return
     */
    @GetMapping("/addShareUser")
    @ApiOperation(value = "分享分组新增被分享人")
    public CommonResult<Boolean> addShareUser(Long shareGroupId) {
        log.info("InterfaceShareController addShareUser shareGroupId={}", shareGroupId);
        //1.判空
        Guard.notNull(shareGroupId, "分享分组id不能为空");
        //2.出参
        return CommonResult.buildSuccessResult(interfaceShareService.addShareUser(shareGroupId));
    }


    /**
     * @param query
     * @return
     */
    @PostMapping("/queryInterfaceShareGroup")
    @ApiOperation(value = "查询分享分组列表")
    public CommonResult<QueryShareGroupResultDTO> queryInterfaceShareGroup(@RequestBody QueryShareGroupReqDTO query) {
        query.setCreator(UserSessionLocal.getUser().getUserId());
        return CommonResult.buildSuccessResult(interfaceShareService.queryInterfaceShareGroup(query));
    }


    /**
     * 获取接口分享树
     *
     * @param shareGroupId
     * @return
     */
    @GetMapping("/findInterfaceShareTree")
    @ApiOperation(value = "获取接口分享树")
    public CommonResult<InterfaceShareTreeDTO> findInterfaceShareTree(Long shareGroupId) {
        log.info("InterfaceShareController findInterfaceShareTree shareGroupId={} ", shareGroupId);
        Guard.notNull(shareGroupId, "shareGroupId");
        return CommonResult.buildSuccessResult(interfaceShareService.findInterfaceShareTree(shareGroupId));
    }


    @PostMapping("/modifyInterfaceShareTree")
    @ApiOperation(value = "修改分组树")
    public CommonResult<Boolean> modifyInterfaceShareTree(@RequestBody InterfaceShareTreeDTO dto) {
        log.info("InterfaceShareController modifyInterfaceShareTree requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto, "保存接口下方法分组树时入参不能为空");
        Guard.notNull(dto.getShareGroupId(), "保存接口下方法分组树时入参shareGroupId不能为空");
        Guard.notEmpty(dto.getGroupLastVersion(), "保存接口下方法分组树时入参groupLastVersion不能为空");
        return CommonResult.buildSuccessResult(interfaceShareService.modifyInterfaceShareTree(dto,true));
    }


    @GetMapping("/addGroup")
    @ApiOperation(value = "新增分组")
    public CommonResult<Long> addGroup(Long shareGroupId, String name, Long parentId) {
        //操作分享分组树
        log.info("InterfaceShareController addGroup interfaceId={} , name={}", shareGroupId, name);
        Guard.notNull(shareGroupId, "新增分享下方法分组时shareGroupId不能为空");
        Guard.notEmpty(name, "新增接口下方法分组时name不能为空");
        return CommonResult.buildSuccessResult(interfaceShareService.addGroup(name, shareGroupId, parentId));

    }

    /**
     *
     * @param id 分享组id
     * @return
     */
    @GetMapping("/getById")
    @ApiOperation(value = "获取分组详情")
    public CommonResult<InterfaceShareGroup> getById(Long id) {
        //操作分享分组树
        InterfaceShareGroup group = interfaceShareService.getById(id);
        if(group == null){
            throw new BizException("无效的分享id");
        }
        group.init();
        return CommonResult.buildSuccessResult(group);

    }

    @GetMapping("/modifyGroup")
    @ApiOperation(value = "修改分组名称")
    public CommonResult<Boolean> modifyGroup(Long shareGroupId, Long id, String name, Long parentId) {
        log.info("InterfaceMethodGroupController modifyGroup shareGroupId={} , id={} , name={}，parentId={}", shareGroupId, id, name, parentId);
        Guard.notNull(shareGroupId, "shareGroupId");
        Guard.notNull(id, "修改接口下方法分组时id不能为空");
        Guard.notEmpty(name, "修改接口下方法分组时name不能为空");
        return CommonResult.buildSuccessResult(interfaceShareService.modifyGroupName(shareGroupId, id, name, parentId));
    }

    @GetMapping("/removeGroup")
    @ApiOperation(value = "删除分组")
    public CommonResult<Boolean> removeGroup(Long shareGroupId, Long id, Long parentId) {
        log.info("InterfaceMethodGroupController removeGroup interfaceId={} , id={},parentId={} ", shareGroupId, id, parentId);
        Guard.notNull(shareGroupId, "删除接口下方法分组时shareGroupId不能为空");
        Guard.notNull(id, "删除接口下方法分组时id不能为空");
        return CommonResult.buildSuccessResult(interfaceShareService.removeGroup(shareGroupId, id, parentId));
    }

    /**
     *
     * @param shareGroupId 分享组id
     * @param id 方法id
     * @param parentId 父节点id
     * @return
     */
    @GetMapping("/removeShareMethod")
    @ApiOperation(value = "删除分享分组里的方法")
    public CommonResult<Boolean> removeShareMethod(Long shareGroupId, Long id, Long parentId) {
        log.info("InterfaceMethodGroupController removeGroup interfaceId={} , id={},parentId={} ", shareGroupId, id, parentId);
        Guard.notNull(shareGroupId, "删除接口下方法分组时shareGroupId不能为空");
        Guard.notNull(id, "删除接口下方法分组时id不能为空");
        return CommonResult.buildSuccessResult(interfaceShareService.removeShareMethod(shareGroupId, id, parentId));
    }
}

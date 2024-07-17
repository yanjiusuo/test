package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.share.IInterfaceShareGroupService;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import io.swagger.annotations.ApiOperation;

/**
 * 项目名称：parent
 * 类 名 称：InterfaceMethodGroupController
 * 类 描 述：方法分组管理
 * 创建时间：2022-11-08 17:46
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/interfaceMethodGroup")
@UmpMonitor
@Api(value = "方法分组管理",tags="方法分组管理")
public class InterfaceMethodGroupController {

    @Resource
    private IInterfaceMethodGroupService iInterfaceMethodGroupService;

    @Resource
    IInterfaceShareGroupService interfaceShareService;

    /**
     *
     * @param interfaceId 接口id
     * @param name 分组名
     * @param parentId 父节点id
     * @return
     */
    @GetMapping("/addGroup")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "新增分组")
    public CommonResult<Long> addGroup(Long interfaceId, String name, @RequestParam(required = false)String enName, Long parentId) {//,int type
       /* if(type == 1){
            //操作分享分组树
            log.info("InterfaceShareController addGroup interfaceId={} , name={}", interfaceId, name);
            Guard.notNull(interfaceId, "新增分享下方法分组时shareGroupId不能为空");
            Guard.notEmpty(name, "新增接口下方法分组时name不能为空");
            return CommonResult.buildSuccessResult(interfaceShareService.addGroup(name, interfaceId, parentId));
        }*/
        log.info("InterfaceMethodGroupController addGroup interfaceId={} , name={}", interfaceId, name);
        Guard.notNull(interfaceId, "新增接口下方法分组时interfaceId不能为空");
        Guard.notEmpty(name, "新增接口下方法分组时name不能为空");
        return CommonResult.buildSuccessResult(iInterfaceMethodGroupService.addGroup(name,enName, interfaceId, parentId));
    }

    /**
     *
     * @param interfaceId 接口id
     * @param id 分组id
     * @param name 分组名
     * @return
     */
    @GetMapping("/modifyGroup")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "修改分组名称")
    public CommonResult<Boolean> modifyGroup(Long interfaceId, Long id, String name, @RequestParam(required = false)String enName) {
        log.info("InterfaceMethodGroupController modifyGroup interfaceId={} , id={} , name={}", interfaceId, id, name);
        Guard.notNull(interfaceId, "修改接口下方法分组时interfaceId不能为空");
        Guard.notNull(id, "修改接口下方法分组时id不能为空");
        Guard.notEmpty(name, "修改接口下方法分组时name不能为空");
        return CommonResult.buildSuccessResult(iInterfaceMethodGroupService.modifyGroupName( id, name,enName));
    }

    @GetMapping("/removeGroup")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "删除分组")
    public CommonResult<Boolean> removeGroup(Long interfaceId, Long id) {
        log.info("InterfaceMethodGroupController removeGroup interfaceId={} , id={} ", interfaceId, id);
        Guard.notNull(interfaceId, "删除接口下方法分组时interfaceId不能为空");
        Guard.notNull(id, "删除接口下方法分组时id不能为空");
        return CommonResult.buildSuccessResult(iInterfaceMethodGroupService.removeGroup( id));
    }

    @GetMapping("/findMethodGroupTree")
    @ApiOperation(value = "获取接口分组树")
    public //@Authorization(key="interfaceId")
    CommonResult<MethodGroupTreeDTO> findMethodGroupTree(Long interfaceId) {
        log.info("InterfaceMethodGroupController findMethodGroupTree interfaceId={} ", interfaceId);
        Guard.notNull(interfaceId, "获取接口下方法分组树时interfaceId不能为空");
        return CommonResult.buildSuccessResult(iInterfaceMethodGroupService.findMethodGroupTree(interfaceId));
    }

    @GetMapping("/findAppHttpTree")
    @ApiOperation(value = "获取接口分组树")
    public //@Authorization(key="interfaceId")
    CommonResult<MethodGroupTreeDTO> findAppHttpTree(Long appId) {
        log.info("InterfaceMethodGroupController findAppHttpTree appId={} ", appId);
        Guard.notNull(appId, "获取接口下方法分组树时interfaceId不能为空");
        return CommonResult.buildSuccessResult(iInterfaceMethodGroupService.findAppHttpTree(appId));
    }

    @PostMapping("/modifyMethodGroupTree")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "修改分组树")
    public CommonResult<Boolean> modifyMethodGroupTree(@RequestBody MethodGroupTreeDTO dto) {
        log.info("InterfaceMethodGroupController modifyMethodGroupTree requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto, "保存接口下方法分组树时入参不能为空");
        Guard.notNull(dto.getInterfaceId(), "保存接口下方法分组树时入参interfaceId不能为空");
        Guard.notEmpty(dto.getGroupLastVersion(), "保存接口下方法分组树时入参groupLastVersion不能为空");
        return CommonResult.buildSuccessResult(iInterfaceMethodGroupService.modifyMethodGroupTree(dto));
    }
}

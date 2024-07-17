package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.Result;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.JsfAliasDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.entity.JsfAlias;
import com.jd.workflow.console.service.JsfAliasService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import io.swagger.annotations.ApiOperation;

/**
 * @author shuchang21
 * @date: 2022/9/9 17:52
 */
@Slf4j
@RestController
@RequestMapping("/jsfAlias")
@UmpMonitor
@Api(value = "jsf别名管理", tags = "jsf别名管理")
public class JsfAliasController {

    @Resource
    JsfAliasService jsfAliasService;

    /**
     * 新增jsf别名
     *
     * @return
     * @date: 2022/9/9 17:52
     * @author shuchang21
     */
    @PostMapping("/add")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "添加JSF别名")
    public CommonResult<Long> add(@RequestBody JsfAliasDTO jsfAliasDTO) {
        log.info("MethodManageController add query={}", JsonUtils.toJSONString(jsfAliasDTO));
        //service层
        Long ref = jsfAliasService.add(jsfAliasDTO);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 修改jsf别名
     *
     * @return
     * @date: 2022/9/9 18：19
     * @author shuchang21
     */
    @PostMapping("/edit")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "修改JSF别名")
    public CommonResult<Long> edit(@RequestBody JsfAliasDTO jsfAliasDTO) {
        log.info("MethodManageController add query={}", JsonUtils.toJSONString(jsfAliasDTO));
        //service层
        Long ref = jsfAliasService.edit(jsfAliasDTO);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 删除jsf别名
     *
     * @return
     * @date: 2022/9/9 18：23
     * @author shuchang21
     */
    @PostMapping("/remove")
    @ApiOperation(value = "删除别名")
    public CommonResult<Boolean> remove(@RequestParam("id") Long id) {
        //service层
        Boolean ref = jsfAliasService.remove(id);
        //出参
        if (ref) {
            return CommonResult.buildSuccessResult(ref);
        } else {
            throw new BizException("删除失败");
        }
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取列表")
    public CommonResult<Page<JsfAlias>> page(Integer current, Integer size, Long interfaceId) {
        //service层
        Page<JsfAlias> result = jsfAliasService.pageJsf(current, size, interfaceId);
        //出参
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/all")
    @ApiOperation(value = "获取全部记录")
    public CommonResult<List<JsfAlias>> aliasAll(Long interfaceId) {
        //service层
        List<JsfAlias> result = jsfAliasService.aliasAll(interfaceId);
        //出参
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/allByName")
    @ApiOperation(value = "获取全部记录")
    public CommonResult<List<JsfAlias>> aliasAll(String interfaceName) {
        //service层
        List<JsfAlias> result = jsfAliasService.aliasAllByInterfaceName(interfaceName);
        //出参
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/getIps")
    @ApiOperation(value = "获取全部记录")
    public CommonResult<List<Map<String, Object>>> getIps(String interfaceName, String alias) {
        //service层
        Result<List<Server>> result = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            result = jsfAliasService.getIps(interfaceName, alias);

            if(result.getData() != null){
                list = result.getData().stream().map(vs -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", vs.getId());
                    map.put("server", vs.getIp() + ":" + vs.getPort());
                    return map;
                }).collect(Collectors.toList());
            }
            //出参
        } catch (Exception e) {
            log.error("获取ip列表失败:interfaceName={}",interfaceName,e );
           return  CommonResult.buildErrorCodeMsg(500, e.getMessage());
        }
        return CommonResult.buildSuccessResult(list);
    }

    @GetMapping("/queryAliasFromJsf")
    @ApiOperation(value = "从jsf查询")
    public CommonResult<List<JsfAlias>> queryAliasFromJsf(@RequestParam String interfaceName) {
        //service层
        List<JsfAlias> result = jsfAliasService.queryAliasFromJsf(interfaceName);
        //出参
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/initAliasAllById")
    @ApiOperation(value = "同步jsf别名")
    public CommonResult<Boolean> initAliasAllById(Long interfaceId) {
        //service层
        Boolean result = jsfAliasService.initAliasAllById(interfaceId);
        //出参
        return CommonResult.buildSuccessResult(result);
    }


}

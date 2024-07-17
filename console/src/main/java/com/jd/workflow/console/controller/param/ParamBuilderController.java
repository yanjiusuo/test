package com.jd.workflow.console.controller.param;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.controller.utils.DtBeanUtils;
import com.jd.workflow.console.dto.requirement.ParamBuilderAddParam;
import com.jd.workflow.console.dto.requirement.ParamBuilderParam;
import com.jd.workflow.console.entity.param.ParamBuilder;
import com.jd.workflow.console.service.param.IParamBuilderService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 入参构建器 前端控制器
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
@Controller
@RequestMapping("/param-builder")
public class ParamBuilderController {

    /**
     *
     */
    @Resource
    IParamBuilderService paramBuilderService;


    @PostMapping("/listPage")
    @ResponseBody
    @ApiOperation(value = "用例列表")
    public CommonResult<Page> listPage(@RequestBody ParamBuilderParam param) {
        return CommonResult.buildSuccessResult(paramBuilderService.listPage(param));
    }


    @GetMapping("/getById/{id}")
    @ResponseBody
    @ApiOperation(value = "用例详情")
    public CommonResult<ParamBuilder> getById(@PathVariable(value="id") Long id) {
        ParamBuilder builder = paramBuilderService.getById(id);
        return CommonResult.buildSuccessResult(builder);
    }

    @GetMapping("/runById/{id}")
    @ResponseBody
    @ApiOperation(value = "执行用例")
    public CommonResult<String> runById(@PathVariable(value="id") Long id, HttpServletRequest req) {
        paramBuilderService.run(id, req.getHeader("Cookie"));
        return CommonResult.buildSuccessResult("ok");
    }

    @GetMapping("/batchRun")
    @ResponseBody
    @ApiOperation(value = "批量执行")
    public CommonResult<String> batchRun(@RequestParam(value="ids") String ids, HttpServletRequest req) {
        paramBuilderService.batchRun(ids, req.getHeader("Cookie"));
        return CommonResult.buildSuccessResult("ok");
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新增用例")
    public CommonResult<Boolean> add(@RequestBody ParamBuilderAddParam param, HttpServletRequest req) {
        return CommonResult.buildSuccessResult(paramBuilderService.saveCase(param, req.getHeader("Cookie")));
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation(value = "更新用例")
    public CommonResult<Boolean> update(@RequestBody ParamBuilderAddParam param, HttpServletRequest req) {
        return CommonResult.buildSuccessResult(paramBuilderService.update(param, req.getHeader("Cookie")));
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    @ApiOperation(value = "删除用例")
    public CommonResult<Boolean> delete(@PathVariable(value="id") Long id) {
        return CommonResult.buildSuccessResult(paramBuilderService.logicDelete(id));
    }

    @GetMapping("/copy")
    @ApiOperation(value = "复制用例")
    @ResponseBody
    public CommonResult<Long> copy(@RequestParam(value="id") Long id) {
        return CommonResult.buildSuccessResult(paramBuilderService.copy(id));
    }

}

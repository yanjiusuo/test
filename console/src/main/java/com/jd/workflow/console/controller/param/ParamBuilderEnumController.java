package com.jd.workflow.console.controller.param;


import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.enums.AssertionOptEnum;
import com.jd.workflow.console.base.enums.ParamOptTypeEnum;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 入参构建器记录 前端控制器
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
@Controller
@RequestMapping("/param-builder-enum")
public class ParamBuilderEnumController {


    @GetMapping("/list/preOpt")
    @ResponseBody
    @ApiOperation(value = "获取前置操作列表")
    public CommonResult<List> preOptList() {
        return CommonResult.buildSuccessResult(ParamOptTypeEnum.getPreOptList());
    }

    @GetMapping("/list/postOpt")
    @ResponseBody
    @ApiOperation(value = "获取后置操作列表")
    public CommonResult<List> postOptList() {
        return CommonResult.buildSuccessResult(ParamOptTypeEnum.getPostOptList());
    }

    @GetMapping("/list/assertionOpt")
    @ResponseBody
    @ApiOperation(value = "获取断言操作符列表")
    public CommonResult<List> assertionOptList() {
        return CommonResult.buildSuccessResult(AssertionOptEnum.getOptList());
    }

}

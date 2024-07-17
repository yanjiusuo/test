package com.jd.workflow.console.controller.param;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dto.requirement.ParamBuilderScriptParam;
import com.jd.workflow.console.entity.param.ParamBuilderScript;
import com.jd.workflow.console.service.impl.OpenApiService;
import com.jd.workflow.console.service.param.IParamBuilderScriptService;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 物料工具 前端控制器
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-13
 */
@Controller
@RequestMapping("/param-builder-script")
public class ParamBuilderScriptController {

    /**
     *
     */
    @Resource
    IParamBuilderScriptService service;

    @Autowired
    OpenApiService openApiService;

    @PostMapping("/listPage")
    @ResponseBody
    @ApiOperation(value = "分页列表，支持搜索")
    public CommonResult<Page> listPage(@RequestBody ParamBuilderScriptParam param) {
        return CommonResult.buildSuccessResult(service.listPage(param));
    }


    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "脚本列表")
    public CommonResult<List<ParamBuilderScript>> list(@Param("source") String source,@Param("appId") String appId) {
        List<ParamBuilderScript> list = service.lambdaQuery().eq(ParamBuilderScript::getYn, DataYnEnum.VALID.getCode())
//                .eq(Objects.nonNull(source),ParamBuilderScript::getScriptSource, source)
                .eq(Objects.nonNull(appId),ParamBuilderScript::getAppId, appId)
                .list();
        return CommonResult.buildSuccessResult(list);
    }

    @GetMapping("/getById/{id}")
    @ResponseBody
    @ApiOperation(value = "查询脚本")
    public CommonResult<ParamBuilderScript> getById(@PathVariable(value="id") Long id) {
        ParamBuilderScript one = service.getById(id);
        return CommonResult.buildSuccessResult(one);
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建脚本")
    public CommonResult<Boolean> add(@RequestBody ParamBuilderScript param) {
        return CommonResult.buildSuccessResult(service.saveScript(param));
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation(value = "更新脚本")
    public CommonResult<Boolean> update(@RequestBody ParamBuilderScript param) {
        return CommonResult.buildSuccessResult(service.editScript(param));
    }

    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除脚本")
    public CommonResult<Boolean> delete(@RequestBody ParamBuilderScript param) {
        ParamBuilderScript bean = new ParamBuilderScript();
        bean.setId(param.getId());
        bean.setYn(0);
        boolean result = service.updateById(bean);
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/copy")
    @ResponseBody
    @ApiOperation(value = "复制")
    public CommonResult<ParamBuilderScript> copy(@RequestParam(value="id") Long id) {
        return CommonResult.buildSuccessResult(service.copy(id));
    }

    @GetMapping("/getScriptResult")
    @ResponseBody
    @ApiOperation(value = "获取模版出参")
    public CommonResult<String> getScriptResult(@RequestParam(value="param") String param) {
        return CommonResult.buildSuccessResult(openApiService.testDataExecute(JSON.parseObject(param)));
    }

}

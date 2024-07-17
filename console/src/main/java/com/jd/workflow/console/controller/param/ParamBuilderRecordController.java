package com.jd.workflow.console.controller.param;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordParam;
import com.jd.workflow.console.entity.param.ParamBuilderRecord;
import com.jd.workflow.console.service.param.IParamBuilderRecordService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 入参构建器记录 前端控制器
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
@Controller
@RequestMapping("/param-builder-record")
public class ParamBuilderRecordController {

    /**
     *
     */
    @Resource
    IParamBuilderRecordService recordService;


    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "执行记录列表")
    public CommonResult<Page<ParamBuilderRecord>> list(ParamBuilderRecordParam param) {
        return CommonResult.buildSuccessResult(recordService.listPage(param));
    }

    @GetMapping("/getById/{id}")
    @ResponseBody
    @ApiOperation(value = "查询记录")
    public CommonResult<ParamBuilderRecord> getById(@PathVariable(value="id") Long id) {
        return CommonResult.buildSuccessResult(recordService.getOneById(id));
    }

    @PostMapping("/updateStatus/{id}/{pwd}")
    @ResponseBody
    @ApiOperation(value = "不开放,自测使用")
    public CommonResult<Boolean> update(@PathVariable(value="id") Long id,@PathVariable(value="pwd") String pwd) {
        if(!"plm".equalsIgnoreCase(pwd)){
            return CommonResult.buildSuccessResult(false);
        }
        ParamBuilderRecord record = recordService.getById(id);
        record.setRunStatus(1);
        boolean result = recordService.updateById(record);
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/delete/{id}")
    @ResponseBody
    @ApiOperation(value = "删除记录")
    public CommonResult<Boolean> delete(@PathVariable(value="id") Long id) {
        ParamBuilderRecord paramBuilderRecord = new ParamBuilderRecord();
        paramBuilderRecord.setId(id);
        paramBuilderRecord.setYn(DataYnEnum.INVALID.getCode());
        boolean result = recordService.updateById(paramBuilderRecord);
        return CommonResult.buildSuccessResult(result);
    }
}

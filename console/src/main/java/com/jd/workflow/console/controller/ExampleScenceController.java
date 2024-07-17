package com.jd.workflow.console.controller;


import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.AuthorizationKeyTypeEnum;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.entity.ExampleScence;
import com.jd.workflow.console.service.ExampleScenceService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/exampleScencce")
@UmpMonitor
@Api(value = "示例场景",tags="示例场景")
public class ExampleScenceController {


    @Resource
    private ExampleScenceService exampleScenceService;

    @GetMapping("/list")
    @ApiOperation(value = "查看场景")
    public CommonResult<List<ExampleScence>> listByMethodId(Long methodId) {
        log.info("ExampleScenceController listByMethodId query={}",methodId);
        //1.判空
        //2.入参封装
        //3.service层
        List<ExampleScence> result = exampleScenceService.listByMethodId(methodId);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 添加
     *
     * @param scences
     * @return
     * @date: 2022/5/16 20:40
     * @author wubaizhao1
     */
    @PostMapping("/add")
    @ApiOperation(value = "编辑场景，覆盖操作")
    public CommonResult<Map<Long,Boolean>> removeAndadd(@RequestBody List<ExampleScence> scences) {
        log.info("ExampleScenceController add query={}", JsonUtils.toJSONString(scences));
        if(CollectionUtils.isEmpty(scences)){
            return CommonResult.buildSuccessResult(null);
        }
        for (ExampleScence scence : scences) {
            Guard.notEmpty(scence.getScenceName(),"name不能为空");
            Guard.notEmpty(scence.getMethodId(),"methodId不能为空");
            Guard.notEmpty(scence.getInputExample(),"inputExample不能为空");
        }
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Map<Long,Boolean> ref = exampleScenceService.removeAndadd(scences);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/addSimple")
    @ApiOperation(value = "添加场景，添加操作")
    public CommonResult<Map<Long,Boolean>> addSimple(@RequestBody List<ExampleScence> scences) {
        log.info("ExampleScenceController add query={}", JsonUtils.toJSONString(scences));
        if(CollectionUtils.isEmpty(scences)){
            return CommonResult.buildSuccessResult(null);
        }
        for (ExampleScence scence : scences) {
            Guard.notEmpty(scence.getScenceName(),"name不能为空");
            Guard.notEmpty(scence.getMethodId(),"methodId不能为空");
            Guard.notEmpty(scence.getInputExample(),"inputExample不能为空");
        }
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Map<Long,Boolean> ref = exampleScenceService.add(scences);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/remove")
    @ApiOperation(value = "删除场景")
    public CommonResult<Boolean> remove(@RequestBody Long id) {
        log.info("ExampleScenceController remove query={}", id);
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Boolean ref = exampleScenceService.remove(id);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/removeHard")
    @ApiOperation(value = "删除场景-物理删除")
    public CommonResult<Boolean> removeHard(@RequestBody Long id) {
        log.info("ExampleScenceController remove query={}", id);
        //3.service层
        Boolean ref = exampleScenceService.removeHard(id);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/getById")
    @ApiOperation(value = "获取场景详情")
    public CommonResult<ExampleScence> getById(String id) {
        log.info("ExampleScenceController getById id={}", JsonUtils.toJSONString(id));
        //入参封装
        //String operator=UserSessionLocal.getUser().getUserId();
        //service层
        ExampleScence ref = exampleScenceService.getEntity(id);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }


}

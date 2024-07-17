package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.mock.MockDocDto;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/mockInfo")
@UmpMonitor
@Api(tags = "mock管理")
public class MockExampleTestController {
    public MockExampleTestController(){
        System.out.println(123);
    }
    /**
     * 获取mock示例
     * @return mock示例
     */
    @GetMapping("/mockExample")
    @ResponseBody
    public CommonResult<List<MockDocDto>> mockDto(){
        try {
            File file = ResourceUtils.getFile("classpath:mock/mock-example.json");
            String content = IOUtils.toString(new FileInputStream(file),"utf-8");
            return CommonResult.buildSuccessResult(JsonUtils.parseArray(content,MockDocDto.class));
        } catch (Exception e) {
            log.error("获取mock示例失败",e);
            return  CommonResult.buildSuccessResult(null);
        }
    }
}

package com.jd.workflow.console.controller.doc;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.dto.doc.ListModifyLogDto;
import com.jd.workflow.console.dto.version.CompareMethodVersionDTO;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/methodModify")
@UmpMonitor
@Api(tags="方法修改日志")
public class MethodModifyLogController {
    @Autowired
    IMethodModifyLogService methodModifyLogService;
    @PostMapping(value = "/pageList")
    @ResponseBody
    public CommonResult<IPage<MethodModifyLog>> listModifyLogs(@RequestBody @Valid ListModifyLogDto dto) {

        return CommonResult.buildSuccessResult(methodModifyLogService.listModifyLogs(dto));
    }
    @GetMapping(value = "/compareMethod")
    @ResponseBody
    public CommonResult<CompareMethodVersionDTO> listModifyLogs(Long id) {

        return CommonResult.buildSuccessResult(methodModifyLogService.compareMethod(id));
    }

    @GetMapping(value = "/getDetailById")
    @ResponseBody
    public CommonResult<MethodModifyLog> getDetailById(Long id) {

        return CommonResult.buildSuccessResult(methodModifyLogService.getDetailById(id));
    }
}



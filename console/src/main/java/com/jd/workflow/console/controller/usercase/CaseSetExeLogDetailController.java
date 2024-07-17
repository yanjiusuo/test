package com.jd.workflow.console.controller.usercase;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDetail;
import com.jd.workflow.console.service.usecase.CaseSetExeLogDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@RestController
@RequestMapping("/caseSetExeLogDetail")
public class CaseSetExeLogDetailController {

    @Autowired
    private CaseSetExeLogDetailService caseSetExeLogDetailService;

    @PostMapping("/create")
    public CommonResult<Long> create(@RequestBody CaseSetExeLogDetail caseSetExeLogDetail) {
        boolean save = caseSetExeLogDetailService.save(caseSetExeLogDetail);
        return CommonResult.buildSuccessResult(caseSetExeLogDetail.getId());
    }
}

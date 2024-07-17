package com.jd.workflow.console.controller.usercase;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.entity.usecase.CaseSetExeLog;
import com.jd.workflow.console.service.jacoco.JacocoService;
import com.jd.workflow.console.service.usecase.CaseSetExeLogService;
import com.jd.workflow.console.worker.usecase.CaseSetExeWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/27
 */
@RestController
@RequestMapping("/jacoco")
public class JacocoController {

    @Autowired
    private JacocoService jacocoService;

    @Autowired
    private CaseSetExeWorker caseSetExeWorker;


    @Autowired
    private CaseSetExeLogService caseSetExeLogService;


    /**
     * 通过Id删除用例集数据
     *
     * @param ip
     * @return
     */
    @GetMapping("/exportJacoco")
    public CommonResult exportJacoco(String ip, Long id) {
        jacocoService.exportJacoco(ip, id);
        return CommonResult.buildSuccessResult(null);
    }

    /**
     * 通过Id删除用例集数据
     *
     * @param ip
     * @return
     */
    @GetMapping("/isJacocoEnabled")
    public CommonResult isJacocoEnabled(String ip) {
        jacocoService.isJacocoEnabled(ip);
        return CommonResult.buildSuccessResult(null);
    }


    /**
     * 通过Id删除用例集数据
     *
     * @param ip
     * @return
     */
    @GetMapping("/queryJacocoStage")
    public CommonResult queryJacocoStage(String ip, Long id) {
        jacocoService.queryJacocoStage(ip, id);
        return CommonResult.buildSuccessResult(null);
    }

    /**
     * 执行待执行的用例集记录
     *
     * @return
     */
    @GetMapping("/caseSetExe")
    public CommonResult caseSetExe() {
        caseSetExeWorker.caseSetExe();
        return CommonResult.buildSuccessResult(null);
    }

    /**
     * 获取用例集执行结果
     *
     * @return
     */
    @GetMapping("/queryJacocoStageWorker")
    public CommonResult queryJacocoStageWorker(Long id) {
        CaseSetExeLog caseSetExeLog = caseSetExeLogService.getById(id);
        caseSetExeWorker.doQueryJacocoStage(caseSetExeLog);
        return CommonResult.buildSuccessResult(null);
    }

    /**
     * 获取用例集执行结果
     *
     * @return
     */
    @GetMapping("/obtainCoverageReport")
    public CommonResult obtainCoverageReport(String  path) {
        caseSetExeWorker.obtainCoverageReport(path);
        return CommonResult.buildSuccessResult(null);
    }

}

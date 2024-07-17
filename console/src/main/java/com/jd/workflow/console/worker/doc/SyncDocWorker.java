package com.jd.workflow.console.worker.doc;

import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.service.doc.SyncDocService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/7/1
 */
@Service
@Slf4j
public class SyncDocWorker {

    @Autowired
    private IDocReportService iDocReportService;
    @Autowired
    private SyncDocService syncDocService;

    /**
     * 同步JSF平台文档
     * @return
     */
    @XxlJob("syncJsfPlatformDoc")
    public ReturnT<String> syncJsfPlatformDoc() {
        iDocReportService.syncDocFromJsfPlatformWorkerT1();
        return ReturnT.SUCCESS;
    }

    /**
     * 根据部署记录（T+1）更新应用接口文档
     * @return
     */
    @XxlJob("syncDocFromDeployInfo")
    public ReturnT<String> syncDocFromDeployInfo() {
        syncDocService.syncDocDependDeployInfo();
        return ReturnT.SUCCESS;
    }
}

package com.jd.workflow.console.service;

import com.jd.workflow.console.base.enums.LogLevelEnum;
import com.jd.workflow.console.entity.CamelStepLog;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogCollectService extends AbstractLogCollectService{
    @Value("${route.useDuccPublisher:true}")
    private boolean useDuccPublisher;

    @Value("${route.log.saveInterval:5000}")
    private Integer interval = 5*1000;


    @Autowired
    ICamelStepLogService stepLogService;


    @PostConstruct
    public void init(){
        this.enabled = !useDuccPublisher;
        this.saveInterval = interval;
        this.logCollector = new LogCollector() {
            @Override
            public void collect(List<LogEntity> list) {
                List<CamelStepLog> logs = list.stream().map(vs -> toLog(vs)).collect(Collectors.toList());
                stepLogService.saveBatch(logs);
            }
        };
        super.start();
    }
    public CamelStepLog toLog(LogEntity logEntity){
        CamelStepLog stepLog = new CamelStepLog();
        stepLog.setMethodId(logEntity.getMethodName()+"");
        stepLog.setCreated(new Date());
        stepLog.setBusinessId(logEntity.getBusinessName()+"");
        if(logEntity.isError()){
            stepLog.setLogLevel(LogLevelEnum.EXCEPTION.getCode());
        }else{
            stepLog.setLogLevel(LogLevelEnum.NONE.getCode());
        }
        stepLog.setLogContent(JsonUtils.toJSONString(logEntity.getMsg()));
        stepLog.setVersion(logEntity.getPublishVersion());
        return stepLog;
    }
}

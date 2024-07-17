package com.jd.workflow.console.service.watch;

import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.enums.LockTypeEnum;
import com.jd.workflow.console.service.lock.JobLockService;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
public class CodeActivityScheduleService {
    @Resource
    private JobLockService jobLockService;
    @Value("${code.enableCodeActivityStat:false}")
    private boolean enableCodeActivityStat;
    @Autowired
    CodeActivityStatisticService codeActivityStatisticService;
    @Scheduled(cron = "${codeActivity.stat_cron: 0 0/30 * ? * *}")
    public void stat(){
        try{
            if(!enableCodeActivityStat){
                log.info("code.disabled_code_activity_stat");
                return;
            }
            String dayFormat = StringHelper.formatDate(new Date(), "yyyy-MM-dd HH:mm");
            jobLockService.createLock(LockTypeEnum.CODE_ACTIVITY_STATISTIC, dayFormat);
            boolean toRunCleanLog = jobLockService.getLock(LockTypeEnum.CODE_ACTIVITY_STATISTIC, dayFormat);
            if(!toRunCleanLog){
                return;
            }
            log.info("japi.begin_codeActivityStat");
            codeActivityStatisticService.statisticDayCodeCostTime(new Date());
            codeActivityStatisticService.computeBuildTime(StringHelper.formatDate(new Date(),"yyyy-MM-dd"));
        }catch (Exception e){
            log.error("japi.err_sync_api_data");
        }

    }
}

package com.jd.workflow.console.service.job;

import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.enums.LockTypeEnum;
import com.jd.workflow.console.elastic.service.EsInterfaceService;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.doc.importer.JapiHttpDataImporter;
import com.jd.workflow.console.service.impl.ScoreManageService;
import com.jd.workflow.console.service.lock.JobLockService;
import com.jd.workflow.console.service.manage.RankScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DataSyncService {
    public DataSyncService(){
       // System.out.println(123456);
    }
    @Autowired
    JapiHttpDataImporter japiHttpDataImporter;
    @Autowired
    RankScoreService rankScoreService;
    @Resource
    private JobLockService jobLockService;
    @Autowired
    private IAppInfoService appInfoService;
    @Autowired
    private IMethodManageService methodManageService;
    @Autowired
    ScoreManageService scoreManageService;

    @Value("${job.skip_data_sync:false}")
    private boolean skipSync;
    @Autowired
    EsInterfaceService esInterfaceService;
    @Scheduled(cron = "${japi.lock_cron:0 1 0 * * ?}")
    public void japiLockCron(){
        if(skipSync){
            log.info("job.skip_create_lock");
            return;
        }
        log.info("japi.begin_create_lock");
        jobLockService.createLock(LockTypeEnum.SYNC_JAPI_DATA, DateUtil.getCurrentDate());
        jobLockService.createLock(LockTypeEnum.SYNC_JDOS_MEMBERS, DateUtil.getCurrentDate());
        jobLockService.createLock(LockTypeEnum.SYNC_INTERFACE_PROP, DateUtil.getCurrentDate());

        jobLockService.createLock(LockTypeEnum.UPDATE_RANK, DateUtil.getCurrentDate());
        jobLockService.createLock(LockTypeEnum.UPDATE_ES_INDEX, DateUtil.getCurrentDate());
    }
    @Scheduled(cron = "${japi.sync_data_cron:0 4 0 * * ?}")
    public void syncJApiData(){
        try{
            boolean toRunCleanLog = jobLockService.getLock(LockTypeEnum.SYNC_JAPI_DATA, DateUtil.getCurrentDate());
            if(!toRunCleanLog){
                 return;
            }
            log.info("japi.begin_sync_japi_app");
           // japiHttpDataImporter.initJApiApp(false);
            japiHttpDataImporter.initJapiInterfaces(null);
            japiHttpDataImporter.initJapiAppInterfaces();
        }catch (Exception e){
            log.error("japi.err_sync_api_data");
        }

    }

    @Scheduled(cron = "${jdos.sync_app_members:0 3 0 * * ?}")
    public void syncJdosMembers(){
        try{
            boolean toRunCleanLog = jobLockService.getLock(LockTypeEnum.SYNC_JDOS_MEMBERS, DateUtil.getCurrentDate());
            if(!toRunCleanLog){
                return;
            }
            log.info("jdos.begin_sync_app_members");
            // japiHttpDataImporter.initJApiApp(false);
            appInfoService.syncJdosMembers();
        }catch (Exception e){
            log.error("jdos.err_sync_api_data");
        }

    }

    @Scheduled(cron = "${es.update_es_index:0 3 1 * * ?}")
    public void updateEsIndex(){
        try{
            boolean toRunCleanLog = jobLockService.getLock(LockTypeEnum.UPDATE_ES_INDEX, DateUtil.getCurrentDate());
            if(!toRunCleanLog){
                return;
            }
            log.info("es.rebuild_all_es_index");
            // japiHttpDataImporter.initJApiApp(false);
            esInterfaceService.initAllAppIndex();
        }catch (Exception e){
            log.error("jdos.err_sync_api_data");
        }

    }

    @Scheduled(cron = "${rank.sync_rank_data:0 3 0 * * ?}")
    public void syncRankData(){
        try{
            boolean toRunCleanLog = jobLockService.getLock(LockTypeEnum.UPDATE_RANK, DateUtil.getCurrentDate());
            if(!toRunCleanLog){
                return;
            }
            log.info("rank.update_rank_data");
            // japiHttpDataImporter.initJApiApp(false);
            scoreManageService.initAllAppScores();
            rankScoreService.updateRankScores();;
        }catch (Exception e){
            log.error("jdos.err_sync_api_data");
        }

    }

    @Scheduled(cron = "${jdos.sync_interface_prop:0 2 0 1/1 * ? }")
    public void syncInterfaceProp() {
        try {
            boolean toRunCleanLog = jobLockService.getLock(LockTypeEnum.SYNC_INTERFACE_PROP, DateUtil.getCurrentDate());
            if (!toRunCleanLog) {
                return;
            }
            log.info("jdos.sync_interface_prop");

            methodManageService.initAllMethodProps();
        } catch (Exception e) {
            log.error("jdos.sync_interface_prop", e);
        }

    }
}

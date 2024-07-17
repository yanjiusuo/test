package com.jd.workflow.console.service.doc.importer;

import com.jd.workflow.console.dto.importer.ImportDto;
import com.jd.workflow.console.dto.importer.JddjApp;
import com.jd.workflow.console.entity.sync.DataSyncRecord;
import com.jd.workflow.console.service.sync.AppType;
import com.jd.workflow.console.service.sync.DataSyncRecordService;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class JddjDataImportTask implements Runnable{
    private JddjApp jddjApp;
    JddjApiImporter importer;
    String cookie;
    ImportDto dto;
    AppType appType;
    DataSyncRecordService dataSyncService ;
    @Override
    public void run() {
        DataSyncRecord record = new DataSyncRecord();
        try{

            record.setSourceAppCode(dto.getDjAppCode());
            record.setSourceGroup(dto.getDjApiGroup());
            record.setSourceEnv(dto.getDjEnv());
            record.setSource("jddj");
            record.setTargetAppCode(dto.getTargetAppCode());
            long start = System.currentTimeMillis();
            Long latestVersion = importer.importDjApp(cookie, dto, appType);
            if(latestVersion == null) {
                return;
            }
            record.setLastSyncVersion(latestVersion+"");
            record.setSuccess(1);
            record.setTotalCost(Variant.valueOf(System.currentTimeMillis() - start).toInt());
            dataSyncService.save(record);
        }catch (Exception e){
            record.setSuccess(0);
            record.setErrorInfo(e.getMessage());
            log.error("jddj.err_import_data:app={}",
                    JsonUtils.toJSONString(jddjApp),e);
            dataSyncService.save(record);
        }

    }
}

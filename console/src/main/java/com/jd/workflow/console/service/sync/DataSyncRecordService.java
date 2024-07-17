package com.jd.workflow.console.service.sync;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dao.mapper.AppInfoMapper;
import com.jd.workflow.console.dao.mapper.sync.DataSyncRecordMapper;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.sync.DataSyncRecord;
import com.jd.workflow.console.service.IAppInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSyncRecordService extends ServiceImpl<DataSyncRecordMapper, DataSyncRecord> {
    public DataSyncRecord getLatestSyncRecord(String sourceAppCode,String sourceEnv,String sourceGroup){
        LambdaQueryWrapper<DataSyncRecord> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DataSyncRecord::getSourceAppCode,sourceAppCode);
        lqw.eq(DataSyncRecord::getSuccess,1);
        lqw.eq(DataSyncRecord::getSourceEnv,sourceEnv);
        lqw.eq(StringUtils.isNotBlank(sourceGroup),DataSyncRecord::getSourceGroup,sourceGroup);
        lqw.isNull(StringUtils.isBlank(sourceGroup),DataSyncRecord::getSourceGroup);
        lqw.orderByDesc(DataSyncRecord::getId);
        lqw.last("LIMIT 1");
         List<DataSyncRecord> list = list(lqw);
         if(!list.isEmpty()) return list.get(0);
         return null;
    }
    public void clearSyncRecord(String source){
        LambdaQueryWrapper<DataSyncRecord> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DataSyncRecord::getSource,source);
        remove(lqw);
    }
}

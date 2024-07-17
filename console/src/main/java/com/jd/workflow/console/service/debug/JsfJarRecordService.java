package com.jd.workflow.console.service.debug;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.IpUtil;
import com.jd.workflow.console.dao.mapper.debug.JsfJarRecordMapper;
import com.jd.workflow.console.entity.debug.JsfJarRecord;
import com.jd.workflow.console.entity.debug.dto.JarLoadStatus;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@Service
public class JsfJarRecordService extends ServiceImpl<JsfJarRecordMapper, JsfJarRecord> {
    public JsfJarRecord getJsfRecord(MavenJarLocation location){
        LambdaQueryWrapper<JsfJarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JsfJarRecord::getGroupId, location.getGroupId());
        queryWrapper.eq(JsfJarRecord::getArtifactId, location.getArtifactId());
        queryWrapper.eq(JsfJarRecord::getVersion, location.getVersion());
        queryWrapper.eq(JsfJarRecord::getYn, 1);
        return getOne(queryWrapper);
    }
    public List<JsfJarRecord> listAllLocalRecords() {
       return listByIp(IpUtil.getLocalIp());
    }
    public List<JsfJarRecord> listByIp(String ip) {
        LambdaQueryWrapper<JsfJarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JsfJarRecord::getYn, 1);

        queryWrapper.eq(JsfJarRecord::getCurrentCachedServerIp, ip);
        return list(queryWrapper);
    }
    public void saveJarRecord(MavenJarLocation location,String ossUrl,String actualJarVersion) {
        JsfJarRecord record = new JsfJarRecord();
        record.setGroupId(location.getGroupId());
        record.setArtifactId(location.getArtifactId());
        record.setVersion(location.getVersion());
        record.setLastUpdatedTime(new Date());
        record.setYn(1);
        record.setJarActualVersion(actualJarVersion);
        record.setOssDownloadUrl(ossUrl);
        record.setCurrentCachedServerIp(IpUtil.getLocalIp());
        save(record);
    }
    public List<JsfJarRecord> getRecords(String groupId, String artifactId) {
        LambdaQueryWrapper<JsfJarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JsfJarRecord::getGroupId, groupId);
        queryWrapper.eq(JsfJarRecord::getArtifactId, artifactId);
        queryWrapper.eq(JsfJarRecord::getYn, 1);
        return list(queryWrapper);
    }
    public List<JsfJarRecord> getNonFailRecords(String groupId, String artifactId) {
        LambdaQueryWrapper<JsfJarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JsfJarRecord::getGroupId, groupId);
        queryWrapper.eq(JsfJarRecord::getArtifactId, artifactId);
        queryWrapper.eq(JsfJarRecord::getYn, 1);
        queryWrapper.ne(JsfJarRecord::getLoadStatus, JarLoadStatus.LOAD_FAIL.getCode());
        return list(queryWrapper);
    }
}

package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.RouteConfigRecordMapper;
import com.jd.workflow.console.dto.PublishRecordDto;
import com.jd.workflow.console.entity.RouteConfigRecord;
import com.jd.workflow.soap.common.lang.Guard;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RouteConfigRecordService extends ServiceImpl<RouteConfigRecordMapper, RouteConfigRecord> {
    public List<RouteConfigRecord> getAllRecord(){
        LambdaQueryWrapper<RouteConfigRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(RouteConfigRecord.class,vs->{
            return !"config".equals(vs.getProperty());
        });

        List<RouteConfigRecord> records = list(queryWrapper);
        return records;
    }
    public void publish(PublishRecordDto dto){
        Guard.notEmpty(dto,"发布配置不可为空");
        Guard.notEmpty(dto.getConfig(),"发布配置不可为空");
        Guard.notEmpty(dto.getMethodId(),"发布方法id不可为空");
        Guard.notEmpty(dto.getPublishVersion(),"发布版本不可为空");
        LambdaQueryWrapper<RouteConfigRecord> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(RouteConfigRecord::getMethodId,dto.getMethodId());
        RouteConfigRecord record = getOne(queryWrapper);
        if(record == null){
            record = new RouteConfigRecord();
            record.setConfig(dto);
            record.setVersion(0L);
            record.setMethodId(dto.getMethodId());
            save(record);
        }else{
            record.setVersion(record.getVersion()+1);
            record.setConfig(dto);
            updateById(record);
        }
    }
    public void unpublish(Long id){
        removeById(id);
    }

}

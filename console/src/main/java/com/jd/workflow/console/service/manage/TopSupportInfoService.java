package com.jd.workflow.console.service.manage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.manage.TopSupportInfoMapper;
import com.jd.workflow.console.entity.manage.TopSupportInfo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
public class TopSupportInfoService extends ServiceImpl<TopSupportInfoMapper, TopSupportInfo> {

    public void cancelTop(Integer type,Long relatedId){
        String operator = UserSessionLocal.getUser().getUserId();
        LambdaQueryWrapper<TopSupportInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TopSupportInfo::getRelatedId,relatedId);
        lqw.eq(TopSupportInfo::getType,type);
        lqw.eq(TopSupportInfo::getOperator,operator);
        remove(lqw);
    }
    public List<Long> getTopIds(List<Long> ids,Integer type){
        if(ids == null || ids.isEmpty()){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<TopSupportInfo> lqw = new LambdaQueryWrapper<>();
        lqw.in(TopSupportInfo::getRelatedId,ids);
        lqw.eq(TopSupportInfo::getType,type);
        lqw.orderByDesc(TopSupportInfo::getCurrentTime);
        lqw.eq(TopSupportInfo::getOperator,UserSessionLocal.getUser().getUserId());
        List<TopSupportInfo> list = list(lqw);

        return list.stream().map(TopSupportInfo::getRelatedId).collect(java.util.stream.Collectors.toList());
    }

    public void top(Integer type,Long relatedId){
        String operator = UserSessionLocal.getUser().getUserId();
        TopSupportInfo topInfo = getTopInfoByRelatedIdAndType(relatedId, type);
        if(topInfo == null){
            topInfo = new TopSupportInfo();
            topInfo.setCurrentTime(System.currentTimeMillis());
            topInfo.setOperator(operator);
            topInfo.setRelatedId(relatedId);
            topInfo.setType(type);
            save(topInfo);
        }else{
            topInfo.setCurrentTime(System.currentTimeMillis());
        }
    }
    public TopSupportInfo getTopInfoByRelatedIdAndType(Long relatedId,Integer type){
        LambdaQueryWrapper<TopSupportInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TopSupportInfo::getRelatedId,relatedId);
        lqw.eq(TopSupportInfo::getType,type);
        lqw.eq(TopSupportInfo::getOperator,UserSessionLocal.getUser().getUserId());
        return getOne(lqw);
    }
}

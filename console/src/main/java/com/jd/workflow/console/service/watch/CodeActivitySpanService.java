package com.jd.workflow.console.service.watch;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.watch.CodeActivitySpanMapper;
import com.jd.workflow.console.entity.watch.CodeActivity;
import com.jd.workflow.console.entity.watch.CodeActivitySpan;
import com.jd.workflow.console.entity.watch.dto.CodeActivitySpanDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Service
public class CodeActivitySpanService extends ServiceImpl<CodeActivitySpanMapper, CodeActivitySpan> {
    @Value("${code.maxKeyAllowInterval:900000}")
    private int afterKeyMaxAllowInterval = 15 * 60 * 1000;
    private int getDay(Timestamp timestamp){
        return timestamp.toLocalDateTime().getDayOfYear();
    }

    public  List<CodeActivitySpanDto> computeSpans(List<CodeActivity> activities, CodeActivity lastActivity) {
        if (activities.isEmpty()) return Collections.emptyList();
        if (lastActivity == null || getDay(lastActivity.getTime()) != getDay(activities.get(0).getTime()) ) {//不可跨天
            lastActivity = activities.get(0);
        }
        List<CodeActivitySpanDto> spans = new ArrayList<>();
        CodeActivitySpanDto lastSpan = new CodeActivitySpanDto();
        lastSpan.setStartTime(lastActivity.getTime());
        int i = 0;
        for (CodeActivity activity : activities) {
            if(activity.getTime().getTime() - lastActivity.getTime().getTime() > afterKeyMaxAllowInterval
                    || i == activities.size() - 1
            ){
                if(activity.getTime().getTime() - lastActivity.getTime().getTime() > afterKeyMaxAllowInterval){
                    lastSpan.setEndTime(lastActivity.getTime());
                }else{
                    lastSpan.setEndTime(activity.getTime());
                }


                lastSpan.setCostTime(lastSpan.getEndTime().getTime() - lastSpan.getStartTime().getTime());
                if(lastSpan.getCostTime() > 0L){
                    spans.add(lastSpan);
                }


                lastSpan = new CodeActivitySpanDto();
                lastSpan.setStartTime(activity.getTime());
            }

            lastActivity = activity;
            i++;
        }


        return spans;
    }
    /**
     * 合并2个时间段的时间线
     * @param existSpans
     * @param newSpans
     */
    public void mergeSpan(List<CodeActivitySpanDto> existSpans,List<CodeActivitySpanDto> newSpans){
        if(newSpans.isEmpty()) return;
        if(existSpans.isEmpty()){
            existSpans.addAll(newSpans);
            return;
        }
        CodeActivitySpanDto lastSpan = existSpans.get(existSpans.size() - 1);
        CodeActivitySpanDto firstSpan = newSpans.get(0);
        if(firstSpan.getStartTime().getTime() - lastSpan.getEndTime().getTime() >= afterKeyMaxAllowInterval) {
            existSpans.addAll(newSpans);
            return;
        }
        lastSpan.setEndTime(firstSpan.getEndTime());
        lastSpan.setCostTime(lastSpan.getCostTime() + firstSpan.getCostTime());
        existSpans.addAll(newSpans.subList(1, newSpans.size()));
    }

}

package com.jd.workflow.console.entity.watch.dto;

import com.jd.workflow.console.entity.watch.CodeActivitySpan;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;

import java.sql.Timestamp;
@Data
public class CodeActivitySpanDto {
    /**
     * 起始时间
     */
    private Timestamp startTime;
    /**
     * 结束时间
     */
    private Timestamp endTime;
    /**
     * 耗费时间
     */
    private Long costTime;

    public  CodeActivitySpan toSpan(String erp){
        CodeActivitySpan span = new CodeActivitySpan();
        span.setErp(erp);
        span.setStatisticDay(StringHelper.formatDate(startTime,"yyyy-MM-dd"));
        span.setStartTime(startTime);
        span.setEndTime(endTime);
        span.setCostTime(costTime);
        return span;
    }
}

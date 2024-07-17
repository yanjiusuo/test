package com.jd.workflow.metrics;

import com.jd.workflow.metrics.stat.CounterStatResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MetricRange {
    private long start;//时间戳毫秒
    // 毫秒
    private long duration;//时间戳毫秒
    private Integer step;// 毫秒
    public List<Long> getSteps(){
        List<Long> result = new ArrayList<>();
        for (int i = 0; i <= getDuration() / getStep(); i++) {

            result.add(getStart() + getStep()*i);
        }
        return result;
    }

}

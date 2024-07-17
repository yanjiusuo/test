package com.jd.workflow.metrics.stat;

import lombok.Data;

@Data
public class CounterStatResult extends BaseMetricStat {

    int total;
    int success;
    int error;
    public double getAvailability(){
        return error*1.0 / total;
    }
}

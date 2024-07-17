package com.jd.workflow.metrics.stat;

import lombok.Data;

@Data
public class HistogramStatResult extends BaseMetricStat {

    Double min;
    Double max;
    Double tp50;
    Double tp90;
    Double tp99;
    Double tp999;
    //Double avg;
}

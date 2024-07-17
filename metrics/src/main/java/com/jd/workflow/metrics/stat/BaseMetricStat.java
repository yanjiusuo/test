package com.jd.workflow.metrics.stat;

import lombok.Data;

@Data
public abstract class BaseMetricStat {
    long timestamp;
    long version;
    String serverIp;
}

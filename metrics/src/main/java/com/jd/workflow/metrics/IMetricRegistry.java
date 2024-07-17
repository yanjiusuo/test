package com.jd.workflow.metrics;

import com.jd.workflow.metrics.alarm.IAlarmManager;
import com.jd.workflow.metrics.counter.ICounter;
import com.jd.workflow.metrics.stat.IMetricStatManager;
import com.jd.workflow.metrics.summary.ISummary;

public interface IMetricRegistry {
    // 计数器
    public ICounter newCounter(MetricId id);

    // 直方图
    public ISummary newSummary(MetricId id);
    // 获取统计信息
    public IMetricStatManager getStatManager();
    // 告警管理
    public IAlarmManager getAlarmManager();
}

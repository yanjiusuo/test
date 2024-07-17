package com.jd.workflow.metrics.stat;

import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.MetricRange;
import com.jd.workflow.metrics.stat.CounterStatResult;
import com.jd.workflow.metrics.stat.HistogramStatResult;

import java.util.List;

public interface IMetricStatManager {
    public List<CounterStatResult> queryCounter(MetricId metricId, MetricRange metricRange);
    public List<HistogramStatResult> queryTp(MetricId metricId, MetricRange metricRange);
}

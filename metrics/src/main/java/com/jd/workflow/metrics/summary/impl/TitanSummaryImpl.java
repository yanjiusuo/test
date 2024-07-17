package com.jd.workflow.metrics.summary.impl;

import com.jd.mlaas.titan.profiler.sdk.TitanProfilerContext;
import com.jd.mlaas.titan.profiler.sdk.metric.*;
import com.jd.workflow.metrics.Label;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.summary.ISummary;
import com.jd.workflow.metrics.summary.Watcher;

import java.util.List;

public class TitanSummaryImpl implements ISummary {
    private TitanProfilerContext profilerContext;
    TitanMetricPlan<TitanHistogram> histogram ;

    public TitanSummaryImpl(MetricId metricId,TitanProfilerContext profilerContext){
        TitanMetricBuilder.HistogramBuilder histogramBuilder = profilerContext.metricRegistry().histogram(metricId.getId());
        List<Label> labels = metricId.labelWithValues();
        for (Label label : labels) {
            histogramBuilder.labels(TitanMetricLabel.of(label.getKey(),label.getValue()));
        }
        this.histogram = histogramBuilder
                //静态 label
                //启用动态 label，定义好有哪些 label
                .labelKeys(metricId.labelKeys())
                .build();

    }

    @Override
    public Watcher newWatcher(String... labelValues) {
        TitanMethodExecutionWatcher watcher = histogram.ofLabelValues(labelValues).watcher();
        return new Watcher() {
            @Override
            public void fault() {
                watcher.fault();
            }

            @Override
            public void close() {
                watcher.close();
            }
        };
    }
}

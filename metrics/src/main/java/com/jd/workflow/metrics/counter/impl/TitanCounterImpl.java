package com.jd.workflow.metrics.counter.impl;

import com.jd.mlaas.titan.profiler.sdk.TitanProfilerContext;
import com.jd.mlaas.titan.profiler.sdk.metric.TitanCounter;
import com.jd.mlaas.titan.profiler.sdk.metric.TitanMetricBuilder;
import com.jd.mlaas.titan.profiler.sdk.metric.TitanMetricLabel;
import com.jd.mlaas.titan.profiler.sdk.metric.TitanMetricPlan;
import com.jd.workflow.metrics.counter.ICounter;
import com.jd.workflow.metrics.Label;

import java.util.List;

public class TitanCounterImpl implements ICounter {

    TitanMetricPlan<TitanCounter> counter;
    public TitanCounterImpl(TitanProfilerContext profilerContext, String name, List<Label> labels,String... labelKeys) {

        TitanMetricBuilder.CounterBuilder counterBuilder = profilerContext.metricRegistry().counter(name);
        for (Label label : labels) {
            counterBuilder.labels(TitanMetricLabel.of(label.getKey(), label.getValue()));
        }
        this.counter = counterBuilder
                //启用动态 label，定义好有哪些 label
                .labelKeys(labelKeys)
                .build();
    }



    @Override
    public void inc(long size,String... labelValues) {
        this.counter.ofLabelValues(labelValues).increment(size);
    }
}

package com.jd.workflow.metrics.counter.impl;

import com.jd.workflow.metrics.Label;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.counter.ICounter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

import java.util.ArrayList;
import java.util.List;

public class PrometheusCounterImpl implements ICounter {
    static CollectorRegistry registry = new CollectorRegistry();
    List<String> fixedLabelValues ;
     Counter counter;
    public PrometheusCounterImpl(MetricId metricId,CollectorRegistry registry){
        List<String> fixedLabelValues=new ArrayList<>();
        List<String> labelKeys = new ArrayList<>();
        for (Label labelWithValue : metricId.labelWithValues()) {
            fixedLabelValues.add(labelWithValue.getValue());
            labelKeys.add(labelWithValue.getKey());
        }
        this.fixedLabelValues = fixedLabelValues;
        for (String s : metricId.labelKeys()) {
            labelKeys.add(s);
        }

        counter  = Counter.build()
                .name(metricId.getId()).labelNames(labelKeys.toArray(new String[0])).register(registry);
    }


    @Override
    public void inc(long size, String... labelValues) {
        List<String> values = new ArrayList<>(fixedLabelValues);
        for (String labelValue : labelValues) {
            values.add(labelValue);
        }
        counter.labels(values.toArray(new String[0])).inc(size);
    }
}

package com.jd.workflow.metrics.summary.impl;

import com.jd.workflow.metrics.Label;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.summary.ISummary;
import com.jd.workflow.metrics.summary.Watcher;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrometheusSummaryImpl implements ISummary {
    Histogram histogram ;
    public static final String SUCCESS_LABEL_NAME = "is_success";
    public static final String RESULT_TRUE = "1";
    public static final String RESULT_FALSE = "0";

    List<String> fixedLabelValues;
    public PrometheusSummaryImpl(MetricId metricId, CollectorRegistry registry){
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
        histogram =  Histogram.build()
                .name(metricId.getId()).register(registry);
    }
    @Override
    public Watcher newWatcher(String... labelValues) {
        List<String> values = new ArrayList<String>(fixedLabelValues);//labelValues
        for (String labelValue : labelValues) {
            values.add(labelValue);
        }
        boolean[] isSuccess = new boolean[]{true};
        final long start = System.nanoTime();
        return new Watcher() {
            @Override
            public void fault() {
                isSuccess[0] = false;
            }

            @Override
            public void close() {
                if(isSuccess[0]){
                    values.add(RESULT_TRUE);
                }else {
                    values.add(RESULT_FALSE);
                }
                long duration = System.nanoTime() - start;
                histogram.labels(values.toArray(new String[0])).observe(duration);
            }
        };
    }
}

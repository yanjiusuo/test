package com.jd.workflow.metrics.registry;

import com.jd.mlaas.titan.profiler.sdk.TitanProfilerContext;
import com.jd.mlaas.titan.profiler.sdk.metric.impl.TitanStaticMetricLabelSuppliers;
import com.jd.workflow.metrics.IMetricRegistry;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.alarm.IAlarmManager;
import com.jd.workflow.metrics.alarm.TitanAlarmManager;
import com.jd.workflow.metrics.client.TitanRequestClient;
import com.jd.workflow.metrics.counter.ICounter;
import com.jd.workflow.metrics.stat.IMetricStatManager;
import com.jd.workflow.metrics.stat.TitanStatManager;
import com.jd.workflow.metrics.summary.ISummary;
import com.jd.workflow.metrics.summary.impl.TitanSummaryImpl;




public class TitanRegistry implements IMetricRegistry {
    TitanRequestClient titanRequestClient;

    TitanAlarmManager alarmManager;

    TitanStatManager titanStatManager;
    TitanProfilerContext profilerContext;
    public void setTitanRequestClient(TitanRequestClient titanRequestClient) {
        this.titanRequestClient = titanRequestClient;
    }

    public void init(){
        profilerContext = TitanProfilerContext.builder("demo")
                // 全局静态 label
                .staticMetricLabelSupplier(TitanStaticMetricLabelSuppliers.ipLabel("client"))
                .build();
        titanStatManager = new TitanStatManager();
        titanStatManager.setClient(titanRequestClient);

        alarmManager = new TitanAlarmManager();
        alarmManager.setClient(titanRequestClient);

        // 初始化
        profilerContext.initialize();
    }
    @Override
    public ICounter newCounter(MetricId id) {
        return null;
    }

    @Override
    public ISummary newSummary(MetricId id) {
        return new TitanSummaryImpl(id,profilerContext);
    }

    @Override
    public IMetricStatManager getStatManager() {
        return titanStatManager;
    }

    @Override
    public IAlarmManager getAlarmManager() {
        return alarmManager;
    }
}

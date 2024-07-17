package com.jd.workflow.metrics.summary;

import com.jd.mlaas.titan.profiler.sdk.metric.impl.SimpleTitanHistogram;

import java.util.concurrent.TimeUnit;

public interface Watcher {
    public void fault();

    public void close() ;
}

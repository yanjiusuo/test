package com.jd.workflow.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class MicroMeterTests {
    public static void main(String[] args) {
        MeterRegistry registry = new SimpleMeterRegistry();
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        Counter counter = registry.counter("wjf", "a");



        counter.increment();
        DistributionSummary summary = registry.summary("2133", "");


    }
}

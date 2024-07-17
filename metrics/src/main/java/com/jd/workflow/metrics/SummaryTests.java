package com.jd.workflow.metrics;

import io.prometheus.client.Summary;

/**
 * 统计数量或者统一延迟
 */
public class SummaryTests {
    private static final Summary requestLatency = Summary.build()
            .name("requests_latency_seconds")
            .help("request latency in seconds")
            .register();

    private static final Summary receivedBytes = Summary.build()
            .name("requests_size_bytes")
            .help("request size in bytes")
            .register();

    Summary requestLatency12 = Summary.build()
            .name("requests_latency_seconds")
            .help("Request latency in seconds.")
            .maxAgeSeconds(10 * 60)
            .ageBuckets(5)
            // ...
            .register();
    public void processRequest(long size) {
        Summary.Timer requestTimer = requestLatency.startTimer();
        try {
            // Your code here.

        } finally {
            requestTimer.observeDuration();
            receivedBytes.observe(size);
        }
    }
}

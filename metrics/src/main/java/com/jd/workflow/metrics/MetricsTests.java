package com.jd.workflow.metrics;

import io.micrometer.core.ipc.http.HttpSender;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;


import java.io.IOException;
import java.util.List;

/**
 * 自定义java exporter https://yunlzheng.gitbook.io/prometheus-book/part-ii-prometheus-jin-jie/exporter/custom_exporter_with_java/client_library_java
 */
public class MetricsTests {
    static CollectorRegistry registry = new CollectorRegistry();
    //static final CustomCollector requests1 = new CustomCollector().register(registry);
    static final Counter requests = Counter.build()
            .name("test_request_total").labelNames("requestMethod").help("Total requests.").register(registry);

    static final Histogram requestLatency = Histogram.build()
            .name("requests_latency_seconds").help("Request latency in seconds.").register(registry);

    static void processHistogramRequest() {
        Histogram.Timer requestTimer = requestLatency.startTimer();
        try {
            // Your code here.
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            requestTimer.observeDuration();
        }
    }
    static void processCounterRequest(){
        try{
            for (int i = 0; i < 100; i++) {
                requests.labels("post").inc();
            }

            List<Collector.MetricFamilySamples> result = requests.collect();
            System.out.println(result);
        }finally {
          /*  PushGateway pg = new PushGateway("127.0.0.1:9091");
            pg.pushAdd(registry, "my_batch_job");*/
        }
    }
    public static void main(String[] args) throws IOException {
        try{
            for (int i = 0; i < 100; i++) {
                processHistogramRequest();
            }
            System.out.println(requestLatency.collect());
            System.out.println(requestLatency.collect());


        }finally {
          /*  PushGateway pg = new PushGateway("127.0.0.1:9091");
            pg.pushAdd(registry, "my_batch_job");*/
        }

    }
}

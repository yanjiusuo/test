//package com.jd.workflow.metrics;
//
//import io.prometheus.client.Collector;
//import io.prometheus.client.GaugeMetricFamily;
//import io.prometheus.client.exporter.HTTPServer;
//import io.prometheus.client.hotspot.DefaultExports;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class CustomCollector   extends Collector {
//    public List<MetricFamilySamples> collect() {
//        //System.out.println(collect());
//        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
//        // With no labels.
//        mfs.add(new GaugeMetricFamily("my_gauge", "help", 42));
//        // With labels
//        GaugeMetricFamily labeledGauge = new GaugeMetricFamily("my_other_gauge", "help", Arrays.asList("labelname"));
//        labeledGauge.addMetric(Arrays.asList("foo"), 4);
//        labeledGauge.addMetric(Arrays.asList("bar"), 5);
//        mfs.add(labeledGauge);
//        return mfs;
//    }
//    /* // Registration
//    static final YourCustomCollector requests = new YourCustomCollector().register() */
//    public static void main(String[] args) throws IOException {
//        DefaultExports.initialize();
//        HTTPServer server = new HTTPServer(1234);
//    }
//}
//
//

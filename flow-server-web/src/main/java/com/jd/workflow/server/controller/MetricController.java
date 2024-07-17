package com.jd.workflow.server.controller;

import com.jd.mlaas.titan.profiler.sdk.TitanProfilerContext;
import com.jd.mlaas.titan.profiler.sdk.metric.*;
import com.jd.mlaas.titan.profiler.sdk.metric.impl.TitanStaticMetricLabelSuppliers;
import com.jd.workflow.soap.common.exception.BizException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

import io.micrometer.core.instrument.Timer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@Slf4j
@RequestMapping("/metrics")
public class MetricController {
    //@Autowired
    CollectorRegistry registry;
 /*   private Counter counter;
    private Counter failCounter;*/
    Summary costTimeSummary;
   /* Timer timer = null;*/
   Histogram requestLatency ;
    private TitanProfilerContext profilerContext;
    TitanMetricPlan<TitanCounter> counterPlan;
    TitanMetricPlan<TitanHistogram> delayStaticPlan;

    @PostConstruct
    private void init(){
        profilerContext = TitanProfilerContext.builder("demo")
                // 全局静态 label
                .staticMetricLabelSupplier(TitanStaticMetricLabelSuppliers.ipLabel("client"))
                .build();


        // 初始化
        profilerContext.initialize();

      /*  failCounter=  registry.counter("requests_add_fail_total","save","carson");
           timer = registry.timer("requests_cost_timer");
        counter = registry.counter("requests_add_total","save","carson");*/
        //Histogram.build().

        /*requestLatency= Histogram.build()
                .name("requests_latency_mill_seconds").labelNames("random","is_success").help("Request latency in seconds.").register(registry);


        costTimeSummary =     Summary.build()
                .name("requests_latency_seconds1")
                 .labelNames("random","is_success")
                .help("Request latency in seconds.")
                .maxAgeSeconds(1 * 60)
                 .quantile(0.0,0)
                 .quantile(0.5,0)
                 .quantile(0.9,0)
                 .quantile(0.99,0)
                 .quantile(0.999,0)
                 .quantile(1,0)
                .ageBuckets(5)

                .register(registry);*/

        counterPlan = profilerContext.metricRegistry().counter("wjf_stat_reqz")
                //静态 label
                .labels(TitanMetricLabel.of("client_id", "1"))
                .labels(TitanMetricLabel.of("cluster", "jim://3044266746474140587/6683"))
                //启用动态 label，定义好有哪些 label
                .labelKeys("server_ip")
                .build();


        delayStaticPlan = profilerContext.metricRegistry().histogram("wjf_stat_delayz")
                //静态 label
                .labels(TitanMetricLabel.of("client_id", "1"))
                .labels(TitanMetricLabel.of("cluster", "jim://3044266746474140587/6683"))
                //启用动态 label，定义好有哪些 label
                .labelKeys("server")
                .build();

       /* costTimeSummary.record(1); 监控分布情况
        costTimeSummary.record(1.3);
        costTimeSummary.record(2.4);
        costTimeSummary.record(3.5);
        costTimeSummary.record(4.1);*/

    }
    AtomicInteger intVar = new AtomicInteger(1);

    @RequestMapping(value = "/add",method = RequestMethod.GET)
    @ResponseBody
    public String add(@Validated String firstName, @Validated String secondName) throws Exception {

        long start = System.currentTimeMillis();
        TitanMethodExecutionWatcher watcher = delayStaticPlan.ofLabelValues("10.10.10.11:6483").watcher();
        try{
            if("error".equals(firstName)){
                throw new BizException("无效d1yic1");
            }
            Random random = new Random();
            counterPlan.ofLabelValues("123").increment();
            //counterPlan.ofLabelValues("10.10.10.10:6473").increment(10);
            String name = firstName+secondName;

            int sleepTime = random.nextInt(10)+2;

            Thread.sleep(sleepTime);
            long cost = System.currentTimeMillis() - start;
            if(intVar.addAndGet(1)%2==0){
                costTimeSummary.labels("a","1").observe(cost);
                requestLatency.labels("a","1").observe(cost/1000.0);
            }else{
                costTimeSummary.labels("b","1").observe(cost);
                requestLatency.labels("b","1").observe(cost/1000.0);
            }




            return name;
        }catch (Exception e){
        watcher.fault();;
            long cost = System.currentTimeMillis() - start;
            costTimeSummary.labels("c","0").observe(cost/1000.0);
            requestLatency.labels("b","0").observe(cost/1000.0);
            //failCounter.increment();
            throw new Exception("异常");
        }finally {
            watcher.close();
        }
    }

}

package com.jd.workflow.console;

import com.jd.mlaas.titan.profiler.sdk.TitanProfilerContext;
import com.jd.mlaas.titan.profiler.sdk.metric.*;
import com.jd.mlaas.titan.profiler.sdk.metric.impl.TitanStaticMetricLabelSuppliers;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StdCalendar;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
public class TestWatch extends BaseTestCase {

    private TitanProfilerContext profilerContext;

    @Before
    public void initialize() {
        // 构建一个 TitanProfilerContext, 一个进程中相同的 source 只需要构建一个 context 即可
        // 这里创建了一个 jimdb source
        profilerContext = TitanProfilerContext.builder("demo")
                // 全局静态 label
                .staticMetricLabelSupplier(TitanStaticMetricLabelSuppliers.ipLabel("client"))
                .build();


        // 初始化
        profilerContext.initialize();
    }

    @Test
    public void histogramDemo() {
        TitanMetricPlan<TitanHistogram> plan = profilerContext.metricRegistry().histogram("wjf_request_histogram1")
                //静态 label
                .labels(TitanMetricLabel.of("client_id", "1"))
                .labels(TitanMetricLabel.of("cluster", "jim://3044266746474140587/6683"))
                //启用动态 label，定义好有哪些 label
                .labelKeys("server")
                .build();

        //plan.ofLabelValues("10.10.10.10:6473").watch(watcher -> System.out.println("hello world"));
        System.out.println("---------------------------------------------");
        System.out.println(System.currentTimeMillis());
        TitanMethodExecutionWatcher watcher = plan.ofLabelValues("10.10.10.11:6483").watcher();

        try {
            Thread.sleep(1300);
        } catch (Exception e) {
            watcher.fault();
        } finally {
            watcher.close();
        }
       while (true);
    }

    @Test
    public void counterDemo() {
        Timer.Sample sample = Timer.start();
        TitanMetricPlan<TitanCounter> plan = profilerContext.metricRegistry().counter("wjf_stat_reqkjf")
                //静态 label
                .labels(TitanMetricLabel.of("client_id", "1"))
                .labels(TitanMetricLabel.of("cluster", "jim://3044266746474140587/6683"))
                //启用动态 label，定义好有哪些 label
                .labelKeys("server_ip")
                .build();


        plan.ofLabelValues("123").increment();
        plan.ofLabelValues("10.10.10.10:6473").increment(10);
        plan.ofLabelValues("10.10.10.10:6473").increment(91);
/*
        plan.ofLabelValues("10.10.10.10:6473").increment(10);
        plan.ofLabelValues("10.10.10.10:6474").increment(5);
        plan.ofLabelValues("10.10.10.10:6474").increment(5);
*/

        plan.ofLabelValues().increment();;
        while (true);
    }
    @Test
    public void testReport() throws URISyntaxException, IOException {
        String baseUri = "http://11.50.162.244";
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000)
                .build();
        HttpClientBuilder builder =HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
        CloseableHttpClient httpClient = builder.build();

        HttpPost httpPost = new HttpPost();
        httpPost.addHeader("Authorization","Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOnsibmFtZSI6ImRlbW8iLCJyb2xlcyI6WyJTT1VSQ0VfT1dORVI6ZGVtbyJdfSwiaWF0IjoxNjI5MzU5MDY5LCJpc3MiOiJ0aXRhbiJ9.Qylk4yn2Uni4twiylY3ufWdplnTlgh4VdO0p_6Oinp60-NksZ3U40w12y0oomcw1bTcPPw7zJmui2oi6L1Vn-Q");

        httpPost.setURI(new URI(baseUri+"/v1/metric-receive/demo/simple"));///v1/metric-receive/{source}/simple
        CloseableHttpResponse response = httpClient.execute(httpPost);
    }
    public String post(Object data,String url) throws IOException, URISyntaxException {
        String baseUri = "http://11.50.162.244";
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000)
                .build();
        HttpClientBuilder builder =HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
        CloseableHttpClient httpClient = builder.build();

        HttpPost httpPost = new HttpPost();
        httpPost.addHeader("content-Type", "application/json;charset=utf-8;");
        httpPost.setEntity(new StringEntity(JsonUtils.toJSONString(data)));
        httpPost.addHeader("Authorization","Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOnsibmFtZSI6ImRlbW8iLCJyb2xlcyI6WyJTT1VSQ0VfT1dORVI6ZGVtbyJdfSwiaWF0IjoxNjI5MzU5MDY5LCJpc3MiOiJ0aXRhbiJ9.Qylk4yn2Uni4twiylY3ufWdplnTlgh4VdO0p_6Oinp60-NksZ3U40w12y0oomcw1bTcPPw7zJmui2oi6L1Vn-Q");

        httpPost.setURI(new URI(baseUri+url));///v1/metric-receive/{source}/simple
        CloseableHttpResponse response = httpClient.execute(httpPost);
        String body = EntityUtils.toString(response.getEntity(),"utf-8");
        return body;
    }
    @Test
    public void testQuery() throws IOException, URISyntaxException {
        StdCalendar stdCalendar = new StdCalendar();
        Map<String,Object> queryData = new HashMap<>();
        Map<String,Object> dataSelect = new HashMap<>();
        dataSelect.put("name","wjf-test-demo");
        queryData.put("dataSelect",dataSelect);
        queryData.put("beginTimestamp",stdCalendar.toStartOfDay().getTimeInMillis());
        queryData.put("duration",24*60*60*1000);;
        String result = post(queryData, "/api/v1/metric-query/demo/range");
        log.info("range_result={}",result);
    }
    /*@Test
    public void gaugeDemo() {
        AtomicInteger counterForSimpleGauge = new AtomicInteger(0);
        AtomicInteger counterForMultiLabeledGauge = new AtomicInteger(0);
        profilerContext.metricRegistry().gauge("simpleGauge")
                .labels(TitanMetricLabel.of("GaugeType", "simple"))
                .gauge(counterForSimpleGauge::incrementAndGet)
                .build();

        profilerContext.metricRegistry().gauge("multiLabeledGauge")
                .labels(TitanMetricLabel.of("GaugeType", "multi"));
        gauge(() -> {
            Map<List<TitanMetricLabel>, Number> result = new HashMap<>();
            result.put(Arrays.asList(
                    TitanMetricLabel.of("TestKey1", "value1"),
                    TitanMetricLabel.of("test_key2", "value2")
                    ), counterForMultiLabeledGauge.incrementAndGet()
            );
            result.put(Arrays.asList(
                    TitanMetricLabel.of("key3", "value3"),
                    TitanMetricLabel.of("key4", "value4")
                    ), counterForMultiLabeledGauge.incrementAndGet()
            );
            return result;
        })
                .build();
    }*/
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}

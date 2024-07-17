package com.jd.workflow.processor;


import com.jd.workflow.BaseTestCase;
import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.core.processor.impl.HttpStepProcessor;
import com.jd.workflow.flow.core.processor.subflow.CamelSubflowProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class HttpBenchTests extends BaseTestCase {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    static WorkflowInput newWorkflowInput() {
        WorkflowInput input = new WorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        input.setParams(params);
        return input;
    }

    Step newStep() {
        StepContext stepContext = new StepContext();
        stepContext.setInput(newWorkflowInput());

        Step step = new Step();
        step.setContext(stepContext);
        return step;
    }

    private HttpStepProcessor newProcessor(String path) {
        HttpStepProcessor processor = new HttpStepProcessor();
        String httpDef = getResourceContent(path);
        HttpStepMetadata httpMetadata = (HttpStepMetadata) StepProcessorRegistry.parseMetadata(JsonUtils.parse(httpDef, Map.class));
        httpMetadata.init();
        processor.init(httpMetadata);
        return processor;
    }

    @Test
    public void testAssemble() throws ExecutionException, InterruptedException {
        WorkflowDefinition def =  WorkflowParser.parse(getResourceContent("classpath:http/http-task-body.json"));;


        CamelRouteLoader routeLoader = new CamelRouteLoader();
        try {
            CamelContext camelContext = routeLoader.buildCamelContext(def);

            camelContext.start();
            final ProducerTemplate template = camelContext.createProducerTemplate();
                WorkflowInput input = newWorkflowInput();


                Exchange exchange = new DefaultExchange(camelContext);
                StepContext stepContext = StepContextHelper.setInput(exchange, input); // 设置输入，返回执行上下文
                stepContext.setSubflowProcessor(new CamelSubflowProcessor(template));
                stepContext.setDebugMode(true);

                template.send("direct:start", exchange);// 执行代码

                Output output = (Output) exchange.getMessage().getBody();



            long start = System.currentTimeMillis();
            ScheduledExecutorService executorService = newExecutorService();

            List<Future> list = new ArrayList<>();

            for (int i = 0; i < 10000; i++) {
                Future future = executorService.submit(new Callable<Object>() {
                    @Override
                    public Object call() {
                        try{
                            Exchange exchange = new DefaultExchange(camelContext);
                            StepContext stepContext = StepContextHelper.setInput(exchange, input); // 设置输入，返回执行上下文
                            stepContext.setSubflowProcessor(new CamelSubflowProcessor(template));

                            template.send("direct:start", exchange);// 执行代码

                            Output output = (Output) exchange.getMessage().getBody();
                            return output;
                        }catch (Exception e){
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
                list.add(future);
            }
            for (int i = 0; i < list.size(); i++) {
                Object result = list.get(i).get();
                System.out.println(result);
            }

            System.out.println("cost::"+(System.currentTimeMillis() - start));
        } catch (Exception e) {

            throw new BizException("调试失败", e);
        }


    }
    @Test
    public void testBody() throws ExecutionException, InterruptedException {
        HttpStepProcessor processor = newProcessor("classpath:http/http-body.json");

        Step step = newStep();
        ScheduledExecutorService executorService = newExecutorService();


        long start = System.currentTimeMillis();

        List<Future> list = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try{
                        processor.process(step);
                        HttpOutput output = (HttpOutput) step.getOutput();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            list.add(future);
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).get();
        }
        System.out.println("cost::"+(System.currentTimeMillis() - start));
    }
    private HttpClient newClient(){
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        HttpClientBuilder builder =HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
        return builder.build();
    }
    ScheduledExecutorService newExecutorService(){
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(8);
        return executorService;
    }
    @Test
    public void testHttpReq() throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        HttpClient httpClient = newClient();
        String url = "http://127.0.0.1:6010/json";
        String data = "{\"id\":1,\"name\":\"wjf\"}";
        ScheduledExecutorService executorService = newExecutorService();


        long start = System.currentTimeMillis();

        List<Future> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            HttpPost post = new HttpPost();
            post.setURI(new URI(url));
            post.setEntity(new StringEntity(data,"utf-8"));
            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try{
                        HttpResponse result = httpClient.execute(post);
                        String ret = IOUtils.toString(result.getEntity().getContent());

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
           list.add(future);
           // System.out.println("retResult::"+ret);
        }
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i).get();
        }
        System.out.println("cost::"+(System.currentTimeMillis() - start));


    }

}

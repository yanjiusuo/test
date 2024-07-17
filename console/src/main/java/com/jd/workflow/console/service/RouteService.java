package com.jd.workflow.console.service;

import com.jd.workflow.console.dto.PublishRecordDto;
import com.jd.workflow.console.entity.RouteConfigRecord;
import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.expr.CustomLanguageResolver;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 路由服务，定期扫描表，获取更新的路由
 */
@Slf4j
@Service
public class RouteService {
    @Value("${route.scanInterval:10000}")
    private Integer scanInterval = 10*1000;

    Map<String/* methodId */,RouteItem> routes = new ConcurrentHashMap<>();
    @Autowired
    RouteConfigRecordService publishRecordService;
    @Value("${route.useDuccPublisher:true}")
    private boolean useDuccPublisher;

    @Autowired
    LogCollectService logCollectService;

    CamelRouteLoader routeLoader = new CamelRouteLoader();
    @PostConstruct
    public void init(){
        if(!useDuccPublisher){
            updateRoute();
            new Thread(new UpdateTask(),"update_route_task").start();
        }

    }
    void collectChangeRoutes(List<RouteConfigRecord> records, List<Long> addOrUpdateIds,List<String> removed){

        for (RouteConfigRecord record : records) {

            RouteItem routeItem = routes.get(record.getMethodId());
            if(routeItem == null){
                addOrUpdateIds.add(record.getId());
            }else if(routeItem.getVersionId() < record.getVersion()){
                addOrUpdateIds.add(record.getId());
            }
        }
        Set<String> set = records.stream().map(RouteConfigRecord::getMethodId).collect(Collectors.toSet());
        for (Map.Entry<String, RouteItem> entry : routes.entrySet()) {
            if(!set.contains(entry.getKey())){
                removed.add(entry.getKey());
            }
        }
    }
    public void updateRoute(){
        long start = System.currentTimeMillis();
        List<RouteConfigRecord> records = publishRecordService.getAllRecord();
        List<Long> addOrUpdateIds = new ArrayList<>();
        List<String> removedMethodIds = new ArrayList<>();
        collectChangeRoutes(records,addOrUpdateIds,removedMethodIds);
        if(!removedMethodIds.isEmpty()){
            log.info("route.remove_route:ids={}",removedMethodIds);

        }
        for (String id : removedMethodIds) {
            RouteItem item = routes.remove(id);
            try{
                item.getProducerTemplate().close();
            }catch (Exception e){
                log.error("logger.err_close_producer:id={}",id,e);
            }
        }
        if(!addOrUpdateIds.isEmpty()){
            log.info("route.update_route:ids={}",addOrUpdateIds);
            List<RouteConfigRecord> addOrUpdateRecords = publishRecordService.listByIds(addOrUpdateIds);
            for (RouteConfigRecord addOrUpdateRecord : addOrUpdateRecords) {
                RouteItem routeItem = buildRouteItemTemplate(addOrUpdateRecord);
                if(routeItem != null){
                    routes.put(addOrUpdateRecord.getMethodId(),routeItem);
                }
            }
        }


        long cost = System.currentTimeMillis() - start;
        log.info("route.update_route_cost:time={}",cost);
    }
    private RouteItem buildRouteItemTemplate(RouteConfigRecord record) {
        try{
            PublishRecordDto dto =  record.getConfig() ;

            DefaultCamelContext camelContext = routeLoader.buildCamelContext(dto.getConfig()) ;
            camelContext.start();
            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            RouteItem routeItem = new RouteItem();
            routeItem.setPublishVersion(record.getConfig().getPublishVersion());
            routeItem.setVersionId(record.getVersion());
            routeItem.setProducerTemplate(producerTemplate);
            return routeItem;
        }catch (Exception e){
            log.error("logger.err_build_producer_template:id={},config={}",record.getId(),record.getConfig(),e);
            return null;
        }

    }

    public HttpOutput route(String id,WorkflowInput input){
        if(useDuccPublisher) return null;
        try {
            RouteItem routeItem = routes.get(id);
            if(routeItem == null){
                return null;
            }
            ProducerTemplate producerTemplate = routeItem.getProducerTemplate();
            Exchange exchange = new DefaultExchange(producerTemplate.getCamelContext());
            StepContext stepContext  =  StepContextHelper.setInput(exchange,input); // 设置输入，返回执行上下文
            long start = System.currentTimeMillis();
            producerTemplate.send("direct:start",exchange);

            Map<String, Object> map = stepContext.toLog();
            logCollectService.addLog(map.get("exception") != null,id+"",map,routeItem.getPublishVersion());
            HttpOutput output = (HttpOutput) exchange.getMessage().getBody();
            log.info("execute_method_cost:time={}",System.currentTimeMillis()-start);
            return output;
        } catch (Exception e) {
            log.error("debug.err_build_route_context",e);
            throw new BizException("调试失败",e);
        }
    }





    public  class UpdateTask implements Runnable {

        @Override
        public void run() {
            while (true){
                sleep(RouteService.this.scanInterval);
                try{
                    RouteService.this.updateRoute();
                }catch (Exception e){
                    log.error("route.err_update_route",e);
                }

            }
        }
        protected void sleep(long timeout) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException var4) {
                Thread.currentThread().interrupt();
            }

        }
    }

    @Data
    public static class RouteItem{
        Long versionId;
        String publishVersion;
        ProducerTemplate producerTemplate;
    }
}

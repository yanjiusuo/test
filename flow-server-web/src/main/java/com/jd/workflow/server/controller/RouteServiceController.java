package com.jd.workflow.server.controller;



import com.jd.ump.profiler.CallerInfo;
import com.jd.ump.profiler.proxy.Profiler;
import com.jd.workflow.console.utils.HttpUtils;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.metrics.IMetricRegistry;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.summary.ISummary;
import com.jd.workflow.metrics.summary.Watcher;
import com.jd.workflow.server.service.CamelRouteServiice;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.NetUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
@RequestMapping("/api/routeService")
public class RouteServiceController {
    @Autowired
    IMetricRegistry registry;
    Map<String,ISummary> summary = new ConcurrentHashMap<>();
    @Autowired
    private CamelRouteServiice camelRouteServiice;

    String metricPrefix = "flow_interface_tp_";

    @PostConstruct
    public void init(){

    }
    ISummary newSummary(String id){
        return summary.computeIfAbsent(id,vs->{
            MetricId metricId = new MetricId();
            metricId.setId(id);
            metricId.labels("serverIp", NetUtils.getLocalHost());
            ISummary summary = registry.newSummary(metricId);
            return summary;
        });

    }

    @RequestMapping(value = "/**")
    public void execute(
                        @RequestHeader Map<String, Object> header,
                        @RequestParam Map<String, Object> params,

                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        String id = request.getRequestURI().substring((request.getContextPath()+"/api/routeService").length());
        if(id.startsWith("/") && StringHelper.countChar(id,'/') ==1){
            id = id.substring(1);
        }
        String umpKey = "api.route."+id;
        CallerInfo callerInfo = Profiler.registerInfo(umpKey, false, true);

        log.info("RouteServiceController#execute：" + id);
        //ISummary summary = newSummary(metricPrefix+id);
        //Watcher watcher = summary.newWatcher();
        try {

            WorkflowInput workflowInput = new WorkflowInput();

            workflowInput.addAllHeaders(header);
            workflowInput.setParams(params);

    
                String body = IOUtils.toString(request.getInputStream(), "utf-8");
                String contentType = request.getContentType();
                if(StringUtils.isNotBlank(body)){
                    if(contentType.contains("json")){
                        workflowInput.setBody(JsonUtils.parse( body));
                    }else if(contentType.contains("form")){
                        Map<String, Object> map = StringHelper.parseQuery(body, "utf-8");
                        workflowInput.setBody(map);
                    }else{
                        workflowInput.setBody(body);
                    }
                }

            HttpOutput httpOutput = camelRouteServiice.execute(id, workflowInput);


            log.info("RouteServiceController#execute return: " + JsonUtils.toJSONString(httpOutput));

            HttpUtils.sendResponse(httpOutput, response);

            if(!httpOutput.isSuccess()){
                Profiler.functionError(callerInfo);
            }else{
                Profiler.registerInfoEnd(callerInfo);
            }


        } catch (Exception e) {
            Profiler.functionError(callerInfo);
           // watcher.fault();
            log.error("RouteServiceController#execute errot: " + id, e);
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write("处理异常，请联系开发人员");
            response.getWriter().flush();
        }finally {

               // watcher.close();

        }
    }
}

package com.jd.workflow.console.controller;



import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.ConvertWebServiceBaseDto;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.console.service.IServiceConvertService;
import com.jd.workflow.console.service.RouteService;
import com.jd.workflow.console.utils.HttpUtils;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.legacy.SoapVersion;
import com.jd.workflow.soap.utils.WsdlUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：example
 * 类 名 称：ServiceConvertController
 * 类 描 述：服务转换
 * 创建时间：2022-05-26 20:56
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/routeService")
@UmpMonitor
@Api(hidden = true)
public class ServiceRouteController {

    @Autowired
    RouteService routeService;

    @RequestMapping(value = "/**")
    @ApiOperation(value = "执行请求", hidden = true)
    public void execute(
                        @RequestHeader Map<String, Object> header,
                        @RequestParam Map<String, Object> params,

                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

        String id = request.getRequestURI().substring((request.getContextPath()+"/api/routeService").length());

        log.info("RouteServiceController#execute：" + id);

        try {
            WorkflowInput workflowInput = new WorkflowInput();
            String cookie = request.getHeader("Cookie");

            workflowInput.addAllHeaders(header);
            if(!StringUtils.isEmpty(cookie)){
                workflowInput.addHeader("Cookie",cookie);
            }

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

            HttpOutput httpOutput = routeService.route(id, workflowInput);
            if(httpOutput != null){
                log.info("RouteServiceController#execute return: " + JsonUtils.toJSONString(httpOutput));
                HttpUtils.sendResponse(httpOutput, response);
               // log.info("execute_erq_cost:time={}",System.currentTimeMillis() - start);
                return;
            }

            response.sendError(404);


        } catch (Exception e) {
            log.error("RouteServiceController#execute errot: " + id, e);
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write("处理异常，请联系开发人员");
            response.getWriter().flush();
        }
    }

}

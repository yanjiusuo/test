package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.FlowImportDTO;
import com.jd.workflow.console.service.IFlowService;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Map;
import io.swagger.annotations.ApiOperation;

/**
 * 针对webservice，前端传入的格式以及返回给前端的格式为：
 * {
 *     id:'step1',
 *     type:"webservice",
 *     entityId:null,
 *     input:{
 *         headers:List<SimpleJsonType>,
 *         body:List<JsonType>
 *     },
 *     output:{
 *         headers:List<SimpleJsonType>
 *         body:List<JsonType>
 *     }
 * }
 * 需要合并前端的返回值与数据库存储的schemaType
 * 实际存储的为：
 * {
 *     id:'step1',
 *     type:"webservice",
 *     entityId:null,
 *     input:{
 *            schemaType:{
 *             "name": "Envelope",
 *             "namespacePrefix": "soapenv",
 *             "attrs": {
 *                 "xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/",
 *                 "xmlns:ser": "http://service.workflow.jd.com/"
 *             },
 *            children:[{}]
 *            }
 *     },
 *    output:{
 *        schemaType:{},
 *        input:{},
 *        body:{}
 *     }
 * }
 * 需要加工后才可以存下来
 */
@RestController
@Slf4j
@RequestMapping("/flow")
@UmpMonitor
@Api(value = "流程管理",tags="流程管理")
public class FlowController {

    /**
     */
    @Resource
    IFlowService flowService;

    @PostMapping(value = "/save")
    @ResponseBody
    @ApiOperation(value = "保存流程编排")
    public CommonResult<Long> saveFlow(@RequestBody FlowImportDTO dto) {
        return CommonResult.buildSuccessResult(flowService.saveFlow(dto));
    }

    @GetMapping(value = "/load")
    @ResponseBody
    @ApiOperation(value = "加载流程编排定义")
    public CommonResult<WorkflowDefinition> loadFlowDef(@RequestParam(name = "id") Long id) {
        WorkflowDefinition definition = flowService.loadFlowDef(id);
        return CommonResult.buildSuccessResult(definition);
    }

    @GetMapping(value = "/loadCamelXml")
    @ResponseBody
    @ApiOperation(value = "加载转换后的xml流程配置文件")
    public CommonResult<String> loadCamelXml(@RequestParam(name = "id") Long id) {
        WorkflowDefinition definition = flowService.loadFlowDef(id);
        //
        String xml = null;
        if (definition == null) {
            xml = RouteBuilder.buildRoute(definition);
        }
        return CommonResult.buildSuccessResult(xml);
    }
}

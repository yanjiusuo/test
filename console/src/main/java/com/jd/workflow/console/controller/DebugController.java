package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.FlowDebugDto;
import com.jd.workflow.console.dto.FlowDebugResult;
import com.jd.workflow.console.dto.LocalFlowDebugDto;
import com.jd.workflow.console.dto.WorkflowTreeBuilderDto;
import com.jd.workflow.console.dto.flow.param.HttpOutputExt;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.*;
import com.jd.workflow.console.service.DebugService;
import com.jd.workflow.console.service.IColorGatewayServiceImpl;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.debug.impl.DefaultJsfCallService;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.jsf.input.JsfOutput;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/debug")
@UmpMonitor
@Api(value = "调试管理",tags="调试管理")
public class DebugController {

    @Autowired
    DebugService debugService;
    @Autowired
    IMethodManageService methodManageService;
    @Autowired
    DefaultJsfCallService defaultJsfCallService;
    /**
     *
     * @param dto
     * @param request
     * @return 调试结果
     */
    @PostMapping(value = "/debugFlow")
    @ResponseBody
    @ApiOperation(value = "调试工作流")
    @ApiResponse(code = 200,message = "请求成功",response = FlowDebugResult.class)
    public CommonResult<Map<String, Object>> debugFlow(@RequestBody FlowDebugDto dto, HttpServletRequest request) {
        String cookie = request.getHeader("Cookie");
        return CommonResult.buildSuccessResult(debugService.debugFlow(dto, cookie).toMap());
    }

    @PostMapping(value = "/debugLoalFlow")
    @ResponseBody
    @ApiOperation(value = "调试本地工作流")
    @ApiResponse(code = 200,message = "请求成功",response = FlowDebugResult.class)
    public CommonResult<Map<String,Object>> debugLoalFlow(@RequestBody LocalFlowDebugDto dto) {

        CamelRouteLoader loader = new CamelRouteLoader();
        StepContext stepContext = loader.debugFlow(dto.getFlowDef(), dto.getInputDef(), null);
        FlowDebugResult result = new FlowDebugResult();
        result.setOutput(stepContext.getOutput());
        result.setStepContext(stepContext);
        return CommonResult.buildSuccessResult(result.toMap());
    }

    @PostMapping(value = "/debugHttp2Ws")
    @ResponseBody
    public CommonResult debugHttp2Ws(@RequestParam(name = "id") Long id, HttpServletRequest request) {
        return CommonResult.buildSuccessResult(null);
    }

    /**
     * 获取方法调试值
     *
     * @param id 方法id
     * @return 示例的请求值
     */
    @GetMapping(value = "getMethodDemoValue")
    public CommonResult<Object> getMethodDemoValue(@RequestParam(name = "id") Long id) {
        return CommonResult.buildSuccessResult(debugService.getReqBodyDemoValue(id));
    }

    /**
     * 获取方法示例值或者历史调试值
     * @param id 方法id
     * @return 方法示例值： http接口的格式为： {input:{targetAddress,url,body,params:Map,headers:Map,path:Map}} ，无历史记录targetAddress和url为null
     *      *      jsf接口的格式为：{input:{input:List<JsonType>,inputData:List,attachments:List<JsonType}}
     */
    @GetMapping(value = "getMethodDemoOrHistoryValue")
    public CommonResult<Object> getMethodDemoOrHistoryValue(@RequestParam(name = "id") Long id, Integer tag) {
        return CommonResult.buildSuccessResult(debugService.getMethodDemoOrHistoryValue(id,tag));
    }

    /**
     * 获取方法示例值
     *
     * @param id 方法id
     * @return 示例的请求值,结构同历史调试数据。 针对http接口，返回值结构未："input": {
     *             "headers": null,
     *             "path": {
     *                 "id": "22"
     *             },
     *             "body": null,
     *             "params": {
     *                 "id": "12"
     *             }
     *         }
     *         针对jsf接口，返回值为：
     *         {
     *     "input": {
     *         "methodId": 44405,
     *         "input": [{
     *                 "name": "arg0",
     *                 "type": "object",
     *                 "className": "com.jd.workflow.soap.example.jsf.entity.Person",
     *                 "exprType": "expr"
     *         ],
     *         "inputData": [{
     *                 "name": "123",
     *                 "id": 123,
     *                 "class": "com.jd.workflow.soap.example.jsf.entity.Person",
     *                 "age": 123
     *             }
     *         ],
     *         "interfaceName": "com.jd.workflow.soap.example.jsf.entity.IPersonService",
     *         "methodName": "save",
     *         "alias": "center",
     *         "site": null,
     *         "env": "test",
     *         "attachments": [{
     *                 "name": " .token",
     *                 "type": "string",
     *                 "value": "123456",
     *                 "nameEditable": true,
     *                 "className": null,
     *                 "exprType": "expr",
     *                 "required": true
     *             }
     *         ],
     *         "protocol": null
     *     }
     * }
     */
    @GetMapping(value = "getMethodExampleValue")
    public CommonResult<Object> getMethodExampleValue(@RequestParam(name = "id") Long id) {
        return CommonResult.buildSuccessResult(methodManageService.getMethodExampleValue(id));
    }



    /**
     * 获取流程编排的示例body值
     * @param id 流程编排方法id
     * @return 当body为
     */
    @GetMapping(value = "getFlowBodyDemoValue")
    public CommonResult<Object> getFlowBodyDemoValue(  Long id) {
        return CommonResult.buildSuccessResult(methodManageService.getFlowReqBodyDemoValue(id));
    }

    @PostMapping(value = "/debugJsf")
    @ResponseBody
    @ApiOperation(value = "调试jsf接口")
    public CommonResult<Map<String, Object>> debugJsf(@RequestBody @Valid JsfDebugDto dto) {
        final Map output = debugService.debugJsf(dto);
        return CommonResult.buildSuccessResult(output);
    }

    /**
     * 调试http方法
     *
     * @param dto http请求信息
     * @return
     */
    @PostMapping(value = "/debugHttpNew")
    @ResponseBody
    public CommonResult<HttpOutputExt> debugHttpNew(@RequestBody @Valid HttpDebugDto dto, HttpServletRequest req) {
        String erp = req.getParameter("erp");
        if(UserSessionLocal.getUser() == null){
            UserSessionLocal.setUser(new UserInfoInSession(erp,erp));
        }
        if(null!=dto.getIsColor()&& dto.getIsColor()){
            Guard.notEmpty(dto.getInput().getColorInputParam(),"color模式-color网关请求参数不能为空");
            //过滤无效header
            List<JsonType> headers=dto.getInput().getColorHeaders().stream().filter(i -> !ObjectUtils.isEmpty(i.getValue())).collect(Collectors.toList());
            dto.getInput().setColorHeaders(headers);
            debugService.initValidParam(dto);
        }
        HttpOutputExt output = debugService.debugHttp(dto, req.getHeader("Cookie"));
        return CommonResult.buildSuccessResult(output);
    }

    /**
     * 调用jsf方法
     * @param dto
     * @param req
     * @return
     */
    @PostMapping(value = "/debugJsfNew")
    @ResponseBody
    public CommonResult<JsfOutputExt> debugJsfNew(@RequestBody @Valid NewJsfDebugDto dto,HttpServletRequest req) {

        if (UserSessionLocal.getUser() == null || UserSessionLocal.getUser().getUserId() == null){
            String erp = req.getParameter("erp");
            UserSessionLocal.setUser(new UserInfoInSession(erp, erp));
        }
        return CommonResult.buildSuccessResult(debugService.debugJsfNew(dto));
    }
    @PostMapping(value = "/jsfJarDebug")
    @ResponseBody
    public CommonResult<Object> jsfJarDebug(@RequestBody @Valid JarJsfDebugDto dto, HttpServletRequest req) {
        String erp = req.getParameter("erp");
        if (UserSessionLocal.getUser() == null) {
            UserSessionLocal.setUser(new UserInfoInSession(erp, erp));
        }
        JsfOutput output = defaultJsfCallService.jarCallJsf(dto);
        return CommonResult.buildSuccessResult(output);
    }

    @PostMapping(value = "/workflowExprTree")
    @ResponseBody
    @ApiOperation(value = "获取快速映射目录树")
    public CommonResult<List<ExprTreeNode>> buildTree(@RequestBody WorkflowTreeBuilderDto dto) {
        return CommonResult.buildSuccessResult(debugService.buildWorkflowExprTree(dto));
    }

    /**
     * 构造伪代码
     *
     * @param dto
     * @return 完成的伪代码
     */
    @PostMapping(value = "/buildPseudoCode")
    @ResponseBody
    @ApiOperation(value = "获取编排伪代码")
    public CommonResult<String> buildPseudoCode(@RequestBody WorkflowTreeBuilderDto dto) {
        return CommonResult.buildSuccessResult(debugService.buildPseudoCode(dto.getDefinition()));
    }

    @PostMapping(value = "/uploadFile")
    @ResponseBody
    @ApiOperation(value = "上传附件")
    public CommonResult<String> uploadFile(@RequestParam MultipartFile multipartFile) {
        return CommonResult.buildSuccessResult(debugService.uploadFile(multipartFile));
    }

}

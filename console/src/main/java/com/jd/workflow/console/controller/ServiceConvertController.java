package com.jd.workflow.console.controller;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.ConvertWebServiceBaseDto;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.console.service.IServiceConvertService;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.legacy.SoapVersion;
import com.jd.workflow.soap.utils.WsdlUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import io.swagger.annotations.ApiOperation;

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
@RequestMapping("/serviceConvert")
@UmpMonitor
@Api(value = "服务转换相关",tags="服务转换相关")
public class ServiceConvertController {

    @Resource
    private UserPrivilegeHelper userPrivilegeHelper;

    @Resource
    private IServiceConvertService serviceConvertService;

    @PostMapping("/addHttpToWebService")
    @ApiOperation(value = "新增http转webservice方法")
    public CommonResult<Boolean> addHttpToWebService(@Validated @RequestBody HttpToWebServiceDTO httpToWebServiceDTO, HttpServletRequest request) {
        log.info("ServiceConvertController httpToWebService post={}", JsonUtils.toJSONString(httpToWebServiceDTO));
        Guard.notNull(httpToWebServiceDTO, "请求数据不能为空");
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        String basePath = WebServiceHelper.getBasePath(request);
        //3.service层
        Boolean ref = serviceConvertService.addHttpToWebService(basePath, httpToWebServiceDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/editHttpToWebService")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "http转webservice方法编辑")
    public CommonResult<HttpToWebServiceDTO> editHttpToWebService(Long id, Long interfaceId) {
        log.info("ServiceConvertController editHttpToWebService id={} , interfaceId={}", id, interfaceId);
        Guard.notNull(id, "编辑http转webservice内容时id不能为空");
        Guard.notNull(interfaceId, "编辑http转webservice内容时interfaceId不能为空");
        //service层
        HttpToWebServiceDTO ref = serviceConvertService.findHttpToWebService(id, interfaceId);
        ref.getInput().setSchemaType(null);
        ref.getInput().setDemoXml(null);
        ref.getOutput().setSchemaType(null);
        ref.getOutput().setDemoXml(null);
        ref.setWsdl(null);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/modifyHttpToWebService")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "修改保存http转webservice方法")
    public CommonResult<Boolean> modifyHttpToWebService(@Validated @RequestBody HttpToWebServiceDTO httpToWebServiceDTO, HttpServletRequest request) {
        log.info("ServiceConvertController modifyHttpToWebService post={}", JsonUtils.toJSONString(httpToWebServiceDTO));
        Guard.notNull(httpToWebServiceDTO, "请求数据不能为空");
        Guard.notNull(httpToWebServiceDTO.getId(), "修改http转换后webService的方法时id不能为空");
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        String basePath = WebServiceHelper.getBasePath(request);
        //3.service层
        Boolean ref = serviceConvertService.modifyHttpToWebService(basePath, httpToWebServiceDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/removeHttpToWebService")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "删除http转webservice方法")
    public CommonResult<Boolean> removeHttpToWebService(Long id, Long interfaceId) {
        log.info("ServiceConvertController removeHttpToWebService id={} , interfaceId={}", id, interfaceId);
        Guard.notNull(id, "删除http转换后webService的方法时id不能为空");
        Guard.notNull(interfaceId, "删除http转换后webService的方法时interfaceId不能为空");
        //service层
        Boolean ref = serviceConvertService.removeHttpToWebService(id, interfaceId);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping(value = "/getConvertWsdlContent", produces = "application/xml")
    @ApiOperation(value = "http转webservice方法获取wsdl内容")
    public //    @Authorization(key="interfaceId")
    String getConvertWsdlContent(HttpServletRequest request, Long id, Long interfaceId) {
        log.info("ServiceConvertController getConvertWsdlContent id={} , interfaceId={}", id, interfaceId);
        Guard.notNull(id, "获取转换webservice后的wsdl内容时id不能为空");
        Guard.notNull(interfaceId, "获取转换webservice后的wsdl内容时interfaceId不能为空");
        //service层
        String ref = serviceConvertService.getConvertWsdlContent(id, interfaceId);
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
        String endpointUrl = basePath + "serviceConvert/ws2http/" + interfaceId + "/" + id;
        String ret = WsdlUtils.replaceServiceAddressByContent(ref, endpointUrl);
        //出参
        return ret;
    }

    @GetMapping("/findHttpToWebServiceList")
    @ApiOperation(value = "http转webservice方法列表展示")
    public //    @Authorization(key="interfaceId")
    CommonResult<List<ConvertWebServiceBaseDto>> findHttpToWebServiceList(Long methodId, Long interfaceId) {
        log.info("ServiceConvertController findHttpToWebServiceList methodId={} , interfaceId={}", methodId, interfaceId);
        Guard.notNull(methodId, "获取转换webservice列表时id不能为空");
        Guard.notNull(interfaceId, "获取转换webservice列表时interfaceId不能为空");
        //service层
        List<ConvertWebServiceBaseDto> ref = serviceConvertService.findHttpToWebServiceList(methodId, interfaceId);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/tryCallHttpToWebService")
    @Authorization(key = "interfaceId")
    @ApiOperation(value = "进入http转webservice方法调试页面")
    public CommonResult<HttpToWebServiceDTO> tryCallHttpToWebService(Long id, Long interfaceId) {
        log.info("ServiceConvertController tryCallHttpToWebService id={} , interfaceId={}", id, interfaceId);
        Guard.notNull(id, "调试http转webservice内容时id不能为空");
        Guard.notNull(interfaceId, "调试http转webservice内容时interfaceId不能为空");
        //service层
        HttpToWebServiceDTO ref = serviceConvertService.findHttpToWebService(id, interfaceId);
        ref.setWsdl(null);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/ws2http/{interfaceId}/{id}")
    public void ws2http(@PathVariable(name = "id") Long id, @PathVariable(name = "interfaceId") Long interfaceId, @RequestBody String content, HttpServletResponse response) {
        log.info("ServiceConvertController tryCallHttpToWebService id={} , interfaceId={},content={}", id, interfaceId, content);
        SoapContext context = SoapContext.DEFAULT;
        String result = null;
        if (id == null) {
            result = SoapMessageBuilder.buildFault("soap:Server", "id not allow empty", SoapVersion.Soap11, context);
        }
        if (interfaceId == null) {
            result = SoapMessageBuilder.buildFault("soap:Server", "interface id not allow empty", SoapVersion.Soap11, context);
        }
        if (StringUtils.isEmpty(content)) {
            result = SoapMessageBuilder.buildFault("soap:Server", "req content not allow empty", SoapVersion.Soap11, context);
        }
        if (result == null) {
            HttpOutput output = serviceConvertService.ws2http(id, content);
            result = (String) output.getBody();
        }
        try {
            OutputStream os = response.getOutputStream();
            os.write(result.getBytes("utf-8"));
            os.flush();
        } catch (IOException e) {
            throw new BizException("响应失败", e);
        }
    }

    @PostMapping("/callHttpToWebService")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "http转webservice方法调试调用")
    public CommonResult<Object> callHttpToWebService(@Validated @RequestBody CallHttpToWebServiceReqDTO callHttpToWebServiceReqDTO) {
        log.info("ServiceConvertController callHttpToWebService post={}", JsonUtils.toJSONString(callHttpToWebServiceReqDTO));
        //service层
        Object ref = serviceConvertService.callHttpToWebService(callHttpToWebServiceReqDTO);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }
}

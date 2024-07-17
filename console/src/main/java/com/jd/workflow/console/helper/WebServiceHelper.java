package com.jd.workflow.console.helper;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.dto.WebServiceMethod;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.client.core.SoapClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.legacy.SoapVersion;
import com.jd.workflow.soap.legacy.SoapVersion11;
import com.jd.workflow.soap.wsdl.*;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.utils.WsdlUtils;
import com.jd.workflow.soap.wsdl.param.Param;
import com.jd.workflow.soap.wsdl.param.ParamType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：example
 * 类 名 称：WebServiceHelper
 * 类 描 述：webservice转换
 * 创建时间：2022-05-27 14:45
 * 创 建 人：wangxiaofei8
 */
@Slf4j
public class WebServiceHelper {
    public static String getBasePath(HttpServletRequest request){
      /*  Long interfaceId = dto.getInterfaceId();
        Long id = dto.getId();*/
        String basePath = request.getScheme()+"://" +
                request.getServerName() + ":" + request.getServerPort() +
                request.getContextPath() ;

        return basePath;

    }
    public static String getWebServiceCallUrl(String basePath,
                                              Long interfaceId,Long id){


        String endpointUrl = basePath+"/serviceConvert/ws2http/"+interfaceId+"/"+id;

        return endpointUrl;
    }
    public static String getWebServiceCallUrl(HttpServletRequest request,
                                              Long interfaceId,Long id){


        return getWebServiceCallUrl(getBasePath(request),interfaceId,id);
    }
    /**
     * 生成Definition  HttpWsdlGenerator
     * @param httpToWebServiceDTO
     * @return
     * @throws WSDLException
     */
    public static Definition generateWsdlDefinition(HttpToWebServiceDTO httpToWebServiceDTO) throws WSDLException {

        return HttpWsdlGenerator.generateWsdlDefinition(httpToWebServiceDTO.toHttpDefinition());
        /*String prefix = StringUtils.capitalize(httpToWebServiceDTO.getMethodName());
        List input = new ArrayList();
        if(CollectionUtils.isNotEmpty(httpToWebServiceDTO.getInput().getHeaders())){
            input.add(HttpWsdlGenerator.toParam(httpToWebServiceDTO.getInput().getHeaders(),prefix,"Headers"));
        }
        if(CollectionUtils.isNotEmpty(httpToWebServiceDTO.getInput().getParams())){
            input.add(HttpWsdlGenerator.toParam(httpToWebServiceDTO.getInput().getParams(),prefix,"Params"));
        }
        if(CollectionUtils.isNotEmpty(httpToWebServiceDTO.getInput().getBody())){
            input.add(HttpWsdlGenerator.toParam(httpToWebServiceDTO.getInput().getBody(),prefix,"Body"));
        }


        Param response = new Param();
        response.setName(prefix+"Response");
        response.setParamType(ParamType.OBJECT);
        if(CollectionUtils.isNotEmpty(httpToWebServiceDTO.getOutput().getHeaders())){
            response.getChildren().add(HttpWsdlGenerator.toParam(httpToWebServiceDTO.getOutput().getHeaders(),prefix,"ResponseHeaders"));
        }
        if(CollectionUtils.isNotEmpty(httpToWebServiceDTO.getOutput().getBody())){
            response.getChildren().add(HttpWsdlGenerator.toParam(httpToWebServiceDTO.getOutput().getBody(),prefix,"ResponseBody"));
        }
        WsdlModelInfo wsdlModelInfo = new WsdlModelInfo();
        wsdlModelInfo.setTargetNamespace(httpToWebServiceDTO.generateTargetNamespace());
        wsdlModelInfo.setServiceName(httpToWebServiceDTO.getServiceName());
        List<ServiceMethodInfo> methods = new ArrayList<>();
        ServiceMethodInfo method = new ServiceMethodInfo(httpToWebServiceDTO.getMethodName(),false);
        method.setInputParams(input);
        method.setOutParam(response);
        methods.add(method);
        wsdlModelInfo.setMethods(methods);
        WsdlGenerator wsdlGenerator = new WsdlGenerator(wsdlModelInfo);
        return wsdlGenerator.buildWsdlDefinition();*/
    }


    /**
     * 封装wsdl以及input、output中deamoxml和schemtype
     * @param httpToWebServiceDTO
     * @param definition
     * @throws Exception
     */
    public static void assembleWsdlInfos(HttpToWebServiceDTO httpToWebServiceDTO,Definition definition) throws Exception {
        //封装wsdl信息
        httpToWebServiceDTO.setWsdl(WsdlUtils.wsdlToString(definition));
        HttpDefinition httpDefinition =httpToWebServiceDTO.toHttpDefinition();
        //wsdlurl参数拼装
        //String wsdlUrl = httpToWebServiceDTO.generateTargetNamespace()+httpToWebServiceDTO.getServiceName()+"?wsdl";
        // 构造soapMessageBuilder
        SoapMessageBuilder soapMessageBuilder = new SoapMessageBuilder(definition);
        // 构造transformer
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(definition);
        SoapContext context = SoapContext.DEFAULT;
        for (Object o : definition.getAllBindings().entrySet()) {
            Map.Entry<QName, Binding> entry = (Map.Entry<QName, Binding>) o;
            Binding binding = entry.getValue();
            List<BindingOperation> operations = binding.getBindingOperations();
            for (BindingOperation operation : operations) {
                String inputMsg =  soapMessageBuilder.buildSoapMessageFromInput(entry.getValue(),operation,context); // 构造示例输入数据
                String outMsg =  soapMessageBuilder.buildSoapMessageFromOutput(entry.getValue(),operation,context); // 构造示例输出数据
                httpToWebServiceDTO.getInput().setDemoXml(inputMsg);
                httpToWebServiceDTO.getOutput().setDemoXml(outMsg);
                JsonType inputSchema = transformer.buildSoapMessageFromInput(binding,
                        operation, SoapContext.DEFAULT).toJsonType();
                JsonType outputSchema = transformer.buildSoapMessageFromOutput(binding,
                        operation, SoapContext.DEFAULT).toJsonType();
                httpToWebServiceDTO.getInput().setSchemaType(inputSchema);
                HttpWsdlGenerator.setReqRawName(inputSchema,httpDefinition);
                HttpWsdlGenerator.setRespRawName(outputSchema,httpDefinition);
                httpToWebServiceDTO.getOutput().setSchemaType(outputSchema);
                return;
            }
        }
    }

    public static HttpOutput debugWebServiceMethod(CallHttpToWebServiceReqDTO dto,String cookie, String callUrl,
                                                   WebServiceMethod method)  {
        try{
            JsonType inputType  = method.getInput().getSchemaType();
            String soapAction = method.getSoapAction();
            String inputXml = null;
            if("xml".equals(dto.getInputType())){
                inputXml = (String) dto.getInput();
            }else { // json
                JsonType input = null;
                try{

                    input = JsonUtils.cast(dto.getInput(), JsonType.class);
                }catch (Exception e){
                    log.error("webservice.err_cast_input",e);
                    throw new BizException("xml类型的input无效");
                }

                Object exprValue = input.toExprValue();
                inputXml = inputType.transformToXml(exprValue).get(0).toXml();
            }


            HttpClientBuilder builder =HttpClientBuilder.create();
            CloseableHttpClient httpClient = builder.build();

            log.info("webservice.debug_service:url={},content={}",callUrl,inputXml);
            HttpPost post = new HttpPost();
            post.addHeader("Cookie", cookie);
            post.addHeader("SOAPAction", SoapVersion.Soap11.getSoapActionHeader(soapAction));
            post.setURI(new URI(callUrl));
            post.setEntity(new StringEntity(inputXml, ContentType.create("text/xml", StandardCharsets.UTF_8)));
            HttpOutput output = new HttpOutput();

            HttpInput input = new HttpInput();
            input.setBody(inputXml);
            input.setReqType(ReqType.xml);
            input.setUrl(callUrl);
            //output.setInput(input);
            CloseableHttpResponse response = httpClient.execute(post);
            for (Header allHeader : response.getAllHeaders()) {
                output.getHeaders().put(allHeader.getName(),allHeader.getValue());
            }
            int statusCode = response.getStatusLine().getStatusCode();

            output.setStatus(statusCode);
            if (statusCode > 199 && statusCode < 300) {
                output.setSuccess(true);
            }else{
                output.setSuccess(false);
            }
            String result  = EntityUtils.toString(response.getEntity(),"utf-8");

            output.setBody(result);
            return output;
        }catch (Exception e){
            log.error("webservice.err_debug_webservice:callUrl={}",callUrl,e);
            throw BizException.adapt("执行失败:"+e.getMessage(),e);
        }

    }
}

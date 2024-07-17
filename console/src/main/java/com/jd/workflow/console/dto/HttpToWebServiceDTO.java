package com.jd.workflow.console.dto;

import com.jd.workflow.soap.utils.StringHelper;
import com.jd.workflow.soap.wsdl.HttpDefinition;
import com.jd.workflow.soap.wsdl.HttpWsdlGenerator;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 项目名称：example
 * 类 名 称：HttpToWebServiceDTO
 * 类 描 述：http转webserviceDto
 * 创建时间：2022-05-26 20:50
 * 创 建 人：wangxiaofei8
 */
@Data
public class HttpToWebServiceDTO {

    /**
     * 转webservice方法后主键
     */
    private Long id;

    /**
     * http接口对应的id
     */
    @NotNull(message = "接口Id不能为空")
    private Long interfaceId;

    /**
     * http接口对应的方法ID
     */
    @NotNull(message = "http方法Id不能为空")
    private Long methodId;

    /**
     * 包名 -> targetNamespace  http://pkgName
     */
    @NotBlank(message = "包名不能为空")
    private String pkgName;

    /**
     * 服务名  methodName + Service
     */
    private String serviceName;

    /**
     * 调用环境 name
     */
    @NotBlank(message = "调用环境不能为空")
    private String env;

    /**
     * 服务方法
     */
    @NotBlank(message = "方法名不能为空")
    private String methodName;

    /**
     * request
     */
    @NotNull(message = "input不能为空")
    private WebServiceInputDTO input;

    /**
     * response
     */
    @NotNull(message = "output不能为空")
    private WebServiceOutputDTO output;

    /**
     * 保存后生成的额wsdl
     */
    private String wsdl;

    /**
     * 调用地址 发布后才能调用 调试时使用
     */
    private String endpointUrl;

    /**
     * 生成targetNamespace
     * @return
     */
    public String generateTargetNamespace(){
        if(pkgName!=null&&pkgName.length()>0){
            return "http://"+StringHelper.getPkgNameByNamespace(pkgName)+"/";
        }
        return null;
    }

    public HttpDefinition toHttpDefinition(){
        HttpDefinition definition = new HttpDefinition();
        definition.setMethodName(this.getMethodName());
        definition.setRespBody(this.getOutput().getBody());
        definition.setRespHeaders(this.getOutput().getHeaders());
        definition.setParams(this.getInput().getParams());
        definition.setHeaders(this.getInput().getHeaders());
        definition.setBody(this.getInput().getBody());
        definition.setPath(this.getInput().getPath());
        definition.setReqType(this.getInput().getReqType());
        definition.setPkgName(this.getPkgName());
        definition.setServiceName(this.getServiceName());
        definition.setWebServiceCallUrl(this.getEndpointUrl());
        return definition;
    }

}

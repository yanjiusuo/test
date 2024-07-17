package com.jd.workflow.console.service.impl;

import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.EnvTypeEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.PublishEnum;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import java.util.ArrayList;
import java.util.List;
import com.jd.workflow.console.dao.mapper.MethodManageMapper;
import com.jd.workflow.console.helper.ProjectHelper;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import java.lang.Long;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.String;
import com.jd.workflow.flow.core.output.HttpOutput;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * websrvice方法转换单元测试
 */
@RunWith(PowerMockRunner.class)
@MockPolicy(Slf4jMockPolicy.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({UserSessionLocal.class,UserInfoInSession.class,WebServiceHelper.class})
public class ServiceConvertServiceImplTest {

    @Before
    public void init(){
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), MethodManage.class);
        PowerMockito.mockStatic(UserSessionLocal.class);
        PowerMockito.mockStatic(UserInfoInSession.class);
        PowerMockito.mockStatic(WebServiceHelper.class);
        UserInfoInSession session = new UserInfoInSession();
        session.setUserName("test_admin");
        session.setUserId("123666");
        PowerMockito.when(UserSessionLocal.getUser()).thenReturn(session);
        try {
            PowerMockito.when(WebServiceHelper.generateWsdlDefinition(Mockito.any())).thenReturn(null);
            PowerMockito.doNothing().when(WebServiceHelper.class, "assembleWsdlInfos", Matchers.any(),Matchers.any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @InjectMocks
    private ServiceConvertServiceImpl serviceConvertServiceImpl;

    @Mock
    private MethodManageMapper methodManageMapper;

    @Mock
    private ProjectHelper projectHelper;

    @Mock
    private IMethodManageService methodManageService;

    @Mock
    private IInterfaceManageService manageService;

    /**
     * mock 转换 dto
     */
    private static HttpToWebServiceDTO mockHttpToWebServiceDTO = buildHttpToWebServiceDTO();



    @Test
    public void when_addHttpToWebService_then_return_success(){
        MethodManage obj = new MethodManage();
        obj.setType(InterfaceTypeEnum.HTTP.getCode());
        when(methodManageMapper.updateById(Mockito.any())).thenReturn(1);
        when(methodManageService.getOne(Mockito.any())).thenReturn(null);
        when(methodManageMapper.selectOne(Mockito.any())).thenReturn(obj);
        when(methodManageMapper.insert(Mockito.any())).thenReturn(1);
        String basePath = "http://localhost:8080/serviceConvert/addHttpToWebService";
        Boolean returnResult = serviceConvertServiceImpl.addHttpToWebService(basePath,mockHttpToWebServiceDTO);
        assert returnResult == true;
    }

    @Test
    public void when_modifyHttpToWebService_then_return_success(){
        when(methodManageMapper.updateById(Mockito.any())).thenReturn(1);
        MethodManage obj = new MethodManage();
        when(methodManageMapper.selectOne(Mockito.any())).thenReturn(obj);
        String basePath = "http://localhost:8080/serviceConvert/editHttpToWebService";
        Boolean returnResult = serviceConvertServiceImpl.modifyHttpToWebService(basePath,mockHttpToWebServiceDTO);
        assert returnResult == true;
    }

    @Test
    public void when_removeHttpToWebService_then_return_success(){
        MethodManage existedObj = new MethodManage();
        when(methodManageMapper.updateById(Mockito.any())).thenReturn(1);
        when(methodManageMapper.selectOne(Mockito.any())).thenReturn(existedObj);
        Long id = 8l;
        Long interfaceId = 6l;
        Boolean returnResult = serviceConvertServiceImpl.removeHttpToWebService(id,interfaceId);
        assert returnResult == true;
    }

    @Test
    public void when_getConvertWsdlContent_then_return_success(){
        MethodManage existedObj = new MethodManage();
        existedObj.setId(8l);
        existedObj.setPublished(PublishEnum.YES.getCode());
        existedObj.setContent(JsonUtils.toJSONString(mockHttpToWebServiceDTO));
        when(projectHelper.getPublishUrl(Mockito.anyString())).thenReturn("http://11.138.2.200/api/routeService/8");
        when(methodManageMapper.selectOne(Mockito.any())).thenReturn(existedObj);
        Long id = 8l;
        Long interfaceId = 6l;
        String returnResult = serviceConvertServiceImpl.getConvertWsdlContent(id,interfaceId);
        assert returnResult != null;
    }

    @Test
    public void when_findHttpToWebService_then_return_success(){
        MethodManage existedObj = new MethodManage();
        existedObj.setId(8l);
        existedObj.setPublished(PublishEnum.YES.getCode());
        existedObj.setContent(JsonUtils.toJSONString(mockHttpToWebServiceDTO));
        when(projectHelper.getPublishUrl(Mockito.anyString())).thenReturn("http://11.138.2.200/api/routeService/8");
        when(methodManageMapper.selectOne(Mockito.any())).thenReturn(existedObj);
        Long id = 8l;
        Long interfaceId = 6l;
        HttpToWebServiceDTO returnResult = serviceConvertServiceImpl.findHttpToWebService(id,interfaceId);
        assert returnResult != null;
    }

    @Test
    public void when_findHttpToWebServiceList_then_return_success(){
        when(methodManageMapper.selectList(Mockito.any())).thenReturn(new ArrayList());
        Long id = 8l;
        Long interfaceId = 6l;
        List returnResult = serviceConvertServiceImpl.findHttpToWebServiceList(id,interfaceId);
        assert returnResult != null;
    }

    @Test
    public void when_callHttpToWebService_then_return_success(){
        MethodManage existedObj = new MethodManage();
        existedObj.setId(8l);
        existedObj.setPublished(PublishEnum.YES.getCode());
        existedObj.setContent(JsonUtils.toJSONString(mockHttpToWebServiceDTO));
        when(projectHelper.getPublishUrl(Mockito.anyString())).thenReturn("http://11.138.2.200/api/routeService/8");
        when(methodManageMapper.selectOne(Mockito.any())).thenReturn(existedObj);
        CallHttpToWebServiceReqDTO callHttpToWebServiceReqDTOData0 = new CallHttpToWebServiceReqDTO();
        callHttpToWebServiceReqDTOData0.setMethodId(3l);
        callHttpToWebServiceReqDTOData0.setInterfaceId(6l);
        callHttpToWebServiceReqDTOData0.setEnvName("测试环境");
        callHttpToWebServiceReqDTOData0.setEndpointUrl("testData");
        callHttpToWebServiceReqDTOData0.setInput("testData");
        callHttpToWebServiceReqDTOData0.setInputType("testData");
        Exception e = null;
        try {
            Object returnResult = serviceConvertServiceImpl.callHttpToWebService(callHttpToWebServiceReqDTOData0);
        } catch (Exception ex) {
            e = ex;
        }
        //assert e instanceof ToXmlTransformException;
        assert e==null;
    }

    @Test
    public void when_ws2http_then_return_success(){
        MethodManage existedObj = new MethodManage();
        existedObj.setId(8l);
        existedObj.setPublished(PublishEnum.YES.getCode());
        existedObj.setContent(JsonUtils.toJSONString(mockHttpToWebServiceDTO));
        when(projectHelper.getPublishUrl(Mockito.anyString())).thenReturn("http://11.138.2.200/api/routeService/8");
        when(methodManageMapper.selectOne(Mockito.any())).thenReturn(existedObj);
        when(methodManageService.getById(Mockito.any())).thenReturn(existedObj);
        InterfaceManage interfaceObj = new InterfaceManage();
        interfaceObj.setId(1l);
        List<EnvModel> models = new ArrayList<>();
        EnvModel model = new EnvModel("测试环境","http://mock.interface.jd.com", EnvTypeEnum.TEST);
        models.add(model);
        interfaceObj.setEnv(JsonUtils.toJSONString(models));
        when(manageService.getById(Mockito.any())).thenReturn(interfaceObj);
        Long id = 8l;
        String content = "testData";
        HttpOutput returnResult = null;
        try {
            returnResult = serviceConvertServiceImpl.ws2http(id,content);
        } catch (Exception e) {
        }
        assert returnResult == null;
    }




    private static HttpToWebServiceDTO buildHttpToWebServiceDTO(){
        String content = "{\"type\":\"webservice\",\"methodName\":\"exception\",\"methodId\":null,\"envName\":null,\"successCondition\":null,\"input\":{\"demoXml\":\"<soapenv:Envelope xmlns:soapenv=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\" xmlns:web=\\\"http://webservice.example.soap.workflow.jd.com/\\\">\\n   <soapenv:Header/>\\n   <soapenv:Body>\\n      <web:exception>\\n         <arg0>?</arg0>\\n      </web:exception>\\n   </soapenv:Body>\\n</soapenv:Envelope>\",\"schemaType\":{\"name\":\"Envelope\",\"namespacePrefix\":\"soapenv\",\"attrs\":{\"xmlns:soapenv\":\"http://schemas.xmlsoap.org/soap/envelope/\",\"xmlns:web\":\"http://webservice.example.soap.workflow.jd.com/\"},\"type\":\"object\",\"children\":[{\"name\":\"Header\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[]},{\"name\":\"Body\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[{\"name\":\"exception\",\"namespacePrefix\":\"web\",\"type\":\"object\",\"children\":[{\"name\":\"arg0\",\"required\":true,\"type\":\"integer\"}]}]}]}},\"output\":{\"demoXml\":\"<soapenv:Envelope xmlns:soapenv=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\" xmlns:web=\\\"http://webservice.example.soap.workflow.jd.com/\\\">\\n   <soapenv:Header/>\\n   <soapenv:Body>\\n      <web:exceptionResponse/>\\n   </soapenv:Body>\\n</soapenv:Envelope>\",\"schemaType\":{\"name\":\"Envelope\",\"namespacePrefix\":\"soapenv\",\"attrs\":{\"xmlns:soapenv\":\"http://schemas.xmlsoap.org/soap/envelope/\",\"xmlns:web\":\"http://webservice.example.soap.workflow.jd.com/\"},\"type\":\"object\",\"children\":[{\"name\":\"Header\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[]},{\"name\":\"Body\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[{\"name\":\"exceptionResponse\",\"namespacePrefix\":\"web\",\"type\":\"string\"}]}]}}}";
        HttpToWebServiceDTO dto = JsonUtils.parse(content, HttpToWebServiceDTO.class);
        dto.setId(8l);
        dto.setInterfaceId(6l);
        dto.setMethodId(2l);
        dto.setEnv("测试环境");

        List<JsonType> headers = new ArrayList<>();
        JsonType o1 = new SimpleJsonType(SimpleParamType.STRING,"Content-Type");
        o1.setValue("application/json");
        o1.setRequired(true);
        o1.setDesc("请求头信息");
        headers.add(o1);
        dto.getInput().setHeaders(headers);

        List<JsonType> body = new ArrayList<>();
        ObjectJsonType o2 = new ObjectJsonType();
        o2.setRequired(true);
        o2.setName("skuInfo");
        o2.setDesc("商品详情");
        o2.setClassName("com.jd.console.User");
        body.add(o2);
        dto.getOutput().setBody(body);
        dto.setWsdl("wsdlcontent_mock");
        return dto;
    }

}

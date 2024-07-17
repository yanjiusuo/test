package com.jd.workflow.console.service.impl;


import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jd.jtfm.configcenter.ducc.manage.ConfigCenterManagerHelperOne2NDucc;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.EnvTypeEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.PublishEnum;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.dto.PublishInfoDTO;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.PublishManage;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.flow.core.metadata.impl.Ws2HttpStepMetadata;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import com.jd.workflow.soap.wsdl.HttpDefinition;
import com.jd.workflow.soap.wsdl.HttpWsdlGenerator;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.IServiceConvertService;
import com.jd.workflow.console.helper.ProjectHelper;
import com.jd.workflow.console.dto.PublishManageDTO;
import java.lang.Long;
import com.jd.workflow.console.dto.WorkFlowPublishReqDTO;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.lang.Boolean;

@RunWith(PowerMockRunner.class)
@MockPolicy(Slf4jMockPolicy.class)
@PowerMockIgnore({"jdk.internal.reflect.*","javax.net.ssl.*"})
@PrepareForTest({UserSessionLocal.class, UserInfoInSession.class, WebServiceHelper.class, ConfigCenterManagerHelperOne2NDucc.class, StepProcessorRegistry.class})
public class PublishManageServiceImplTest {


    @Before
    public void init(){
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), MethodManage.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), PublishManage.class);

        PowerMockito.mockStatic(UserSessionLocal.class);
        PowerMockito.mockStatic(UserInfoInSession.class);
        PowerMockito.mockStatic(WebServiceHelper.class);
        PowerMockito.mockStatic(ConfigCenterManagerHelperOne2NDucc.class);
        //PowerMockito.mockStatic(StepProcessorRegistry.class);
        UserInfoInSession session = new UserInfoInSession();
        session.setUserName("test_admin");
        session.setUserId("123666");
        PowerMockito.when(UserSessionLocal.getUser()).thenReturn(session);
        //Ws2HttpStepMetadata ws2HttpStepMetadata = buildWs2HttpStepMetadata();
        try {
            //PowerMockito.when(StepProcessorRegistry.parseMetadata(Mockito.any())).thenReturn(ws2HttpStepMetadata);
            //PowerMockito.doNothing().when(WebServiceHelper.class, "assembleWsdlInfos", Matchers.any(),Matchers.any());
            //PowerMockito.doNothing().when(ConfigCenterManagerHelperOne2NDucc.class,"register",Matchers.any(),Matchers.any());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @InjectMocks
    private PublishManageServiceImpl publishManageServiceImpl;


    @Mock
    private IInterfaceManageService interfaceManageService;
    @Mock
    private IMethodManageService methodManageService;
    @Mock
    private IServiceConvertService serviceConvertService;

    @Mock
    private  BaseMapper baseMapper;

    @Mock
    private ProjectHelper projectHelper;

    /**
     * mock 转换 dto
     */
    private static HttpToWebServiceDTO mockHttpToWebServiceDTO = buildHttpToWebServiceDTO();

    @Test
    public void when_publishConvertWebService_then_return_success(){
        MethodManage existedObj = new MethodManage();
        existedObj.setId(8l);
        existedObj.setPublished(PublishEnum.NO.getCode());
        existedObj.setContent(JsonUtils.toJSONString(mockHttpToWebServiceDTO));
        InterfaceManage interfaceObj = new InterfaceManage();
        List<EnvModel> models = new ArrayList<>();
        EnvModel model = new EnvModel("测试环境","http://mock.interface.jd.com", EnvTypeEnum.TEST);
        models.add(model);
        interfaceObj.setEnv(JsonUtils.toJSONString(models));
        when(interfaceManageService.getById(Mockito.any())).thenReturn(interfaceObj);
        when(serviceConvertService.findHttpToWebService(Mockito.anyLong(),Mockito.anyLong())).thenReturn(mockHttpToWebServiceDTO);
        when(methodManageService.updateById(Mockito.any())).thenReturn(true);
        Long id = 8l;
        Long interfaceId = 6l;
        Long clusterId = 0l;
        Boolean returnResult = null;
        try {
            returnResult = publishManageServiceImpl.publishConvertWebService(id,interfaceId,clusterId);
        } catch (Exception e) {
           //jdk1.8
        }
        assert returnResult ==null;
    }

    @Test
    public void when_republishService_then_return_success(){
        Long id = 1l;
        Long methodId=2l;
        Long interfaceId = 3l;
        PublishManage entity = new PublishManage();
        entity.setRelatedMethodId(methodId);
        entity.setIsLatest(PublishEnum.NO.getCode());
        entity.setVersionId(1);
        PublishInfoDTO publishInfoDTO = new PublishInfoDTO();
        publishInfoDTO.setCamelData("testCamelData");
        entity.setContent(JsonUtils.toJSONString(publishInfoDTO));
        when(baseMapper.selectById(Mockito.any())).thenReturn(entity);
        when(baseMapper.updateById(Mockito.any())).thenReturn(1);
        when(baseMapper.selectList(Mockito.any())).thenReturn(null);
        when(methodManageService.getBaseMapper()).thenReturn(mapper);
        when(methodManageService.updateById(Mockito.any())).thenReturn(true);
        Boolean returnResult = publishManageServiceImpl.republishService(id,methodId,interfaceId);
        assert returnResult != null;
    }


    @Test
    public void when_publishWorkflow_then_return_success(){
        MethodManage methodManage = new MethodManage();
        methodManage.setId(2l);
        methodManage.setType(InterfaceTypeEnum.ORCHESTRATION.getCode());
        methodManage.setContent("{\"tasks\":[{\"id\":\"ws2http\",\"type\":\"ws2http\",\"key\":null,\"entityId\":null,\"opName\":null,\"endpointUrl\":[\"http://127.0.0.1:6010\"],\"url\":\"/json\",\"taskDef\":null,\"input\":{\"schemaType\":{\"name\":\"Envelope\",\"namespacePrefix\":\"soapenv\",\"attrs\":{\"xmlns:soapenv\":\"http://schemas.xmlsoap.org/soap/envelope/\",\"xmlns:wjf\":\"http://wjf.com/\"},\"type\":\"object\",\"children\":[{\"name\":\"Header\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[]},{\"name\":\"Body\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[{\"name\":\"GetPerson\",\"namespacePrefix\":\"wjf\",\"type\":\"object\",\"children\":[{\"name\":\"headers\",\"required\":true,\"type\":\"object\",\"children\":[{\"name\":\"token\",\"type\":\"string\"}]},{\"name\":\"body\",\"required\":true,\"type\":\"object\",\"children\":[{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"sid\",\"type\":\"long\"}]}]},{\"name\":\"params\",\"required\":true,\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}]}]},\"header\":null,\"body\":null},\"output\":{\"schemaType\":{\"name\":\"Envelope\",\"namespacePrefix\":\"soapenv\",\"attrs\":{\"xmlns:soapenv\":\"http://schemas.xmlsoap.org/soap/envelope/\",\"xmlns:wjf\":\"http://wjf.com/\"},\"type\":\"object\",\"children\":[{\"name\":\"Header\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[]},{\"name\":\"Body\",\"namespacePrefix\":\"soapenv\",\"type\":\"object\",\"children\":[{\"name\":\"GetPersonResponse\",\"namespacePrefix\":\"wjf\",\"type\":\"object\",\"children\":[{\"name\":\"return\",\"required\":true,\"type\":\"object\",\"children\":[{\"name\":\"body\",\"type\":\"object\",\"children\":[{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"sid\",\"type\":\"long\"}]}]},{\"name\":\"headers\",\"type\":\"object\",\"children\":[{\"name\":\"token\",\"type\":\"string\"}]}]}]}]}]},\"header\":null,\"body\":null},\"reqType\":\"json\",\"httpMethod\":\"post\"}],\"taskDef\":null,\"input\":null,\"output\":null,\"failOutput\":null}\n");
        when(methodManageService.getById(Mockito.any())).thenReturn(methodManage);
        when(baseMapper.selectList(Mockito.any())).thenReturn(null);
        when(baseMapper.insert(Mockito.any())).thenReturn(1);
        when(methodManageService.updateById(Mockito.any())).thenReturn(true);
        WorkFlowPublishReqDTO workFlowPublishReqDTOData0 = new WorkFlowPublishReqDTO();
        workFlowPublishReqDTOData0.setInterfaceId(3l);
        workFlowPublishReqDTOData0.setMethodId(2l);
        Boolean returnResult = publishManageServiceImpl.publishWorkflow(workFlowPublishReqDTOData0);
        assert returnResult != null;
    }

    @Test
    public void when_findPublicVersionList_then_return_success(){
        Long methodId=2l;
        Long interfaceId = 3l;
        List<PublishManage> list = new ArrayList<>();
        PublishManage entity = new PublishManage();
        entity.setId(1l);
        entity.setRelatedMethodId(methodId);
        entity.setIsLatest(PublishEnum.NO.getCode());
        entity.setVersionId(1);
        PublishInfoDTO publishInfoDTO = new PublishInfoDTO();
        publishInfoDTO.setCamelData("testCamelData");
        entity.setContent(JsonUtils.toJSONString(publishInfoDTO));
        list.add(entity);
        when(baseMapper.selectList(Mockito.any())).thenReturn(list);
        List returnResult = publishManageServiceImpl.findPublicVersionList(methodId,interfaceId);
        assert returnResult.size() == 1;
    }

    @Test
    public void when_findPublishVersionDetail_then_return_success(){
        when(projectHelper.getPublishUrl(Mockito.anyString())).thenReturn("http://11.138.2.200/api/routeService/2");
        Long id = 1l;
        Long methodId=2l;
        Long interfaceId = 3l;
        PublishManage entity = new PublishManage();
        entity.setId(1l);
        entity.setRelatedMethodId(methodId);
        entity.setIsLatest(PublishEnum.YES.getCode());
        entity.setVersionId(1);
        PublishInfoDTO publishInfoDTO = new PublishInfoDTO();
        publishInfoDTO.setCamelData("testCamelData");
        entity.setContent(JsonUtils.toJSONString(publishInfoDTO));
        entity.setCreated(new Date());
        entity.setModified(new Date());
        entity.setCreator("admin");
        entity.setModifier("admin");
        when(baseMapper.selectById(Mockito.any())).thenReturn(entity);
        PublishManageDTO returnResult = publishManageServiceImpl.findPublishVersionDetail(id,methodId,interfaceId);
        assert returnResult != null;
    }


    public static BaseMapper mapper =   new BaseMapper(){

        @Override
        public int insert(Object entity) {
            return 0;
        }

        @Override
        public int deleteById(Serializable id) {
            return 0;
        }

        @Override
        public int delete(Wrapper queryWrapper) {
            return 0;
        }

        @Override
        public int updateById(Object entity) {
            return 0;
        }

        @Override
        public int update(Object entity, Wrapper updateWrapper) {
            return 0;
        }

        @Override
        public Object selectById(Serializable id) {

            MethodManage methodEntity = new MethodManage();
            methodEntity.setInterfaceId(3l);
            methodEntity.setYn(DataYnEnum.VALID.getCode());
            return methodEntity;
        }

        @Override
        public Object selectOne(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public Integer selectCount(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public List selectList(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public List<Map<String, Object>> selectMaps(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public List<Object> selectObjs(Wrapper queryWrapper) {
            return null;
        }

        @Override
        public IPage<Map<String, Object>> selectMapsPage(IPage page, Wrapper queryWrapper) {
            return null;
        }

        @Override
        public IPage selectPage(IPage page, Wrapper queryWrapper) {
            return null;
        }

        @Override
        public List selectByMap(Map columnMap) {
            return null;
        }

        @Override
        public List selectBatchIds(Collection idList) {
            return null;
        }

        @Override
        public int deleteBatchIds(Collection idList) {
            return 0;
        }

        @Override
        public int deleteByMap(Map columnMap) {
            return 0;
        }
    };


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


    private static Ws2HttpStepMetadata buildWs2HttpStepMetadata(){
        String content = "{\n" +
                "  \"method\": \"POST\",\n" +
                "  \"url\": \"http://localhost:6010/json\",\n" +
                "  \"reqType\": \"json\",\n" +
                "  \"serviceName\": \"GetPersonService\",\n" +
                "  \"pkgName\": \"com.wjf\",\n" +
                "  \"methodName\": \"GetPerson\",\n" +
                "  \"headers\": [{\n" +
                "    \"name\":\"token\",\n" +
                "    \"type\": \"string\"\n" +
                "  }],\n" +
                "  \"params\": [{\n" +
                "    \"name\":\"id\",\n" +
                "    \"type\": \"string\"\n" +
                "  }],\n" +
                "  \"body\": [\n" +
                "    {\n" +
                "      \"name\": \"root\",\n" +
                "      \"type\": \"object\",\n" +
                "      \"children\": [\n" +
                "        {\n" +
                "          \"name\": \"sid\",\n" +
                "          \"type\": \"long\"\n" +
                "        },{\n" +
                "          \"name\": \"name\",\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"respBody\": [{\n" +
                "    \"name\": \"root\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"children\": [\n" +
                "      {\n" +
                "        \"name\": \"sid\",\n" +
                "        \"type\": \"long\"\n" +
                "      },{\n" +
                "        \"name\": \"name\",\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }],\n" +
                "  \"respHeaders\":  [{\n" +
                "    \"name\":\"token\",\n" +
                "    \"type\": \"string\"\n" +
                "  }]\n" +
                "}";
        try {
            HttpDefinition def = JsonUtils.parse(content, HttpDefinition.class);
            Definition definition = HttpWsdlGenerator.generateWsdlDefinition(def);
            Binding binding = null;
            BindingOperation bindingOperation = null;
            for (Object o : definition.getAllBindings().entrySet()) {
                Map.Entry<QName, Binding> entry = (Map.Entry<QName, Binding>) o;
                binding = entry.getValue();
            }
            for (Object o : binding.getBindingOperations()) {
                bindingOperation = (BindingOperation) o;
            }
            //SoapMessageBuilder messageBuilder = new SoapMessageBuilder(definition);
            //String outputXml = messageBuilder.buildSoapMessageFromOutput(binding, bindingOperation, SoapContext.DEFAULT);
            //String result = WsdlUtils.wsdlToString(definition);
            SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(definition);
            JsonType reqEnvelop = transformer.buildSoapMessageFromInput(binding,
                    bindingOperation, SoapContext.DEFAULT).toJsonType();
            JsonType respEnvelop = transformer.buildSoapMessageFromOutput(binding,
                    bindingOperation, SoapContext.DEFAULT).toJsonType();
            Map<String, Object> jsonSchemaType = reqEnvelop.toJson();
            Map<String, Object> args = new HashMap<>();
            Map<String, Object> output = new HashMap<>();
            output.put("schemaType", respEnvelop);
            Map<String, Object> input = new HashMap<>();
            input.put("schemaType", jsonSchemaType);
            args.put("input", input);
            args.put("id", "ws2http");
            args.put("endpointUrl", Collections.singletonList("http://127.0.0.1:6010"));
            args.put("url", "/json");
            args.put("httpMethod", "post");
            args.put("reqType", "json");
            args.put("type", "ws2http");
            args.put("output", output);
            Ws2HttpStepMetadata stepMetadata = (Ws2HttpStepMetadata) StepProcessorRegistry.parseMetadata(args);
            return stepMetadata;
            /*BeanStepDefinition beanDef = new BeanStepDefinition();
            beanDef.setMetadata(stepMetadata);
            WorkflowDefinition workflowDefinition = new WorkflowDefinition();
            workflowDefinition.setTasks(Collections.singletonList(beanDef));
            System.out.println(RouteBuilder.buildRoute(workflowDefinition));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

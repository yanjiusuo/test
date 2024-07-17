package com.jd.workflow.console.service.remote;


import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.registry.ClientRegistry;
import com.jd.jsf.gd.registry.RegistryFactory;
import com.jd.jsf.gd.util.Constants;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.jsf.parser.JsfClassParser;
import com.jd.workflow.soap.common.parser.ClassParser;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.y.jsf.HttpMckJsf;
import com.jd.y.jsf.JsfMockOpenAPI;
import com.jd.y.model.dto.JsfInterfaceOpenDTO;
import com.jd.y.model.qo.JsfMethodOpenQO;
import com.jd.y.model.vo.HttpInterfaceVO;
import com.jd.y.model.vo.JsfInterfaceOpenVO;
import com.jd.y.model.vo.JsfMethodOpenVO;
import com.jd.y.response.ReplyVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.mockito.Matchers.any;

@Slf4j
@RunWith(PowerMockRunner.class)
public class EasyMockRemoteServiceTests extends BaseTestCase {
    @InjectMocks
    EasyMockRemoteService easyMockRemoteService;
    @Mock
    private IInterfaceManageService interfaceManageService;

    JsfMockOpenAPI jsfMockOpenAPI;
    @Mock
    HttpMckJsf httpMckJsf;

    @Mock
    private IMethodManageService methodManageService;
    //全局只需要一个注册中心client
    private static ClientRegistry CLIENT_REGISTRY;
    private static RegistryConfig registryConfig;
    static {
        //参见https://cf.jd.com/pages/viewpage.action?pageId=245185287#JSF%E5%AE%A2%E6%88%B7%E7%AB%AF%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C-APPID%E5%8F%8AAPPNAME%E4%BC%A0%E9%80%92
        //一定要设置这两个字段，会进行校验；
        //不要随意填写，防止和别的应用名字冲突，这个两个字段可以通过jdos申请应用获取，即使不部署也可以申请一个空应用，用于获取appId及appName；
        JSFContext.putIfAbsent( JSFContext.KEY_APPID, "123456" );
        JSFContext.putIfAbsent( JSFContext.KEY_APPNAME, "test_app" );
        //设置jsf客户端与注册中心的心跳间隔，默认30秒；
        JSFContext.putGlobalVal(Constants.SETTING_REGISTRY_HEARTBEAT_INTERVAL, "30000");
        //设置jsf客户端从注册中心定期拉取provider列表的时间间隔，一般设置120秒，不要太快，对注册中心有压力；
        JSFContext.putGlobalVal(Constants.SETTING_REGISTRY_CHECK_INTERVAL, "120000");
        //设置provider列表可为null，用于防止jsf1.6.X及以上版本null保护
        JSFContext.putGlobalVal(Constants.SETTING_CONSUMER_PROVIDER_NULLABLE, "true");

        //注册中心配置信息，通过index寻址注册中心地址；
        List<RegistryConfig> registryConfigs = RegistryFactory.getRegistryConfigs();
        if(registryConfigs!=null && !registryConfigs.isEmpty()) {
            registryConfig = registryConfigs.get(0);
        } else {
            registryConfig = new RegistryConfig();
            registryConfig.setIndex("i.jsf.jd.com");
        }
        //仅订阅注册中心服务发现
        CLIENT_REGISTRY = RegistryFactory.getRegistry(registryConfig);
    }
    HttpMckJsf newHttpMckJsf(){
        ConsumerConfig<HttpMckJsf> consumerConfig = new ConsumerConfig<HttpMckJsf>();
        consumerConfig.setInterfaceId("com.jd.y.jsf.HttpMckJsf");
        consumerConfig.setAlias("easymock-local");
        consumerConfig.setUrl("jsf://11.50.218.96:22000");
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setProtocol("jsf");
        consumerConfig.setTimeout(30000);
        return consumerConfig.refer();
    }
    JsfMockOpenAPI newJsfMck(){
        ConsumerConfig<JsfMockOpenAPI> consumerConfig = new ConsumerConfig<JsfMockOpenAPI>();
        consumerConfig.setInterfaceId(JsfMockOpenAPI.class.getName());
        consumerConfig.setAlias("easymock-local");
        consumerConfig.setUrl("jsf://11.50.218.96:22000");
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(30000);
        return consumerConfig.refer();
    }
    @Before
    public void before(){
        //when(methodManageService.updateById(any())).thenReturn(true);
        Mockito.doAnswer(vs->{
            return true;
        }).when(methodManageService).updateById(any());
        HttpInterfaceVO httpDto = new HttpInterfaceVO();

        httpDto.setId(1);
        //when(httpMckJsf.addHttpInterface(any())).thenReturn(ReplyVO.success(httpDto));
        //when(httpMckJsf.addHttpTemplate(any())).thenReturn(ReplyVO.success(null));


        jsfMockOpenAPI = newJsfMck();
        httpMckJsf = newHttpMckJsf();
        easyMockRemoteService.setJsfMockOpenAPI(jsfMockOpenAPI);
        easyMockRemoteService.setHttpMockJsf(httpMckJsf);
        String host = JSFContext.getLocalHost();
        UserInfoInSession userInfoInSession = new UserInfoInSession();
        userInfoInSession.setUserName("wangjingfang3");
        userInfoInSession.setUserId("wangjingfang3");
        UserSessionLocal.setUser(userInfoInSession);
        //String callUrl = "jsf://"+host+":22000?alias=center";
        //consumerConfig.setUrl(callUrl);
        //httpMckJsf = consumerConfig.refer();
        //easyMockRemoteService.setHttpMockJsf(httpMckJsf);
    }
    HttpMethodModel newHttpMethodModel(){
        String content = getResourceContent("classpath:definition/http-method-model.json");
        return JsonUtils.parse(content,HttpMethodModel.class);
    }
    @Test
    public void testAddJsfInterface(){
        InterfaceManage manage = new InterfaceManage();
        manage.setName("com.jd.y.jsf.HttpMckJsf");
        manage.setPath("com.jd.y:easymock-api:1.0.3-SNAPSHOT");
        easyMockRemoteService.addOrUpdateJsfInterface(manage);
        easyMockRemoteService.openOrCloseInterface(manage,true);
        log.info("jsf_call_result={}",manage.getRelatedId());
        log.info("jsf_call_result={}",JsonUtils.toJSONString(manage));
    }

    @Test
    public void testAddEasyMockJsfInterface(){
        InterfaceManage manage = new InterfaceManage();
        manage.setName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        manage.setPath("com.jd.mpaas:mpaas-console-jsf:0.0.1-SNAPSHOT");
        easyMockRemoteService.addOrUpdateJsfInterface(manage);
        easyMockRemoteService.openOrCloseInterface(manage,true);

        log.info("jsf_call_result={}",manage.getRelatedId());
        log.info("jsf_call_result={}",JsonUtils.toJSONString(manage));
    }
    //70
    @Test
    public void testAddStd(){
        InterfaceManage manage = new InterfaceManage();
        manage.setName("com.jd.up.standardserve.api.client.service.StandServeAppInfoService");
        manage.setPath("com.jd.up.standardserve:up-standardserve-client:1.0.0-SNAPSHOT");
        easyMockRemoteService.addOrUpdateJsfInterface(manage);
        easyMockRemoteService.openOrCloseInterface(manage,true);
        log.info("jsf_call_result={}",manage.getRelatedId());
        log.info("jsf_call_result={}",JsonUtils.toJSONString(manage));
    }
    // jsfId= 69
    @Test
    public void testQueryInterface(){
        String pomInfo = " <dependency>\n" +
                "            <artifactId>com.jd.mpaas</artifactId>\n" +
                "            <groupId>mpaas-console-jsf</groupId>\n" +
                "            <version>0.0.3-SNAPSHOT</version>\n" +
                "        </dependency>";
        JsfInterfaceOpenDTO dto = new JsfInterfaceOpenDTO();
        dto.setInterfaceName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        dto.setMavenInfo(pomInfo);
        dto.setMockedAlias(EasyMockRemoteService.DEFAULT_ALIAS);
        final ReplyVO<List<JsfInterfaceOpenVO>> listReplyVO = jsfMockOpenAPI.queryInterface(dto);
        log.info("jsf_call_result={}",listReplyVO);

    }
    public void testOpenJsfInterface(){

        final ReplyVO<String> replyVO = jsfMockOpenAPI.openOrCloseInterface("com.jd.mpaas.console.jsf.service.MpaasRemoteService", EasyMockRemoteService.DEFAULT_ALIAS, true);
        log.info("result={}",JsonUtils.toJSONString(replyVO));
    }


    public void testQueryHttpInterface(){// httpId=203
        InterfaceManage interfaceManage = new InterfaceManage();
        MethodManage method = new MethodManage();
        interfaceManage.setServiceCode("wjf");
        method.setPath("/wjf");
        method.setHttpMethod("get");
        final Integer id = easyMockRemoteService.queryHttpInterface(interfaceManage,method);
        log.info("http_id={}",id);
    }
    public void testAddHttpTemplate(){
        InterfaceManage interfaceManage = new InterfaceManage();
        interfaceManage.setServiceCode("testMock");
        MethodManage methodManage = new MethodManage();
        methodManage.setName("html");
        methodManage.setRelatedId(203L);
        methodManage.setPath("/html");
        methodManage.setHttpMethod("GET");
        methodManage.setContent(JsonUtils.toJSONString(newHttpMethodModel()));
        easyMockRemoteService.addHttpTemplate(methodManage,methodManage.getRelatedId().toString());
    }
    public void testUpdateHttpTemplate(){
        InterfaceManage interfaceManage = new InterfaceManage();
        interfaceManage.setServiceCode("testMock");
        MethodManage methodManage = new MethodManage();
        methodManage.setName("html");
        methodManage.setRelatedId(203L);
        methodManage.setPath("/html");
        methodManage.setHttpMethod("GET");
        methodManage.setContent(JsonUtils.toJSONString(newHttpMethodModel()));
        easyMockRemoteService.updateHttpTemplate(methodManage,null);
    }
    public void testChangeInterface(){
        InterfaceManage manage = new InterfaceManage();
        manage.setRelatedId(69L);
        manage.setName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        manage.setPath("com.jd.mpaas:mpaas-console-jsf:0.0.3-SNAPSHOT");

        easyMockRemoteService.addOrUpdateJsfInterface(manage);

    }
    @Test
    public void testDeliverInfo(){
        InterfaceManage manage = new InterfaceManage();
        manage.setRelatedId(69L);
        manage.setPath("com.jd.mpaas:mpaas-console-jsf:0.0.3-SNAPSHOT");
        easyMockRemoteService.updateDeliverInfo(manage,null,"wff");
    }
    @Test
    public void testAddJsfMethod(){ // interfaceId=68 methodId=38
        InterfaceManage manage = new InterfaceManage();
        manage.setRelatedId(69L);
        MethodManage method = new MethodManage();
        method.setName("initApp");
        easyMockRemoteService.addJsfMethod(manage,method,"69");
    }
    public void testQueryJsfMethod(){ // interfaceId=69 methodId=42
        JsfMethodOpenQO qo = new JsfMethodOpenQO();
        qo.setInterfaceId(69);
        qo.setNameEn("initApp");
        final ReplyVO<List<JsfMethodOpenVO>> replyVo = jsfMockOpenAPI.queryMethods(qo);
        log.info("jsf.query_jsf_method:interfaceId={},methodName={},replyVo={}",null,null,JsonUtils.toJSONString(replyVo));

    }
    @Test
    public void testAddHttpInterface(){ // 204
        InterfaceManage interfaceManage = new InterfaceManage();
        interfaceManage.setServiceCode("testMock");
        MethodManage methodManage = new MethodManage();
        methodManage.setName("html");
        methodManage.setPath("/html");
        methodManage.setHttpMethod("GET");
        methodManage.setContent(JsonUtils.toJSONString(newHttpMethodModel()));
        easyMockRemoteService.addHttpMethod(interfaceManage,methodManage);

    }

    @Test
    public void testUpdateInterface(){
        InterfaceManage interfaceManage = new InterfaceManage();
        interfaceManage.setServiceCode("testMock");
        MethodManage methodManage = new MethodManage();
        methodManage.setRelatedId(204L);
        methodManage.setName("html");
        methodManage.setPath("/html");
        methodManage.setHttpMethod("GET");
        easyMockRemoteService.updateHttpMethod(interfaceManage,methodManage);
    }
    @Test
    public void testAddJsfTemplate(){
        JsfStepMetadata stepMetadata = JsfClassParser.buildMethodInfo(IPersonService.class, "test");
        InterfaceManage interfaceManage = new InterfaceManage();
        interfaceManage.setRelatedId(69L);
        MethodManage method = new MethodManage();
        method.setContent(JsonUtils.toJSONString(stepMetadata));
        interfaceManage.setName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        method.setRelatedId(42L);
        easyMockRemoteService.addJsfTemplate(interfaceManage,method,method.getRelatedId().intValue());

    }
    @Test
    public void testUpdateJsfTemplate(){

        JsfStepMetadata stepMetadata = JsfClassParser.buildMethodInfo(IPersonService.class, "test");
        InterfaceManage interfaceManage = new InterfaceManage();
        interfaceManage.setRelatedId(69L);
        MethodManage method = new MethodManage();
        method.setContent(JsonUtils.toJSONString(stepMetadata));
        interfaceManage.setName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        method.setRelatedId(42L);
        easyMockRemoteService.updateJsfTemplate(interfaceManage,method,method.getRelatedId().intValue());

    }
    static class IPersonService{
        public void test(Integer id,Person person){}
    }
    @Data
    static class Person{
        Integer id;
        String name;
    }
}

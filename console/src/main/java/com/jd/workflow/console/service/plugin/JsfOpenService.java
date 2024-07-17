package com.jd.workflow.console.service.plugin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.jd.jsf.gd.client.TelnetClient;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.jsf.open.api.ApplicationService;
import com.jd.jsf.open.api.InterfaceService;
import com.jd.jsf.open.api.ProviderAliaService;
import com.jd.jsf.open.api.ProviderService;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.BaseRequest;
import com.jd.jsf.open.api.vo.request.QueryInterfaceRequest;
import com.jd.jsf.open.api.vo.request.QueryMethodInfoRequest;
import com.jd.jsf.open.api.vo.request.QueryProviderRequest;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.entity.JsfAlias;
import com.jd.workflow.console.service.auth.InterfaceAuthService;
import com.jd.workflow.console.service.ducc.entity.HotUpdateEnvironmentConf;
import com.jd.workflow.console.service.plugin.jsf.JsfInputInfo;
import com.jd.workflow.console.service.plugin.jsf.JsfMethodCmdInfo;
import com.jd.workflow.console.service.remote.MockDataBuildService;
import com.jd.workflow.console.utils.ClassReference;
import com.jd.workflow.console.utils.JsfCmdInfoUtils;
import com.jd.workflow.console.utils.RestTemplateUtils;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.util.json.CustomModule;
import com.jd.workflow.soap.common.xml.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.jd.workflow.console.service.plugin.jsf.MethodResponse;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JsfOpenService {
    @Resource
    ProviderAliaService providerAliaService;
    @Autowired
    InterfaceService interfaceService;
    @Resource
    ProviderService providerService;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    MockDataBuildService mockDataBuildService;

    @Autowired
    HotUpdateService hotUpdateService;

    @Value("${xbp.env}")
    private String xbpEnv;

    @Resource(name = "defaultScheduledExecutor")
    ScheduledThreadPoolExecutor defaultScheduledExecutor;



    @Autowired
    private RestTemplateUtils restTemplateUtils;
    public boolean isTest(){
        return "TEST".equalsIgnoreCase(xbpEnv);
    }
    public List<String> queryAliasByInterfaceName(String interfaceName){
        List<JsfAlias> resultList = Lists.newArrayList();

        QueryInterfaceRequest req = JSFArgBuilder.buildQueryInterfaceRequest();
        req.setInterfaceName(interfaceName);
        //jsf查询到的别名
        Result<List<String>> result = providerAliaService.getAliasByInterfaceName(req);
         if(!result.isSuccess()){
             throw new BizException("获取jsf别名失败："+result.getMsg());
         }

        return result.getData();
    }
    public static <T> T parse(String text,Class<T> clazz)   {
        ExtraProcessor extraProcessor = new ExtraProcessor() {
            @Override
            public void processExtra(Object object, String key, Object value) {
                if (object instanceof JsfMethodCmdInfo) {
                    ((JsfMethodCmdInfo) object).addExtraAttr(key, value);
                }
            }
        };

        return  JSON.parseObject(text, clazz, extraProcessor, Feature.IgnoreNotMatch);
    }
    /**
     * 状态：0.无心跳，1. 正常 4. 反注册 5.逻辑删除 (注意，此状态与上下线非同一个含义，上下线特指由用户发起对改服务的操作，而此状态是服务的自身行为，如果实例与注册中心心跳不断则此状态是正常，如果与注册中心没有了心跳，那么就变成了无心跳，如果服务正常停止通过sdk会进行正常反注册)
     * @param interfaceName
     * @param alias
     * @return
     */
    public List<Server> getProviders(String interfaceName, String alias)  {

        String host = JSFContext.getLocalHost();
        //String callUrl = "jsf://"+host+":22000?alias=center";
        //consumerConfig.setUrl(callUrl);

        QueryProviderRequest req = JSFArgBuilder.buildQueryProviderRequest();
        req.setInterfaceName(interfaceName);
        req.setStatusList(Collections.singletonList(1));
        req.setAlias(alias);
        try{
            Result<List<Server>> result = providerService.query(req);
            log.info("jsf.query_provider:interfaceName={},alias={},result={}",interfaceName,alias, JsonUtils.toJSONString(result));
            if(result.isSuccess()){
                List<Server> servers =  result.getData();
                if(servers == null) servers = new ArrayList<>();
                 return servers;
            }else{
                return Collections.emptyList();
            }
        }catch (Exception e){

            throw new BizException("获取jsf提供者失败："+e.getMessage(),e);
        }

    }
    public List<Server> getValidProviders(String interfaceName,String alias){
        return getProviders(interfaceName, alias).stream().filter(item->{
            return item.getStatus() == 1;
        }).collect(Collectors.toList());

    }
    public JsfInputInfo getInputJsfInputInfos(String interfaceName, String alias, String methodName) {
        List<Server> validProviders = getValidProviders(interfaceName, alias);
        if(CollectionUtils.isEmpty(validProviders)){
            throw new BizException("没有可用的服务提供者");
        }
        List<Object> result = new ArrayList<Object>();
        Server server = validProviders.get(0);
        TelnetClient client = new TelnetClient(server.getIp(),server.getPort(),8000,8000);
        String cmd = "info " + interfaceName + " " + methodName;
        try{
            client.connect();
            // {"error":"The method [addUser] is not exists!"}
            String resultTel = client.telnetJSF("info " + interfaceName + " " + methodName);
            //                  String  resultTel = "{\"methodName\":\"queryPaymentInfoByOrderId\",\"returnType\":\"com.jd.ept.common.domain.EptRemoteResult<java.util.List<com.jd.ept.order.vo.imid.response.EptOrderPaymentVo>>\",\"EptRemoteResult\":{\"code\":\"int\",\"isSuccess\":\"boolean\",\"message\":\"java.lang.String\",\"model\":\"java.lang.Object\"},\"EptOrderPaymentVo\":{\"orderId\":\"java.lang.Long\",\"currencyBuy\":\"java.lang.String\",\"payId\":\"java.lang.String\",\"payType\":\"java.lang.String\",\"payEnum\":\"java.lang.String\",\"payTime\":\"java.util.Date\"},\"parameters\":[{\"param1\":\"java.lang.Long\"}]}";
            JsfMethodCmdInfo method = parse(resultTel, JsfMethodCmdInfo.class);
            if(StringUtils.isNotBlank(method.getError())){
                throw new BizException("获取jsf入参失败："+method.getError());
            }
            List<BuilderJsonType> builderJsonTypes = JsfCmdInfoUtils.parseJsfInfoCmdInputParam(method.getParameters());
            List<JsonType> jsonTypes = builderJsonTypes.stream().map(item -> item.toJsonType()).collect(Collectors.toList());
            JsfInputInfo jsfInputInfo = new JsfInputInfo();
            jsfInputInfo.setInputParams(jsonTypes);
            List<Object> demoValues = jsonTypes.stream().map(item -> buildDemoValue(item, false)).collect(Collectors.toList());
            jsfInputInfo.setDemoValues(demoValues);
            jsfInputInfo.setParameterTypes(builderJsonTypes.stream().map(item -> item.getClassName()).collect(Collectors.toList()));
            return jsfInputInfo;
        }catch(BizException e){
            throw e;
        }catch (Exception e){
            throw new BizException("连接服务提供者失败："+e.getMessage(),e);
        }finally {
            try{
                client.close();
            }catch (Exception e){

            }
        }
    }
    public  static Object buildDemoValue(JsonType jsonType,boolean convert){

        try {
            Object value = jsonType.toExprValue(new ValueBuilderAcceptor() {
                @Override
                public Object afterSetValue(Object value, JsonType jsonType) {
                    final Object demoValue = JsfParamConverterRegistry.buildDemoValue(jsonType);
                    if (demoValue != null) return demoValue;

                    if(value instanceof Map && jsonType instanceof ObjectJsonType){
                        if( jsonType.getClassName() != null
                                && (
                                jsonType.getClassName().equals("java.util.Object")
                                        || jsonType.getClassName().startsWith("java.util.") && jsonType.getClassName().contains("Map")
                        )
                        ){
                            return value;
                        }
                        ((Map<String, Object>) value).put("class",jsonType.getClassName());
                    }

                    return value;
                }
            });
            if(convert){
                return JsfParamConverterRegistry.convertValue(jsonType, value);
            }
            return value;
        }catch(Exception e){
            log.error("jsf.err_convert_input_value",e);
            return null;
        }
    }
        public List getInputTemplate(String interfaceName,String alias,String methodName){
            List<Server> validProviders = getValidProviders(interfaceName, alias);
            if(CollectionUtils.isEmpty(validProviders)){
              throw new BizException("没有可用的服务提供者");
            }
            List<Object> result = new ArrayList<Object>();
            Server server = validProviders.get(0);
            TelnetClient client = new TelnetClient(server.getIp(),server.getPort(),8000,8000);
            String cmd = "info " + interfaceName + " " + methodName;
            try{
                client.connect();
                String resultTel = client.telnetJSF("info " + interfaceName + " " + methodName);
                //                  String  resultTel = "{\"methodName\":\"queryPaymentInfoByOrderId\",\"returnType\":\"com.jd.ept.common.domain.EptRemoteResult<java.util.List<com.jd.ept.order.vo.imid.response.EptOrderPaymentVo>>\",\"EptRemoteResult\":{\"code\":\"int\",\"isSuccess\":\"boolean\",\"message\":\"java.lang.String\",\"model\":\"java.lang.Object\"},\"EptOrderPaymentVo\":{\"orderId\":\"java.lang.Long\",\"currencyBuy\":\"java.lang.String\",\"payId\":\"java.lang.String\",\"payType\":\"java.lang.String\",\"payEnum\":\"java.lang.String\",\"payTime\":\"java.util.Date\"},\"parameters\":[{\"param1\":\"java.lang.Long\"}]}";
                JSONObject resultTel_obj = JSON.parseObject(resultTel);
                JSONArray parameters = resultTel_obj.getJSONArray("parameters");
                for (int index = 0; index < parameters.size(); index++) {
                    JSONObject jsonObject = parameters.getJSONObject(index);
                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (key.startsWith("param")) {
                            String classStr = value.toString();
                            String[] classStr_Arr = null;
                            if (classStr.contains("<")) {
                                classStr_Arr = classStr.substring(0, classStr.indexOf("<")).split("\\.");
                            } else {
                                classStr_Arr = classStr.split("\\.");
                            }
                            String key_ = classStr_Arr[classStr_Arr.length - 1];
                            String param = JSON.toJSONString(jsonObject.getJSONObject(key_));
                            Map<String, Object> param_Map = new HashMap<String, Object>();
                            if (!param.equals("null")) {
                                param_Map = JSON.parseObject(param, new TypeReference<Map<String, Object>>() {
                                });
                                param_Map.put("class", classStr);
                                result.add(param_Map);
                            } else {
                                result.add(classStr);
                            }
                            break;
                        }
                    }
                }
                return result;
            }catch (Exception e){
                throw new BizException("连接服务提供者失败："+e.getMessage(),e);
            }finally {
                try{
                    client.close();
                }catch (Exception e){

                }
            }


    }

    public List<MethodResponse> queryJsfMethods(String interfaceName,String alias,String ipAndPort){

            interfaceName = interfaceName.trim();
            QueryMethodInfoRequest queryMethodRequest = JSFArgBuilder.buildBaseReq(QueryMethodInfoRequest.class);
            queryMethodRequest.setInterfaceName(interfaceName);
            queryMethodRequest.setAlias(alias);
            if(StringUtils.isNotBlank(ipAndPort)){
                List<String> strs = StringHelper.split(ipAndPort, ":");
                queryMethodRequest.setIp(strs.get(0));
                queryMethodRequest.setPort(Integer.valueOf(strs.get(1)));
            }
        try {
            Result<List<String>> methodResult = interfaceService.getMethodList(queryMethodRequest);
            if(methodResult.isSuccess()) {
                List<String> data = methodResult.getData();

                List<MethodResponse> methodResponses = new ArrayList<MethodResponse>();
                if (data != null) {
                    List<String> result = data.subList(1, data.size());
                    for (String tmpArr : result) {
                        String subStr = tmpArr.substring(0, tmpArr.indexOf("("));

                        String outParam = subStr.substring(0, subStr.lastIndexOf(" "));
                        String methodName = subStr.substring(subStr.lastIndexOf(" ") + 1, subStr.length());
                        String inParam = tmpArr.substring(tmpArr.indexOf("("), tmpArr.indexOf(")"));
                        inParam = inParam.substring(1, inParam.length());
                        String[] inputParam = inParam.split(", ");
                        MethodResponse methodResponse = new MethodResponse();
                        methodResponse.setMethodName(methodName);
                        if (inParam.length() > 0) {
                            methodResponse.setInputParam(inputParam);
                        }
                        methodResponse.setOutputParam(outParam);
                        methodResponses.add(methodResponse);
                    }
                }
                return methodResponses;
            }else{

                throw new BizException("获取jsf方法列表失败："+methodResult.getMsg());
            }


        } catch (Exception e) {
            throw new BizException("获取jsf方法列表失败："+e.getMessage(),e);
        }
    }

    public List<String> queryJsfAllInterface(String appCode){
        String jsfAppCode  ="jdos_"+appCode;
        BaseRequest baseRequest = JSFArgBuilder.buildBaseReq(BaseRequest.class);
        baseRequest.setData(jsfAppCode);
        Result<List<String>> result = applicationService.queryInterfaceNameByAppName(baseRequest);
        return result.getData();
    }
    public List<String> queryTestAndOnlineInterface(String appCode){
        List<String> result = new ArrayList<>();

            result.addAll(queryJsfAllInterface(appCode));

        if(isTest()){
            Future<?> future = defaultScheduledExecutor.submit(() -> {
                queryTestJsfInterfaces(appCode).forEach(item -> {
                    if (!result.contains(item)) {
                        result.add(item);
                    }
                });
                ;
            });
            try {
                future.get();
            } catch (Exception e) {
                log.error("query_test_jsf_interfaces error", e);
            }
        }

        return result;
    }
    public List<String> queryTestJsfInterfaces(String appCode){
        if(isTest()){
            return Collections.emptyList();
        }
        HotUpdateEnvironmentConf hotUpdateEnvironmentConf = hotUpdateService.getEnvByCode("test");



        //3.转发，并返回结果
        if (org.apache.commons.lang3.StringUtils.isEmpty(hotUpdateEnvironmentConf.getHost())) {

            HttpHeaders headers = new HttpHeaders();
            headers.add("Host", hotUpdateEnvironmentConf.getHost());
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = String.format("/jsf/queryAppAllJsfInterfaces?appCode="+appCode, hotUpdateEnvironmentConf.getHostIp());
            String response = restTemplateUtils.post(url, headers, JSON.toJSONString(appCode));
            log.info("query_test_jsf_interfaces response:{}", response);
            CommonResult<List<String>> result = JsonUtils.parse(response, new com.fasterxml.jackson.core.type.TypeReference<CommonResult<List<String>>>() {
            });
            return result.getData()==null?Collections.emptyList():result.getData();
        }
        return Collections.emptyList();
    }

    public static void main(String[] args) {
        String result = "{\"methodName\":\"batchMergeSkuDisplayLabel\",\"returnType\":\"com.jd.gms.component.labrador.api.response.Result<java.util.Map<com.jd.gms.component.labrador.api.domain.SkuDisplayLabel, com.jd.gms.component.labrador.api.response.Result>>\",\"Result\":{\"success\":\"boolean\",\"errorCode\":\"java.lang.String\",\"errorMessage\":\"java.lang.String\",\"obj\":\"java.lang.Object\"},\"SkuDisplayLabel\":{\"skuId\":\"java.lang.Long\",\"Terminal\":{\"site\":\"com.jd.gms.component.labrador.api.domain.Site\",\"ua\":\"com.jd.gms.component.labrador.api.domain.UserAgent\",\"subSite\":\"java.lang.String\",\"siteStr\":\"java.lang.String\",\"uaStr\":\"java.lang.String\"},\"labels\":\"java.util.Set<com.jd.gms.component.labrador.api.domain.DisplayLabel>\",\"DisplayLabel\":{\"key\":\"java.lang.String\",\"value\":\"java.lang.String\"}},\"parameters\":[{\"param1\":\"com.jd.gms.component.labrador.api.request.BatchMergeSkuDisplayLabelParam\",\"BatchMergeSkuDisplayLabelParam\":{,\"ClientInfo\":{\"uuid\":\"java.lang.String\",\"ip\":\"java.lang.String\",\"hostName\":\"java.lang.String\",\"instanceHome\":\"java.lang.String\",\"appId\":\"java.lang.String\",\"name\":\"java.lang.String\",\"sourceIp\":\"java.lang.String\",\"userAgent\":\"java.lang.String\",\"sourceIpPort\":\"java.lang.String\",\"businessIdentity\":\"java.lang.String\",\"tenantId\":\"java.lang.String\",\"headParam\":\"java.util.Map<java.lang.String, java.lang.String>\"},\"VenderInfo\":{\"venderId\":\"java.lang.String\",\"venderSource\":\"java.lang.String\",\"shopId\":\"java.lang.String\",\"supplyUnit\":\"java.lang.String\"},\"skuDisplayLabels\":\"java.util.Set<com.jd.gms.component.labrador.api.domain.SkuDisplayLabel>\",\"SkuDisplayLabel\":{\"skuId\":\"java.lang.Long\",\"Terminal\":{\"site\":\"com.jd.gms.component.labrador.api.domain.Site\",\"ua\":\"com.jd.gms.component.labrador.api.domain.UserAgent\",\"subSite\":\"java.lang.String\",\"siteStr\":\"java.lang.String\",\"uaStr\":\"java.lang.String\"},\"labels\":\"java.util.Set<com.jd.gms.component.labrador.api.domain.DisplayLabel>\",\"DisplayLabel\":{\"key\":\"java.lang.String\",\"value\":\"java.lang.String\"}}}}]}";
        Map map = parse(result, Map.class);
        JsfMethodCmdInfo method = JsonUtils.parse(JsonUtils.toJSONString(map), JsfMethodCmdInfo.class);
        System.out.println(method);
        List<BuilderJsonType> builderJsonTypes = JsfCmdInfoUtils.parseJsfInfoCmdInputParam(method.getParameters());
        List<JsonType> jsonTypes = builderJsonTypes.stream().map(item -> item.toJsonType()).collect(Collectors.toList());
        JsfInputInfo jsfInputInfo = new JsfInputInfo();
        jsfInputInfo.setInputParams(jsonTypes);
        List<Object> demoValues = jsonTypes.stream().map(item -> buildDemoValue(item, false)).collect(Collectors.toList());
        jsfInputInfo.setDemoValues(demoValues);
        jsfInputInfo.setParameterTypes(builderJsonTypes.stream().map(item -> item.getClassName()).collect(Collectors.toList()));
        System.out.println(jsfInputInfo);
    }
}

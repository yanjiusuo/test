package com.jd.workflow.jsf.example;

import com.jd.jsf.gd.GenericService;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;

import com.jd.jsf.gd.util.Constants;

import com.jd.jsf.gd.util.RpcContext;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
@Slf4j
public class JsfGenericCallDemo {
    private final static Logger logger = LoggerFactory.getLogger(JsfGenericCallDemo.class);

    /*public static Object jsonTypeToParameterObj(JsonType jsonType,Object value){
        if(jsonType == null || value == null){
            return null;
        }
        if(jsonType.isSimpleType()){
            return ((SimpleJsonType)jsonType).castValue(value);
        }else if(jsonType instanceof ObjectJsonType){
            Map map = new HashMap();
            map.put("class",jsonType.getClassName());
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                Object childValue = BeanTool.getProp(value,child.getName());
                map.put(child.getName(),jsonTypeToParameterObj(child,childValue));
            }
            return map;
        }else if(jsonType instanceof ArrayJsonType){
            ArrayJsonType arrayJsonType = (ArrayJsonType) jsonType;
            List list = (List) value;
            List ret = new ArrayList<>();
            if(arrayJsonType.getArrayItemType() != null){
                ret.addAll(list);
            }else{
                for (Object o : list) {
                    ret.add(jsonTypeToParameterObj(arrayJsonType.getChildren().get(0),o));
                }
            }
            return ret;

        }
        return null;
    }*/
    static SimpleJsonType newSimpleType(String name,String type){
        SimpleJsonType simpleJsonType = new SimpleJsonType();
        simpleJsonType.setType(SimpleParamType.from(type).typeName());
        simpleJsonType.setName(name);
        return simpleJsonType;
    }
    static ObjectJsonType newPersonType(){
        ObjectJsonType jsonType = new ObjectJsonType();
        jsonType.setClassName("com.jd.workflow.jsf.service.test.Person");
        SimpleJsonType id = newSimpleType("id","long");
        SimpleJsonType name = newSimpleType("name","string");
        SimpleJsonType age = newSimpleType("age","integer");
        jsonType.getChildren().add(id);
        jsonType.getChildren().add(name);
        jsonType.getChildren().add(age);
        return jsonType;
    }
    private static void callPersonService(){
        // 注册中心实现（必须）
        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex("test.i.jsf.jd.local"); // 测试环境192.168.150.121 i.jsf.jd.com
        logger.info("实例RegistryConfig");
        // 服务消费者连接注册中心，设置属性
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId("com.jd.demo.service.rpc.UserService");// 这里写真实的类名
        consumerConfig.setRegistry(jsfRegistry);
        consumerConfig.setProtocol("jsf");
        consumerConfig.setAlias("local-debug-local");
        consumerConfig.setParameter("proxy.enableProxy", "true");  //直接连方式 开启代理
        String callUrl = "jsf://"+"192.168.1.3"+":22000?alias=local-debug-local";//直连方式，不配置代理，本地---本地  可以调试

//      String callUrl = "jsf://"+"10.0.70.41"+":22000?alias=local-debug-local";
//      String callUrl = "jsf://"+JSFContext.getLocalHost()+":19999?alias=center";

//        String callUrl = "jsf://test-local-debug.jd.local:19999";  //vpn 无法通过泛化调用  内网可以

        consumerConfig.setUrl(callUrl);
//        consumerConfig.setParameter(".token","123456");
        //consumerConfig.setLazy(true);
        consumerConfig.setGeneric(true); // 需要指定是Generic调用true
        // consumerConfig.setAsync(true); // 如果异步
        logger.info("实例ConsumerConfig");
        // 得到泛化调用实例，此操作很重，请缓存consumerConfig或者service对象！！！！（用map或者全局变量）
        GenericService service = consumerConfig.refer();
        //   consumerConfig.setParameter("testUrlAddress",JSFContext.getLocalHost()+":22000");
        //while (true) {
        try {
            Map map = new HashMap();
//            map.put("id",1);
//            map.put("name","zhangg21genericobj");
//            map.put("age",12);
//            map.put("class","com.jd.workflow.jsf.service.test.Person");
                /*Map childMap = new HashMap();
                childMap.put("id",1);
                childMap.put("name","zhangg21genericobj");
                childMap.put("age",12);
                childMap.put("class","com.jd.workflow.jsf.service.test.Person");
                map.put("person",childMap);*/

            //Object value = jsonTypeToParameterObj(personType, map);


            // 传入方法名，参数类型，参数值
            Object result = service.$invoke("delUser", new String[]{"java.lang.String"},
                    new Object[]{"sss"});
            Map map1 = new HashMap();
            map1.put("pattern","yyyy-MM-dd");
            map1.put("class","java.text.SimpleDateFormat");
               /* Object result1 = service.$invoke("simpleDateFormat", new String[]{"java.text.SimpleDateFormat"},
                        new Object[]{map1});*/
            // 如果异步
            // ResponseFuture future = RpcContext.getContext().getFuture();
            // result = future.get();
            logger.info("result :{}", result);




        } catch (Exception e) {
            logger.error("error_call_response", e);
        }

    }
    private static void callStdService(){
        // 注册中心实现（必须）
        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex("i.jsf.jd.com"); // 测试环境192.168.150.121 i.jsf.jd.com
        logger.info("实例RegistryConfig");
        // 服务消费者连接注册中心，设置属性
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId("com.jd.up.standardserve.api.client.service.StandServeAppInfoService");// 这里写真实的类名
        consumerConfig.setRegistry(jsfRegistry);
        consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(50*1000);
        consumerConfig.setAlias("dev");
        //String callUrl = "jsf://"+JSFContext.getLocalHost()+":22000?alias=center";
        //String callUrl = "jsf://"+JSFContext.getLocalHost()+":19999?alias=dev";
        String callUrl = "jsf://test-local-debug.jd.local:19999?alias=dev";
        consumerConfig.setUrl(callUrl);
        //consumerConfig.setParameter(".token","123456");
        //consumerConfig.setLazy(true);
        consumerConfig.setGeneric(true); // 需要指定是Generic调用true
        // consumerConfig.setAsync(true); // 如果异步
        logger.info("实例ConsumerConfig");
        // 得到泛化调用实例，此操作很重，请缓存consumerConfig或者service对象！！！！（用map或者全局变量）
        GenericService service = consumerConfig.refer();
        //   consumerConfig.setParameter("testUrlAddress",JSFContext.getLocalHost()+":22000");
        //while (true) {
        try {


            // 传入方法名，参数类型，参数值
            Object result = service.$invoke("getAppInfoById", new String[]{"java.lang.Long"},
                    new Object[]{309L});

            logger.info("result :{}", result);




        } catch (Exception e) {
            logger.error("error_call_response", e);
        }

    }
    private static String invokeCore(JSFParam jsfParam){

        String interfaceName = jsfParam.getInterfaceName();
        String method = jsfParam.getMethod();
        String token =jsfParam.getToken();
        String ip = jsfParam.getIp();
        Integer port = jsfParam.getPort();
        String jsonParam = jsfParam.getJsonParam();
        //String[] paramTypes = StringUtils.split(jsfParam.getInputParamType(), Constants.JSF_PARAM_SPLIT);

        String jsonResult = null;
        try{
            //获取反射调用服务
            GenericService genericService = getJsfService(jsfParam);
            if(genericService == null){
                jsonResult = "调用失败 Error: 获取接口"+interfaceName+"服务失败";
                return jsonResult;
            }


            Map map = new HashMap();
            map.put("id",1);
            map.put("name","zhangg21genericobj");
            map.put("age",12);
            map.put("class","com.jd.workflow.jsf.service.test.Person");



            // 传入方法名，参数类型，参数值
            Object result = genericService.$invoke("save", new String[]{"com.jd.workflow.jsf.service.test.Person"},
                    new Object[]{map});



            jsonResult = JsonUtils.toJSONString(result);

        }catch (Exception e){
            log.error("invokeJSFService 调用"+interfaceName+"接口方法"+method+"失败 Error: "+e);
            jsonResult = "调用失败 Error: "+e;
            e.printStackTrace();
        }
        return jsonResult;
    }
    private static GenericService getJsfService(JSFParam jsfParam){
        String interfaceName = jsfParam.getInterfaceName();
        String alias = jsfParam.getAlias();
        String ip = jsfParam.getIp();
        Integer port = jsfParam.getPort();
        String token =jsfParam.getToken();

        //String key = interfaceName + Constants.JSF_SERVICEKEY_SPLIT + alias ;
        try{
            ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<>();

            RegistryConfig jsfRegistry = new RegistryConfig();
            jsfRegistry.setIndex("i.jsf.jd.com"); // 测试环境192.168.150.121 i.jsf.jd.com
            logger.info("实例RegistryConfig");
            // 服务消费者连接注册中心，设置属性
            consumerConfig.setInterfaceId("com.jd.workflow.jsf.service.test.IPersonService");// 这里写真实的类名
            consumerConfig.setAlias("center");
            consumerConfig.setRegistry(jsfRegistry);
           // consumerConfig.setProtocol("jsf");
                consumerConfig.setSerialization("msgpack");
                consumerConfig.setParameter(".token","123456");
            /*if(consumerConfig != null){
                if(StringUtils.isNotBlank(token)){
                    token = token.replaceAll(" ","");
                    if(token.contains("{") && token.contains("}")){
                        LinkedHashMap paramMap = JSON.parseObject(token, new TypeReference<LinkedHashMap<String, Object>>() {});
                        consumerConfig.setParameters(paramMap);
                    }else{
                        consumerConfig.setParameter(com.jd.jsf.gd.util.Constants.HIDDEN_KEY_TOKEN, token);
                        consumerConfig.setParameter(".accessToken", token); //兼容两种token
                    }
                }else{
                    consumerConfig.setParameters(new HashMap<String, String>()); //清空缓存token
                }*/
            consumerConfig.setUrl("jsf://11.159.196.121:19999");
              /*  if(StringUtils.isNotBlank(ip) && port > 0) {
                    //ip直连seturl
                    //initConsumerConfigUrl(consumerConfig,jsfParam);

                }else{
                    if("pinpoint".equals(consumerConfig.getCluster())){
                        consumerConfig.setCluster("failover");
                        consumerConfig.setUrl(null);
                        consumerConfig.unrefer(); //重置url需要解绑
                    }
                }*/
               /* if (jsfParam.getOvertime()!=null && consumerConfig.getTimeout()!=jsfParam.getOvertime()){
                    consumerConfig.setTimeout(jsfParam.getOvertime().intValue());
                    consumerConfig.unrefer();
                }
                if (jsfParam.getRetryTime()!=null && consumerConfig.getRetries()!=jsfParam.getRetryTime()){//设置重试次数
                    consumerConfig.setRetries(jsfParam.getRetryTime().intValue());
                    consumerConfig.unrefer();
                }*/

                    consumerConfig.setGeneric(true);

               // consumerConfig.setProtocol("jsf");
                log.info("jsf泛化调用refer：start");
                return consumerConfig.refer();

        }catch (Exception e){
            e.printStackTrace();
            log.error("Get GenericService Error: "+ e.getMessage());
            log.error(e.getMessage(),e);
        }
        return null;
    }
    public static void main(String[] args) {
        //callStdService();
           callPersonService();
        //}
//        invokeCore(new JSFParam());
    }


    @Data
    public static class JSFParam implements Serializable,Cloneable{


        private static final long serialVersionUID = 8537404137944756002L;
        private String interfaceName;

        private Long interfaceId;

        private String alias;

        private String ip;

        private Integer port;

        private String token;

        private String method;

        private String inputParamType;

        private String jsonParam; //json格式入参

        private Object[] objParams; //入参对象，若json格式入参存在丢参数情况，可选择入参对象

        private String serialization = null;//Constants.SERIALIZATION_HESSIAN;

        private String clientType;

        private String caseId; //用例id，用于读取jimdb里的信息

        private String caseDetailId; //用例步骤id

        private Long callType;

        private String exeErp; //执行用户erp

        private String url;//直连ip：port通过，分割

        private String serializerFeature;

        private Long overtime;  //调用超时时间

        private Long retryTime;  //失败重试次数

        //   private int ipStatus;  //状态  0 在线 1 离线



        public void setInterfaceName(String interfaceName) {
            if(interfaceName != null){
                interfaceName = interfaceName.trim();
            }
            this.interfaceName = interfaceName;
        }

        public boolean checkParam(){
            this.ip = null;
            this.port = 0;
            if(StringUtils.isEmpty(interfaceName) || StringUtils.isEmpty(alias) || StringUtils.isEmpty(method)){
                return false;
            }
            return true;
        }

        public boolean checkDirectParam(){
            if(StringUtils.isEmpty(interfaceName) || StringUtils.isEmpty(method) || StringUtils.isEmpty(alias) || StringUtils.isEmpty(ip) || port<=0){
                return false;
            }
            return true;
        }

        @Override
        public Object clone() {
            JSFParam jsfParam = null;
            try{
                jsfParam = (JSFParam)super.clone();
            }catch(CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return jsfParam;
        }

    }
}

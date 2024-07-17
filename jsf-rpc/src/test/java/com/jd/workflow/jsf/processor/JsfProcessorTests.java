package com.jd.workflow.jsf.processor;


import com.alibaba.fastjson.JSONObject;
import com.jd.jsf.gd.GenericService;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.msg.Invocation;
import com.jd.jsf.gd.msg.RequestMessage;
import com.jd.jsf.gd.protocol.JSFProtocol;
import com.jd.jsf.gd.util.Constants;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.cast.impl.PrimitiveParamConverter;
import com.jd.workflow.jsf.input.JsfOutput;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.jsf.parser.JsfClassParser;
import com.jd.workflow.jsf.service.test.*;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.parser.ClassParser;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.NetUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.ValueBuilderAcceptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
public class JsfProcessorTests extends BaseTestCase {
    ComplexTypeClass.Child newChild(){
        ComplexTypeClass.Child info = new ComplexTypeClass.Child();
        info.setIntVar(1);
        info.setShortVar((short) 2);
        info.setLongVar(1L);
        info.setFloatVar(1.33f);
        info.setDoubleVar(23.11);
        info.setCharVar('1');
        info.setBooleanVar(true);
        info.setStrVar("fsd");
        return info;
    }
    @Test
    public void testJsfEncode(){
        RequestMessage requestMessage = new RequestMessage();


        Invocation invocation = new Invocation();
        invocation.setIfaceId("com.jd.workflow.jsf.service.test.IPersonService");
        //invocation.setAlias("center");
        invocation.setArgsType(new Class[]{
           Person.class
        });
        Person person = new Person();
        person.setId(1L);

        invocation.setArgs(new Object[]{
                person
        });

        requestMessage.setInvocationBody(invocation);
        requestMessage.setMethodName("wjf");
       // requestMessage.setAlias("center");
        JSFProtocol jsfProtocol = new JSFProtocol(Constants.CodecType.json);


        final ByteBuf byteBuf = Unpooled.directBuffer();
        jsfProtocol.encode(requestMessage,byteBuf);

        final Object decode = jsfProtocol.decode(byteBuf, RequestMessage.class);
        System.out.println(ByteBufUtil.getBytes(byteBuf));
    }
    ComplexTypeClass newComplexClass(){
        try{
            ComplexTypeClass info = new ComplexTypeClass();
            info.setIntVar(1);
            info.setShortVar((short) 2);
            info.setLongVar(1L);
            info.setFloatVar(1.33f);
            info.setDoubleVar(23.11);
            info.setCharVar('1');
            info.setBooleanVar(true);
            info.setStrVar("fsd");
            info.setBigIntegerVar(new BigInteger("31"));
            info.setBigDecimalVar(new BigDecimal("23.12"));
            info.setByteVar((byte) 12);
            info.setBytesVar(new byte[]{(byte)12});
            //info.setQNameVar(new QName("http://ns","123"));
            //info.setDateTimeVar(null);
            info.setTimestamp(new Timestamp(System.currentTimeMillis()));
            info.setDate(new Date());
            info.setSqlDate(new java.sql.Date(System.currentTimeMillis()));

            ArrayList childList = new ArrayList();
            childList.add(newChild());
            info.setChildList(childList);

            HashMap<String, ComplexTypeClass.Child> childHashMap = new HashMap<>();
            childHashMap.put("child",newChild());
            info.setChildHashMap(childHashMap);

            HashMap<String,String> stringHashMap = new HashMap<>();
            stringHashMap.put("child","child");
            info.setStringHashMap(stringHashMap);

            String[] strArray = new String[]{"123"};
            info.setStrArray(strArray);
            info.setFruit(ComplexTypeClass.Fruit.apple);
            String[][] strArrArr = new String[1][1];
            strArrArr[0]=strArray;
            info.setStrArrArr(strArrArr);

            ComplexTypeClass.Child[] children  = new   ComplexTypeClass.Child[1];
            children[0] = newChild();
            info.setChildren(children);


            //info.setByteBuffer(ByteBuffer.wrap(new byte[]{(byte)12}));
            info.setCharArray(new char[]{'a','b'});
            //info.setThrowable(new NullPointerException());
            //info.setStackTraceElement(new NullPointerException().getStackTrace()[0]);
            info.setTime(new Time(System.currentTimeMillis()));
            /*info.setFile(new File("d:/tmp"));
            info.setUrl(new URL("http://jd.com"));
            info.setUri(new URI("http://jd.com"));*/
            info.setClazz(NullPointerException.class);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            info.setCalendar(calendar);
            info.setLocale(Locale.CHINESE);
            info.setTimeZone(TimeZone.getTimeZone("BST"));
            info.setUuid(UUID.randomUUID());
            //info.setStackTraceElement(new NullPointerException().getStackTrace());

            ArrayList<String> strList = new ArrayList<>();
            strList.add("dfs");
            info.setStringList(strList);
            return info;
        }catch (Exception e){
            return null;
        }

    }
    @Test
    public void testTransform() throws MalformedURLException, URISyntaxException {


        ClassParser handler = new ClassParser();
        MethodMetadata methodInfo = handler.buildMethodInfo(IPersonService.class, "test");
        List<? extends JsonType> input = methodInfo.getInput();
        //JsfParamConverterRegistry.convertValue(input.get(0),value);
        log.info("methodInfo={}", JsonUtils.toJSONString(methodInfo));

        String typeResult = JsonUtils.toJSONString(newComplexClass());
        Map map = JsonUtils.parse(typeResult, Map.class);

        Object convertValue = JsfParamConverterRegistry.convertValue(input.get(0), map);
        log.info("convertValue={}",JsonUtils.toJSONString(convertValue));
    }

    @Test
    public void testComMsg() throws MalformedURLException, URISyntaxException {


        ClassParser handler = new ClassParser();
        MethodMetadata methodInfo = handler.buildMethodInfo(IPersonService.class, "comMsg");
        List<? extends JsonType> input = methodInfo.getInput();
        //JsfParamConverterRegistry.convertValue(input.get(0),value);
        log.info("methodInfo={}", JsonUtils.toJSONString(methodInfo));

        //String typeResult = JsonUtils.toJSONString(newComMsg());
        String typeResult = "{\"id\":1,\"msg\":\"msg\",\"data\":{\"class\":\"com.jd.workflow.jsf.service.test.ComMsg\",\"json\":{\"class\":\"java.util.HashMap\",\"a\":1,\"childMap\":{\"childMap\":1}}}}";
        Map map = JsonUtils.parse(typeResult, Map.class);

        Object convertValue = JsfParamConverterRegistry.convertValue(input.get(0), map);
        log.info("convertValue={}",JsonUtils.toJSONString(convertValue));
        ConsumerConfig<GenericService> consumerConfig = newConsumerConfig();
        GenericService service = consumerConfig.refer();
        String[] paramTypes = new String[]{
                "com.jd.workflow.jsf.service.test.ReportMessage"
        };


        //Object[] paramValue = new Object[]{person};
        Object[] paramValue = new Object[]{convertValue};
        final Object result = service.$invoke("comMsg", paramTypes, paramValue);
        log.info("ret_result={}",result);
    }


    private ReportMessage newComMsg(){
        ReportMessage comMsg = new ReportMessage();
        comMsg.setId(1L);
        comMsg.setMsg("msg");

        Map<String,Object> data = new HashMap<>();
        JSONObject  json = new JSONObject(data);
        ComMsg msg = new ComMsg();
        msg.setJson(json);

        data.put("a",1);
        {
            Map<String,Object> childMap = new HashMap<>();
            childMap.put("childMap",1);
            data.put("childMap",childMap);
        }
        comMsg.setData(msg);
        return comMsg;

    }
    @Test
    public void testComplexTypeCall(){
        ClassParser handler = new ClassParser();
        MethodMetadata methodInfo = handler.buildMethodInfo(IPersonService.class, "test");
        List<? extends JsonType> input = methodInfo.getInput();

        String typeResult = JsonUtils.toJSONString(newComplexClass());
        Map map = JsonUtils.parse(typeResult, Map.class);
        Object convertValue = JsfParamConverterRegistry.convertValue(input.get(0), map);

        ConsumerConfig consumerConfig = newNonGenericConsumerConfig();
        IPersonService service = (IPersonService) consumerConfig.refer();
        ReportMessage reportMessage = service.comMsg(newComMsg());
        log.info("ret_result={}",reportMessage);
    }
    ConsumerConfig newConsumerConfig(){
        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex("i.jsf.jd.com"); // 测试环境192.168.150.121 i.jsf.jd.com
        // 服务消费者连接注册中心，设置属性
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId("com.jd.workflow.jsf.service.test.IPersonService");// 这里写真实的类名
        consumerConfig.setRegistry(jsfRegistry);
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://"+ NetUtils.getLocalHost() +":22000?alias=center");
        consumerConfig.setTimeout(50*1000);
        consumerConfig.setAlias("center");
        consumerConfig.setParameter(".token","123456");

        //consumerConfig.setLazy(true);
        consumerConfig.setGeneric(true); // 需要指定是Generic调用true
        return consumerConfig;
    }
    ConsumerConfig newNonGenericConsumerConfig(){
        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex("i.jsf.jd.com"); // 测试环境192.168.150.121 i.jsf.jd.com
        // 服务消费者连接注册中心，设置属性
        ConsumerConfig<IPersonService> consumerConfig = new ConsumerConfig<IPersonService>();
        consumerConfig.setInterfaceId("com.jd.workflow.jsf.service.test.IPersonService");// 这里写真实的类名
        consumerConfig.setRegistry(jsfRegistry);
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://"+ NetUtils.getLocalHost() +":22000?alias=center");
        consumerConfig.setTimeout(50*1000);
        consumerConfig.setAlias("center");
        consumerConfig.setParameter(".token","123456");

        //consumerConfig.setLazy(true);
        consumerConfig.setGeneric(false); // 需要指定是Generic调用true
        return consumerConfig;
    }
    @Test
    public void testGenericCall(){

        // 服务消费者连接注册中心，设置属性
        ConsumerConfig<GenericService> consumerConfig = newConsumerConfig();

        // 得到泛化调用实例，此操作很重，请缓存consumerConfig或者service对象！！！！（用map或者全局变量）
        GenericService service = consumerConfig.refer();
        String[] paramTypes = new String[]{
                "com.jd.workflow.jsf.service.test.Person"
        };
        Person person = new Person();
        person.setId(1L);
        //person.setByteBuf(ByteBuffer.wrap(new byte[]{123}));

        final Map cast = JsonUtils.parse(JsonUtils.toJSONString(person),Map.class);
        //Object[] paramValue = new Object[]{person};
        Object[] paramValue = new Object[]{cast};
        final Object result = service.$invoke("save", paramTypes, paramValue);
        log.info("ret_result={}",result);
    }
    @Test
    public void testJsfCaller(){
        ConsumerConfig<IPersonService> consumerConfig = new ConsumerConfig<IPersonService>();
        consumerConfig.setInterfaceId("com.jd.workflow.jsf.service.test.IPersonService");
        consumerConfig.setAlias("center");
        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex("i.jsf.jd.com"); // 测试环境192.168.150.121 i.jsf.jd.com
        consumerConfig.setRegistry(jsfRegistry);
        consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(10000);
        String host = JSFContext.getLocalHost();
        String callUrl = "jsf://"+host+":22000?alias=center";
        consumerConfig.setUrl(callUrl);
        IPersonService personService = consumerConfig.refer();
        ComplexTypeClass complexTypeClass = new ComplexTypeClass();
        //complexTypeClass.setQNameVar(new QName("aa","ff","dd"));
        final ComplexTypeClass response = personService.test(complexTypeClass);
        System.out.println(response);
    }
    @Test
    public void testWildtype(){
        ClassParser handler = new ClassParser();
        MethodMetadata methodInfo = handler.buildMethodInfo(IPersonService.class, "wildcardType");
        log.info("methodInfo={}", JsonUtils.toJSONString(methodInfo));
    }
    @Test
    public void testThrowableElement(){
        String str = JsonUtils.toJSONString(new NullPointerException());
        log.info("str={}",str);
    }
    @Test
    public void callJsf(){
        JsfStepMetadata methodInfo = JsfClassParser.buildMethodInfo(IPersonService.class, "save");
        log.info("methodInfo={}", JsonUtils.toJSONString(methodInfo));
        methodInfo.setAlias("center");
        methodInfo.setUrl("jsf://127.0.0.1:22000?alias=center");

        JsfProcessor processor = new JsfProcessor();
        processor.init(methodInfo);
        Step step = new Step();
        final StepContext stepContext = new StepContext();
        stepContext.setInput(new WorkflowInput());

        step.setContext(stepContext);
        processor.process(step);
        log.info("jsf_input={}",JsonUtils.toJSONString(step.getInput()));
        log.info("jsf_output={}",JsonUtils.toJSONString(step.getOutput().getBody()));
        assertEquals("{\"name\":\"fd\",\"id\":null,\"class\":\"com.jd.workflow.jsf.service.test.Person\",\"age\":123}",JsonUtils.toJSONString(step.getOutput().getBody()) );
    }

    @Test
    public void buildDemoValue(){
        JsfStepMetadata stepMetadata = JsfClassParser.buildMethodInfo(IPersonService.class, "test");
        JsonType output = stepMetadata.getOutput();
        Object o = output.toExprValue(new ValueBuilderAcceptor() {
            @Override
            public Object afterSetValue(Object value, JsonType jsonType) {
                final Object demoValue = JsfParamConverterRegistry.buildDemoValue(jsonType);
                if (demoValue != null) return demoValue;
                return value;
            }
        });
        //Object o = JsfParamConverterRegistry.buildDemoValue(output);


        log.info("outputVal={}",JsonUtils.toJSONString(o));
    }
    @Test
    public void buildJsfTask(){
        JsfStepMetadata stepMetadata = JsfClassParser.buildMethodInfo(IPersonService.class, "test");
        log.info("stepMetadata={}", JsonUtils.toJSONString(stepMetadata));
        stepMetadata.setAlias("center");
        stepMetadata.setType("jsf");
        stepMetadata.setId("jsf");
        stepMetadata.setUrl("jsf://127.0.0.1:22000?alias=center");


        Map<String,Object> definitionMap = new HashMap<>();
        definitionMap.put("tasks",Collections.singletonList(stepMetadata));


        log.info("jsfTasks={}",JsonUtils.toJSONString(definitionMap));
    }
    @Test
    public void testJsfSave() throws InvocationTargetException, IllegalAccessException {
        String content = getResourceContent("classpath:jsf/jsf-save-definition.json");
        final Map map = JsonUtils.parse(content, Map.class);
        BeanUtils.setProperty(map,"tasks.[0].url","jsf://"+NetUtils.getLocalHost()+":22000");


        WorkflowInput workflowInput = new WorkflowInput();
        final JsfOutput output = (JsfOutput) FlowTestUtils.execFlowByContent(workflowInput, JsonUtils.toJSONString(map));

        log.info("jsfTasks={}",JsonUtils.toJSONString(output));
        assertEquals("{\"name\":\"fd\",\"id\":123,\"class\":\"com.jd.workflow.jsf.service.test.Person\",\"age\":123}",JsonUtils.toJSONString(output.getBody()));
    }
    @Test
    public void testJsfScript() throws InvocationTargetException, IllegalAccessException {
        String content = getResourceContent("classpath:jsf/jsf-script.json");
        final Map map = JsonUtils.parse(content, Map.class);
        BeanUtils.setProperty(map,"tasks.[0].url","jsf://"+NetUtils.getLocalHost()+":22000");


        WorkflowInput workflowInput = new WorkflowInput();
        final JsfOutput output = (JsfOutput) FlowTestUtils.execFlowByContent(workflowInput, JsonUtils.toJSONString(map));

        log.info("jsfTasks={}",JsonUtils.toJSONString(output));
        assertEquals("{\"name\":\"fd\",\"id\":1,\"class\":\"com.jd.workflow.jsf.service.test.Person\",\"age\":123}",JsonUtils.toJSONString(output.getBody()));
    }
    @Test
    public void testJsfFallback() throws InvocationTargetException, IllegalAccessException {
        String content = getResourceContent("classpath:jsf/jsf-fallback.json");
        final Map map = JsonUtils.parse(content, Map.class);
        BeanUtils.setProperty(map,"tasks.[0].url","jsf://"+NetUtils.getLocalHost()+":22000");


        WorkflowInput workflowInput = new WorkflowInput();
        final Output output = (Output) FlowTestUtils.execFlowByContent(workflowInput, JsonUtils.toJSONString(map));

        log.info("jsfTasks={}",JsonUtils.toJSONString(output));
        assertEquals("{\"id\":2}",JsonUtils.toJSONString(output.getBody()));
    }
    @Test
    public void testJsfPreProcess() throws InvocationTargetException, IllegalAccessException {
        String content = getResourceContent("classpath:jsf/jsf-pre-process.json");
        final Map map = JsonUtils.parse(content, Map.class);
        BeanUtils.setProperty(map,"tasks.[0].url","jsf://"+NetUtils.getLocalHost()+":22000");


        WorkflowInput workflowInput = new WorkflowInput();
        final Output output = (Output) FlowTestUtils.execFlowByContent(workflowInput, JsonUtils.toJSONString(map));

        log.info("jsfTasks={}",JsonUtils.toJSONString(output));
        assertEquals("{\"name\":\"fd\",\"id\":339,\"class\":\"com.jd.workflow.jsf.service.test.Person\",\"age\":123}",JsonUtils.toJSONString(output.getBody()));
    }
    @Test
    public void testJsfTests() throws InvocationTargetException, IllegalAccessException {
        String content = getResourceContent("classpath:jsf/jsf-test-definition.json");
        final Map map = JsonUtils.parse(content, Map.class);
        final String url = "jsf://" + NetUtils.getLocalHost() + ":22000";
        BeanUtils.setProperty(map,"tasks.[0].url", url);


        WorkflowInput workflowInput = new WorkflowInput();
        final Output output = (Output) FlowTestUtils.execFlowByContent(workflowInput, JsonUtils.toJSONString(map));

        log.info("jsfTasks={}",JsonUtils.toJSONString(output));
        assertEquals("{\"name\":\"fd\",\"id\":123,\"class\":\"com.jd.workflow.jsf.service.test.Person\",\"age\":123}",JsonUtils.toJSONString(output.getBody()));

    }
    GenericService newGenericService(){
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId("com.jd.workflow.jsf.service.test.IPersonService");
        consumerConfig.setAlias("center");
        consumerConfig.setGeneric(true);
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setIndex("test.i.jsf.local");
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(10000);
        String host = JSFContext.getLocalHost();
        String callUrl = "jsf://"+host+":22000?alias=center";
        consumerConfig.setUrl(callUrl);
        consumerConfig.setParameter(".token","123456");
        GenericService personService = consumerConfig.refer();
        return personService;
    }
    @Test
    public void testJsfGenericCallSimpleType(){
        String data = "{\"name\":\"arg0\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.SimpleTypeClass\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"bytesVar\",\"type\":\"array\",\"className\":\"[B\",\"children\":[{\"name\":null,\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true}]},{\"name\":\"intsVar\",\"type\":\"array\",\"className\":\"[I\",\"children\":[{\"name\":null,\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}";
        final GenericService genericService = newGenericService();

        JsonType jsonType = JsonUtils.parse(data,JsonType.class);
        Object value = jsonType.toExprValue();
       Object result = genericService.$invoke("simpleType",new String[]{
        "com.jd.workflow.jsf.service.test.SimpleTypeClass"
        },new Object[]{value});
       log.info("result={}",result);
       assertEquals("{\"bytesVar\":\"\",\"longVar\":0,\"floatVar\":0.0,\"doubleVar\":0.0,\"intVar\":0,\"intsVar\":[],\"class\":\"com.jd.workflow.jsf.service.test.SimpleTypeClass\",\"charVar\":\"\\u0000\",\"shortVar\":0,\"booleanVar\":false}",JsonUtils.toJSONString(result));
    }
    @Test
    public void testJsfGenericCallTest(){
        String data = "{\"name\":\"arg0\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"bigIntegerVar\",\"type\":\"string\",\"className\":\"java.math.BigInteger\"},{\"name\":\"bigDecimalVar\",\"type\":\"string\",\"className\":\"java.math.BigDecimal\"},{\"name\":\"byteVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"timestamp\",\"type\":\"long\",\"className\":\"java.sql.Timestamp\"},{\"name\":\"date\",\"type\":\"string\",\"className\":\"java.util.Date\"},{\"name\":\"sqlDate\",\"type\":\"string\",\"className\":\"java.sql.Date\"},{\"name\":\"bytesVar\",\"type\":\"array\",\"className\":\"[B\",\"children\":[{\"name\":null,\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true}]},{\"name\":\"childList\",\"type\":\"array\",\"genericTypes\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}],\"className\":\"java.util.ArrayList\",\"children\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}]},{\"name\":\"childHashMap\",\"type\":\"object\",\"allowEdit\":true,\"genericTypes\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}],\"className\":\"java.util.HashMap\",\"children\":[]},{\"name\":\"stringList\",\"type\":\"array\",\"genericTypes\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}],\"className\":\"java.util.ArrayList\",\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"stringHashMap\",\"type\":\"object\",\"allowEdit\":true,\"genericTypes\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}],\"className\":\"java.util.HashMap\",\"children\":[]},{\"name\":\"strArray\",\"type\":\"array\",\"className\":\"[Ljava.lang.String;\",\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"strArrArr\",\"type\":\"array\",\"className\":\"[[Ljava.lang.String;\",\"children\":[{\"name\":null,\"type\":\"array\",\"className\":null,\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}]}]},{\"name\":\"children\",\"type\":\"array\",\"className\":\"[Lcom.jd.workflow.jsf.service.test.ComplexTypeClass$Child;\",\"children\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}]},{\"name\":\"childrenChildren\",\"type\":\"array\",\"className\":\"[[Lcom.jd.workflow.jsf.service.test.ComplexTypeClass$Child;\",\"children\":[{\"name\":null,\"type\":\"array\",\"className\":null,\"children\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}]}]},{\"name\":\"child\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]},{\"name\":\"typeChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass\",\"children\":[]},{\"name\":\"charArray\",\"type\":\"array\",\"className\":\"[C\",\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true}]},{\"name\":\"time\",\"type\":\"object\",\"className\":\"java.sql.Time\",\"children\":[]},{\"name\":\"file\",\"type\":\"object\",\"className\":\"java.io.File\",\"children\":[{\"name\":\"path\",\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"url\",\"type\":\"string\",\"className\":\"java.net.URL\"},{\"name\":\"uri\",\"type\":\"string\",\"className\":\"java.net.URI\"},{\"name\":\"clazz\",\"type\":\"string\",\"className\":\"java.lang.Class\"},{\"name\":\"calendar\",\"type\":\"string\",\"className\":\"java.util.Calendar\"},{\"name\":\"locale\",\"type\":\"string\",\"className\":\"java.util.Locale\"},{\"name\":\"timeZone\",\"type\":\"string\",\"className\":\"java.util.TimeZone\"},{\"name\":\"uuid\",\"type\":\"string\",\"className\":\"java.util.UUID\"},{\"name\":\"fruit\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"interfaceTypeEnum\",\"type\":\"string\",\"className\":\"java.lang.String\"}]}";
        final GenericService genericService = newGenericService();

        JsonType jsonType = JsonUtils.parse(data,JsonType.class);
        Object value = jsonType.toExprValue();
        final Object convertValue = JsfParamConverterRegistry.convertValue(jsonType, value);
        Object result = genericService.$invoke("test",new String[]{
                "com.jd.workflow.jsf.service.test.ComplexTypeClass"
        },new Object[]{convertValue});
        log.info("result={}",result);
        assertEquals("{\"date\":null,\"bytesVar\":\"\",\"longVar\":0,\"stringHashMap\":{},\"fruit\":null,\"intVar\":0,\"childList\":[{\"subChild\":{\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\"},\"longVar\":0,\"strVar\":null,\"floatVar\":0.0,\"doubleVar\":0.0,\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"charVar\":\"\\u0000\",\"shortVar\":0,\"booleanVar\":false}],\"locale\":null,\"interfaceTypeEnum\":null,\"uuid\":null,\"sqlDate\":null,\"byteVar\":0,\"file\":null,\"children\":[{\"subChild\":{\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\"},\"longVar\":0,\"strVar\":null,\"floatVar\":0.0,\"doubleVar\":0.0,\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"charVar\":\"\\u0000\",\"shortVar\":0,\"booleanVar\":false}],\"charArray\":\"\",\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass\",\"shortVar\":0,\"timestamp\":null,\"calendar\":null,\"bigDecimalVar\":null,\"strArray\":[],\"strVar\":null,\"strArrArr\":[[]],\"floatVar\":0.0,\"doubleVar\":0.0,\"typeChild\":{\"date\":null,\"bytesVar\":null,\"longVar\":0,\"stringHashMap\":null,\"fruit\":null,\"intVar\":0,\"childList\":null,\"locale\":null,\"interfaceTypeEnum\":null,\"uuid\":null,\"sqlDate\":null,\"byteVar\":0,\"file\":null,\"children\":null,\"charArray\":null,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass\",\"shortVar\":0,\"timestamp\":null,\"calendar\":null,\"bigDecimalVar\":null,\"strArray\":null,\"strVar\":null,\"strArrArr\":null,\"floatVar\":0.0,\"doubleVar\":0.0,\"typeChild\":null,\"throwable\":null,\"timeZone\":null,\"childHashMap\":null,\"uri\":null,\"url\":null,\"bigIntegerVar\":null,\"booleanVar\":false,\"childrenChildren\":null,\"stringList\":null,\"time\":null,\"charVar\":\"\\u0000\",\"clazz\":null,\"child\":null},\"throwable\":null,\"timeZone\":null,\"childHashMap\":{},\"uri\":null,\"url\":null,\"bigIntegerVar\":null,\"booleanVar\":false,\"childrenChildren\":[[{\"subChild\":{\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\"},\"longVar\":0,\"strVar\":null,\"floatVar\":0.0,\"doubleVar\":0.0,\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"charVar\":\"\\u0000\",\"shortVar\":0,\"booleanVar\":false}]],\"stringList\":[],\"time\":null,\"charVar\":\"\\u0000\",\"clazz\":null,\"child\":{\"subChild\":{\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\"},\"longVar\":0,\"strVar\":null,\"floatVar\":0.0,\"doubleVar\":0.0,\"intVar\":0,\"class\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"charVar\":\"\\u0000\",\"shortVar\":0,\"booleanVar\":false}}",JsonUtils.toJSONString(result));
    }
    @Test
    public void buildComplexValue(){
        String data = "{\"name\":\"arg0\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"bigIntegerVar\",\"type\":\"string\",\"className\":\"java.math.BigInteger\"},{\"name\":\"bigDecimalVar\",\"type\":\"string\",\"className\":\"java.math.BigDecimal\"},{\"name\":\"byteVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"timestamp\",\"type\":\"long\",\"className\":\"java.sql.Timestamp\"},{\"name\":\"date\",\"type\":\"string\",\"className\":\"java.util.Date\"},{\"name\":\"sqlDate\",\"type\":\"string\",\"className\":\"java.sql.Date\"},{\"name\":\"bytesVar\",\"type\":\"array\",\"className\":\"[B\",\"children\":[{\"name\":null,\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true}]},{\"name\":\"childList\",\"type\":\"array\",\"genericTypes\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}],\"className\":\"java.util.ArrayList\",\"children\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}]},{\"name\":\"childHashMap\",\"type\":\"object\",\"allowEdit\":true,\"genericTypes\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}],\"className\":\"java.util.HashMap\",\"children\":[]},{\"name\":\"stringList\",\"type\":\"array\",\"genericTypes\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}],\"className\":\"java.util.ArrayList\",\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"stringHashMap\",\"type\":\"object\",\"allowEdit\":true,\"genericTypes\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}],\"className\":\"java.util.HashMap\",\"children\":[]},{\"name\":\"strArray\",\"type\":\"array\",\"className\":\"[Ljava.lang.String;\",\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"strArrArr\",\"type\":\"array\",\"className\":\"[[Ljava.lang.String;\",\"children\":[{\"name\":null,\"type\":\"array\",\"className\":null,\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.String\"}]}]},{\"name\":\"children\",\"type\":\"array\",\"className\":\"[Lcom.jd.workflow.jsf.service.test.ComplexTypeClass$Child;\",\"children\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}]},{\"name\":\"childrenChildren\",\"type\":\"array\",\"className\":\"[[Lcom.jd.workflow.jsf.service.test.ComplexTypeClass$Child;\",\"children\":[{\"name\":null,\"type\":\"array\",\"className\":null,\"children\":[{\"name\":null,\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]}]}]},{\"name\":\"child\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$Child\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true},{\"name\":\"shortVar\",\"type\":\"integer\",\"className\":\"java.lang.Byte\",\"required\":true},{\"name\":\"longVar\",\"type\":\"long\",\"className\":\"java.lang.Long\",\"required\":true},{\"name\":\"floatVar\",\"type\":\"float\",\"className\":\"java.lang.Float\",\"required\":true},{\"name\":\"doubleVar\",\"type\":\"float\",\"className\":\"java.lang.Double\",\"required\":true},{\"name\":\"charVar\",\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true},{\"name\":\"booleanVar\",\"type\":\"boolean\",\"className\":\"java.lang.Boolean\",\"required\":true},{\"name\":\"strVar\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"subChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass$SubChild\",\"children\":[{\"name\":\"intVar\",\"type\":\"integer\",\"className\":\"java.lang.Integer\",\"required\":true}]}]},{\"name\":\"typeChild\",\"type\":\"object\",\"className\":\"com.jd.workflow.jsf.service.test.ComplexTypeClass\",\"children\":[]},{\"name\":\"charArray\",\"type\":\"array\",\"className\":\"[C\",\"children\":[{\"name\":null,\"type\":\"string\",\"className\":\"java.lang.Character\",\"required\":true}]},{\"name\":\"time\",\"type\":\"object\",\"className\":\"java.sql.Time\",\"children\":[]},{\"name\":\"file\",\"type\":\"object\",\"className\":\"java.io.File\",\"children\":[{\"name\":\"path\",\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"url\",\"type\":\"string\",\"className\":\"java.net.URL\"},{\"name\":\"uri\",\"type\":\"string\",\"className\":\"java.net.URI\"},{\"name\":\"clazz\",\"type\":\"string\",\"className\":\"java.lang.Class\"},{\"name\":\"calendar\",\"type\":\"string\",\"className\":\"java.util.Calendar\"},{\"name\":\"locale\",\"type\":\"string\",\"className\":\"java.util.Locale\"},{\"name\":\"timeZone\",\"type\":\"string\",\"className\":\"java.util.TimeZone\"},{\"name\":\"uuid\",\"type\":\"string\",\"className\":\"java.util.UUID\"},{\"name\":\"fruit\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"interfaceTypeEnum\",\"type\":\"string\",\"className\":\"java.lang.String\"}]}";
        JsonType jsonType = JsonUtils.parse(data,JsonType.class);
        Object value = jsonType.toExprValue(new ValueBuilderAcceptor() {
            @Override
            public Object afterSetValue(Object value, JsonType jsonType) {
                final Object demoValue = buildDemoValue(jsonType);
                if(demoValue != null) return demoValue;
                return value;
            }
        });
        System.out.println(JsonUtils.toJSONString(value));
    }
    Object buildDemoValue(JsonType jsonType){
        if(jsonType instanceof SimpleJsonType){
            final JsfParamConverter converter = JsfParamConverterRegistry.getConverter(jsonType);
            if(converter instanceof PrimitiveParamConverter){
                try{
                    return converter.getDemoValue(Class.forName(jsonType.getClassName()));
                }catch (Exception e){
                    log.error("jsf.err_build_demo_value:class={}",jsonType.getClassName(),e);
                }
            }
        }
        return null;
    }

 /*   public static void main(String[] args) {
        ComplexTypeClass info = new ComplexTypeClass();
        info.setIntVar(1);
        info.setShortVar((short) 2);
        info.setLongVar(1L);
        info.setStrVar("fsd");
        ArrayList<String> strList = new ArrayList<>();
        info.setStringList(strList);
        String result = JsonUtils.toJSONString(info, new com.jd.fastjson.serializer.SerializerFeature[0]);
        System.out.println(result);

    }*/
}

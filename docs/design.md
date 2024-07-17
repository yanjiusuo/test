# wsdl
webservice通过xml传输报文，通过wsdl里的描述生成输入及输出报文，举例说明：
```xml
<?xml version='1.0' encoding='UTF-8'?><wsdl:definitions xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:tns="http://service.workflow.jd.com/" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:ns1="http://schemas.xmlsoap.org/soap/http" name="FullTypedWebServiceService" targetNamespace="http://service.workflow.jd.com/">
  <!-- types用来定义类型，java里的类型在此定义 -->
  <wsdl:types>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tns="http://service.workflow.jd.com/" elementFormDefault="unqualified" targetNamespace="http://service.workflow.jd.com/" version="1.0">

  <xs:element name="test" type="tns:test"/>

  <xs:element name="testResponse" type="tns:testResponse"/>

  <xs:complexType name="test">
    <xs:sequence>
      <xs:element minOccurs="0" name="arg0" type="tns:person"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="person">
    <xs:sequence>
      <xs:element minOccurs="0" name="id" type="xs:string"/>
      <xs:element minOccurs="0" name="name" type="xs:string"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="testResponse">
    <xs:sequence>
      <xs:element minOccurs="0" name="return" type="xs:string"/>
    </xs:sequence>
  </xs:complexType>

</xs:schema>
  </wsdl:types>
  <!-- 消息用来定义入参、出参对象 -->
  <wsdl:message name="test">
    <wsdl:part element="tns:test" name="parameters">
    </wsdl:part>
  </wsdl:message>
  <wsdl:message name="testResponse">
    <wsdl:part element="tns:testResponse" name="parameters">
    </wsdl:part>
  </wsdl:message>
  <!-- portType用来定义接口信息 -->
  <wsdl:portType name="FullTypedWebService">
    <wsdl:operation name="test">
      <wsdl:input message="tns:test" name="test">
    </wsdl:input>
      <wsdl:output message="tns:testResponse" name="testResponse">
    </wsdl:output>
    </wsdl:operation>
  </wsdl:portType>
  <!-- binding用来定义接口与传输协议的绑定 -->
  <wsdl:binding name="FullTypedWebServiceServiceSoapBinding" type="tns:FullTypedWebService">
    <!-- style规定了入参格式，可以为document、rpc
		transport为传输协议，可以为http、smtp、ftp等
	-->
    <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
    <wsdl:operation name="test">
      <soap:operation soapAction="" style="document"/>
      <wsdl:input name="test">
        <soap:body use="literal"/>
      </wsdl:input>
      <wsdl:output name="testResponse">
        <soap:body use="literal"/>
      </wsdl:output>
    </wsdl:operation>
  </wsdl:binding>
  <!-- service用来指定调用地址 --> 
  <wsdl:service name="FullTypedWebServiceService">
    <wsdl:port binding="tns:FullTypedWebServiceServiceSoapBinding" name="FullTypedWebServicePort">
      <soap:address location="http://127.0.0.1:7001/FullTypedWebService"/>
    </wsdl:port>
  </wsdl:service>
</wsdl:definitions>
```
根据上面wsdl可以解析出输入结构为：
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.workflow.jd.com/">
<soapenv:Header/>
<soapenv:Body>
  <ser:test>
	 <!--Optional:-->
	 <arg0>
		<!--Optional:-->
		<id>?</id>
		<!--Optional:-->
		<name>?</name>
	 </arg0>
  </ser:test>
</soapenv:Body>
</soapenv:Envelope>
```
最复杂的是xml schema，它用来描述节点类型，使用xml schema 描述节点类型有哪些缺点？
1. xml schema解析比较复杂，里面包含的要素过多
2. wsdl管理接口的描述分散在wsdl:operation、wsdl:message、wsdl:types3个节点里，如果想要将某一个接口的描述单独抽离出来比较复杂(接口编排的时候就是需要生成对应格式的报文传输才可以)
3. 需要有一个新的描述来替代这些描述，并且可以很方便的解析、存储，并且可以根据这个描述生成入参、出参信息。

# 解决方案-自己规定描述
从使用的角度上，json无疑比xml更加灵活、简单，主要体现在
1. json只支持array、object、simpleType等一些简单类型
2. json不支持引用：xml支持引用，单增加引用无疑会增加解析的复杂性

因此考虑用json描述来替代xml ，来解决xml描述接口比较复杂、不方便解析的问题。
从本质上来说，wsdl最重要描述的是input报文、output报文，因此只要能用json描述好这个xml结构即可。

可以参考json-schema,规定这种描述方式：
```
{
  type:"object|array|integer|...",
  name:'xxx',
  attrs:{},// xml属性
  children:'',// 子节点
  "prefix":"",// xml前缀
}
```
与之对应，上面的输入报文可以描述为：
```json
{"name":"Envelope","namespacePrefix":"soapenv","attrs":{"xmlns:soapenv":"http://schemas.xmlsoap.org/soap/envelope/","xmlns:ser":"http://service.workflow.jd.com/"},"type":"object","children":[{"name":"Body","namespacePrefix":"soapenv","type":"object","children":[{"name":"test","namespacePrefix":"ser","type":"object","children":[{"name":"arg0","type":"object","children":[{"name":"id","type":"string"},{"name":"name","type":"string"}]}]}]},{"name":"Header","namespacePrefix":"soapenv","attrs":{"xmlns:soapenv":"http://schemas.xmlsoap.org/soap/envelope/"},"type":"object","children":[]}]}
```
这样描述的话，可以在编排的时候脱离wsdl且只根据该描述就可以生成输入报文、输出报文


# webservice转http
本质上是协议转换， `json input -> xml soap input -> 请求webservice服务 -> xml soap response ->  json response`   


```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.workflow.jd.com/">
<soapenv:Header/>
<soapenv:Body>
  <ser:test>
	 <!--Optional:-->
	 <arg0>
		<!--Optional:-->
		<id>?</id>
		<!--Optional:-->
		<name>?</name>
	 </arg0>
  </ser:test>
</soapenv:Body>
</soapenv:Envelope>
```
比如，针对上面的xml描述，对应的json可以转换为:
```json
{
  Header:{},
  Body:{
    test:{
	  "arg0":{
	     "id":1,
		 "name":"2"
	  }
	}
  }
}
```

如果用户通过http接口输入json信息，就可以根据这些信息加上 json描述来生成xml

# http转webservice
webservice本质是对java接口的描述，http接口可以抽象为：
```
public interface HttpService{
   HttpResponse call(HttpBody,HttpParam,HttpHeaders);
}
```
只需要想办法将这个方法转换为wsdl描述即可。

协议转换过程`xml input -> json input -> 请求http接口 -> json response -> xml output`

可以参考cxf，动态生成java类，然后将java类转换为wsdl

# 代码参考
1. json描述相关：JsonType、SimpleJsonType、ObjectJsonType、ArrayJsonType、JsonTypeUtils，用来描述json信息.
2. XNode : 用来描述xml 节点
3. SoapOperationToJsonTransformer 用来将wsdl报文转换为json描述，buildSoapMessageFromInput用来构造输入报文的JsonType描述、buildSoapMessageFromOutput用来构造输出报文的JsonType描述
4. JsonTypeUtils：JsonType工具类
5. SoapUtils: soap报文工具类，soapXmlToJson方法将soap报文转换为json
6. HttpWsdlGenerator#generateWsdl(HttpDefinition definition) 用来将http定义转换为wsdl描述
7. SoapMessageBuilder：用来构造webservice示例报文，buildSoapMessageFromInput 用来构造输入报文、buildSoapMessageFromOutput用来构造输出报文

## 单元测试
1. webservice解析 & 示例报文构造：TestSampleUtil
2. SoapInputToJsonTests : 根据wsdl生成 JsonType描述测试
3. TestHttpWsdlGenerator ： http接口生成wsdl描述单元测试
4. SoapInputToJsonTests ： soap报文转换为json单元测试
5. WebServiceProcessorTests 将webservice转换为http单元测试


# http接口步骤要素
## 接口管理存储数据结构
```json
{
  "type":"http",
  "input":{
    "method":"GET|POST|OPTIONS|...",
	"url":"/user/{type}/{id}",    // {id}表示路径参数，需要在path属性里体现
	"path":Array<SimpleJsonType>, // path参数里只有url里存在占位符的参数，如{type}
	 params:Array<SimpleJsonType>,
  	 headers:Array<SimpleJsonType>,
	 reqType:"form|json",
	 body:[{// type为json的时候：
	      name:"root" // name固定为root
		  type:'array|object',
		  children:Array<JsonType>
	 }],
	 body:[{// type为form的时候：
	      name:"key1"
		  type:'array|object|string',
		  children:Array<JsonType> // 只有当type为object或者array的时候才有children
	 }]
  },
  successCondition:"response.code==0",// 本次请求成功的条件，是mvel表达式
  output:{
    headers:Array<SimpleJsonType>,
	body:{
	  name:"root" // name固定为root
	  type:'array|object',
	  children:Array<JsonType>
	}
  }
}
```
path、params、body、headers均使用JsonType存储，分为SimpleJsonType、ObjectJsonType和ArrayJsonType

SimpleJsonType的结构示例如下：
```json
{name:"sid",type:"integer|long|double|string|boolean",required:"true|false",desc:null,value:"xxx"}
```
ObjectJsonType的类型如下：
```json
{name:"user",type:"object",required:"true|false",children:Array<JsonType>,value:"xxx"}
```
ArrayJsonType的类型如下：

```json
{name:"user",type:"array",required:"true|false",children:Array<JsonType>,value:"xxx"}
```
注意事项：
1. value属性只有接口编排的时候才会用

## 接口编排存储数据结构
接口编排大体结构同接口管理，只不过JsonType额外增加了value属性,
比如：

```json
{
    "id": "step1",
    "type": "http",
    "input": {// 同接口管理，增加了preProcess、script属性，用来加工数据
		preProcess:'',预处理,mvel脚本
		script:'' //脚本
	}, 
    "output": { // 同接口管理
      "headers": {},
      "response": { // 增加了script属性，用来加工数据
	    script:'' //脚本
	  }
    },
	successCondition:'response.code==0',//成功条件
	"taskDef":{ // 任务定义
	  timeout:10000,// 超时时间,ms
	  fallback:stop|continue,// 超时时间,ms
	  fallbackContent:'',//失败后的返回内容，json字符串
	}
}
```

# webservice
## 接口管理后台存储
这块后端自动生成结构信息，结构类似http
```json
{
  "type":"webservice",
   methodNames:"", //
  "input":{
     demoXml:"<soap><soap:header/><soap:body/></soap>", // 生成的示例的soap xml信息
	 schemaTypes:{ 
	    headers:Array<SimpleJsonType>,// 对应的头信息描述文件,由SchemaTypeToJsonType转换
		body:Array<JsonType>
	     
	 }
  },
  successCondition:'',//执行成功的条件
  "output":{
    demoXml:"<soap><soap:header/><soap:body/></soap>",// 生成的示例的soap xml信息
	schemaTypes:{
	   headers:Array<SimpleJsonType>,// 对应的schemaTypes信息
	   body:Array<JsonType>
	}
  }
}
```
## 接口编排后台存储
接口编排时没有demoXml信息了，
```json
{
  "type":"webservice",
   methodNames:"", 
  "input":{
	 preValidate:'', // 校验信息
     script:"", // 用来做数据加工的脚本
	 schemaTypes:{ 
	    headers:Array<SimpleJsonType>,// 对应的头信息描述文件,由SchemaTypeToJsonType转换
		body:Array<JsonType>
	     
	 }
  },
  "output":{
    script:'',
	schemaTypes:{
	   headers:Array<SimpleJsonType>,// 对应的schemaTypes信息
	   body:Array<JsonType>
	}
  }
}
```

# choose步骤
```json
{
  type:"choose",
  children:[
   {
     when:'',//表达式
	 children:Array<Step> // 匹配后执行此步骤
   },
   {
     when:'',//表达式
	 children:Array<Step> // 匹配后执行此步骤
   },
   { // 默认执行的逻辑
     otherwise:'',
	 children:Array<Step> 
   }
  ]
}
```
# aggregate
```
{
  type:"aggregate",
  output:{},//聚合步骤的输出
  script:'',// 聚合步骤用来聚合的脚本
  children:Array<Step>
}
```




# 工作流
 工作流定义通过将不同步骤聚合到一起，形成工作流。
 结构定义如下：
 ```json
{
   input:{// 定义输入结构
	headers:List<JsonType>,
    "body": List<JsonType>
   },
   output:{ // 定义输入结构，同http接口管理
	headers:xx,
	body:xx
   },
   failOutput:{// 执行失败后的输出结果，同http接口管理
    headers:xx,
	body:xx
   },
   tasks:Array<Step>,//任务定义
   taskDef:{
    timeout:10000// 整体任务超时时间
   }
}
```
基本概念：
* Step:执行步骤信息，有输入和输出
* Context ：执行上下文

# webservice解析示例
TestSampleUtil#testCreateInputMsg

# schemaType转json
SchemaTypeToJsonType#createJsonType


单元测试：WsdlSchemaTypeToJsonTests

# http转webservice
需要将soap报文转换为http请求，并把http响应转换为soap响应。
转换规则：
http头对应soap头。
Response call(body,params,headers,path)
response分为header & body

# 项目结构：
console: 控制台
flow-server-web: 利哥提供的线上服务
common-service: console和flow-server-web公用的一些类
flow: 流程编排、将json转camel xml，参数校验
  核心类： 
     WorkflowParser： 流程解析
     RouteBuilder： camel xml构造
     TransformUtils : mvel里可以用的数据加工工具函数
      
  演示示例单元测试示例：
     CamelStepRouteTests#testHttpDemo1  对应并行步骤
     CamelStepRouteTests#testHttpDemo2  数据加工
     CamelStepRouteTests#testHttpDemo3  选择步骤
     CamelStepRouteTests#testHttpDemo4， 负责数据加工
     HttpProcessorTests:http步骤单测
     WebServiceProcessorTests: http转webservice单测
     Ws2HttpProcessorTests: webservice转http单测
     
examples: 一些示例，没啥用
jsf-rpc:jsf步骤开发，这块是飞哥做的
soap-common: 通用工具类
soap-client : soap调用
wsdl-builder:根据http接口信息生成wsdl描述
  核心类：HttpWsdlGenerator，仿照cxf写的，里面还有一些cxf的代码 
  单元测试： TestHttpWsdlGenerator
soap-builder: 根据wsdl生成 xml示例调用描述 以及json描述
   核心类：SoapMessageBuilder、SoapOperationToJsonTransformer
   SoapMessageBuilder是从soap-ui工具类拷贝下来的，SoapOperationToJsonTransformer是仿照SoapMessageBuilder写的
   单元测试： TestSampleUtil

# 异常处理的基本原则：
1. 需要知道哪些步骤出现的异常
2. 异常信息要能够收集到
3. 分为校验异常和执行异常，校验异常不可恢复，执行异常可以恢复
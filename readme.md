# 快速入门关键代码
* console: 控制台
* flow-server-web: 线上发布服务
* common-service: console和flow-server-web公用的一些类
* flow: 流程编排、将json转camel xml，参数校验   
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
     
* examples: 一些示例，没啥用
* jsf-rpc:jsf步骤开发，这块是飞哥做的
* soap-common: 通用工具类
* soap-client : soap调用   
* wsdl-builder:根据http接口信息生成wsdl描述
  核心类：HttpWsdlGenerator，仿照cxf写的，里面还有一些cxf的代码 
  单元测试： TestHttpWsdlGenerator
* soap-builder: 根据wsdl生成 xml示例调用描述 以及json描述   
   核心类：SoapMessageBuilder、SoapOperationToJsonTransformer   
   SoapMessageBuilder是从soap-ui工具类拷贝下来的，SoapOperationToJsonTransformer是仿照SoapMessageBuilder写的    
   单元测试： TestSampleUtil   




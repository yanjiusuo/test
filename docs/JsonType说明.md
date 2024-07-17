现在前端属性存储采用json描述，类似json schema，格式为：
```json5
// 简单字段类型
{"type":"string|integer|double|...",desc:"字段描述",required:true|false,name:"xxx",...}
// 对象字段类型。对象类型对应json里的对象，有children节点
{"type":"object","desc":"xx",name:"xx",children:[],...}
// 数组或者容器类型。children只有一个节点的时候，认为是List<T>类型。有多个子节点时，认为是数组类型
{"type":"array","desc":"xx",name:"xx",children:[],...}
```

在java类里面对应类为分别为：SimpleJsonType、ObjectJsonType、ArrayJsonType

他们的基类是JsonType。

JsonType想要加一个属性，步骤如下：
1. 在JsonType里面加一个属性，比如：prop1
2. 给prop1加上getter和setter方法
3. 在JsonType#toJson()方法里加上prop1的序列化代码
4. 在JsonType#cloneTo方法里加上prop1的克隆代码

怎么确定改属性已经被添加成功了呢？
1. 构造一个json，包含prop1属性，如：
```json
{"type":"string","prop1":"prop1"}
```
使用**JsonUtils.parse(text,JsonType.class)**代码解析，如果没有异常，说明序列化正常。

2. 将步骤1解析的JsonType对象序列化，通过JsonUtils.toJSONString(obj)方法，如果序列化的结果里有prop1，则说明序列化正常

3. 通过BuilderJsonType.from(JsonType).toJsonType()方法，将步骤1构造的对象转换为BuilderJsonType对象，然后再转换为JsonType,如果没有问题，则说明clone方法正常
4. 若prop1属性需要计算差量，需要在DeltaHelper.needDeltaAttrs里加上改属性。

注意：需要计算差量指的是自动上报的接口文档在前端页面可以编辑，编辑完成后，需要将用户编辑的属性差量存下来。若prop1可以在前端编辑，则需要在DeltaHelper里加上该属性
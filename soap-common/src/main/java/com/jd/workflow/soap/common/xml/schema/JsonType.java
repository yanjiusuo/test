package com.jd.workflow.soap.common.xml.schema;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.ColorGatewayEnumDTO;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
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
 */
/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,property = "type",visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ObjectJsonType.class, name = "object"),
        @JsonSubTypes.Type(value = ArrayJsonType.class, name = "array"),
        @JsonSubTypes.Type(value = SimpleJsonType.class ,names = {
                "long","double","string","float","integer",
                "boolean"
        }),
})*/
@JsonDeserialize(using = JsonTypeDeserializer.class)
@JsonSerialize(using = JsonTypeSerializer.class)
@ApiModel(value = "JsonType类型", description = "JsonType类型")
public abstract class JsonType {
    public static final QName XSI_TYPE = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");
    public static final String XSI_TYPE_FULL_NAME = XSI_TYPE.getNamespaceURI() + ":" + XSI_TYPE.getLocalPart();
    /**
     * 字段描述
     */
    String desc;
    /**
     * 字段名称
     */
    String name;
    /**
     * http转换为webservice使用，由于java变量不能有-，需要将_分割符转换为驼峰，比如http的有一些字段，比如：content-type变成了contentType，需要记录下原始字段的名称
     */
    @ApiModelProperty(hidden = true)
    String rawName;
    /**
     * mock属性，前端录入的时候为mock值.以@开头的为mock规则
     */
    String mock;
    /**
     * 是否隐藏
     */
    Boolean hidden;

    Constraint constraint;

    /**
     * 手必填
     */
    boolean required;
    /**
     * 默认值以及mock属性。前端录入的时候为mock值.以@开头的为mock规则
     * 转换的时候使用此属性。前端录入的时候为mock值.以@开头的为mock规则
     */
    Object value;


    /**
     * 泛型类型，java类型需要有泛型类型，
     */
    @ApiModelProperty(hidden = true)
    List<JsonType> genericTypes;
    /**
     * 当是脚本的时候，需要编译值
     */
    @ApiModelProperty(hidden = true)
    Object compiledValue;
    /**
     * @hidden
     */
    @ApiModelProperty(hidden = true)
    String namespacePrefix;
    /**
     * 类型变量名称。泛型类型时，需要将此信息存放下来，比如CommonResult<T>的data字段是
     *
     * @hidden
     */
    @ApiModelProperty(hidden = true)
    String typeVariableName;
    /**
     * 实际类型名
     */
    String className;

    ExprType exprType;
    @ApiModelProperty(hidden = true)
    BuilderJsonType parent;
    /**
     * json转xml的时候使用，xml attrs主要是 name
     *
     * @hidden
     */
    Map<String, String> attrs = Collections.emptyMap();
    /**
     * 有些不是属性的值可以放到这个地方: json 序列化的话会被展平
     */
    @ApiModelProperty(hidden = true)
    Map<String, Object> extAttrs = new LinkedHashMap<>();


    JsonEnumDTO childEnum;


    Long enumId;
    /**
     * 参数来源
     */

    ColorGatewayEnumDTO source;
    /**
     * 分类信息
     */
    ColorGatewayEnumDTO mark;

    /**
     * 是否传递 {"pre":{"name":"网关生成","value":"1"}}
     */
    Map<String,ColorGatewayEnumDTO> isTransparent;
    /**
     * 是否必填
     */
    ColorGatewayEnumDTO isAppNecessary;
    /**
     * 0 两个环境都有，1线上环境关注 2 预发环境关注
     */
    Integer dataZone;
    /**
     * color网关参数类型   1-requestHeader 2-requestParam 3-responseHeader'
     */
    Integer colorType;

    /**
     * 默认展示 1=展示  快捷调用 下拉列表使用
     */
    Integer defaultShow;

    public Integer getDefaultShow() {
        return defaultShow;
    }

    public void setDefaultShow(Integer defaultShow) {
        this.defaultShow = defaultShow;
    }

    public Integer getColorType() {
        return colorType;
    }

    public void setColorType(Integer colorType) {
        this.colorType = colorType;
    }

    public ColorGatewayEnumDTO getSource() {
        return source;
    }

    public void setSource(ColorGatewayEnumDTO source) {
        this.source = source;
    }

    public Map<String, ColorGatewayEnumDTO> getIsTransparent() {
        return isTransparent;
    }

    public void setIsTransparent(Map<String, ColorGatewayEnumDTO> isTransparent) {
        this.isTransparent = isTransparent;
    }

    public ColorGatewayEnumDTO getIsAppNecessary() {
        return isAppNecessary;
    }

    public void setIsAppNecessary(ColorGatewayEnumDTO isAppNecessary) {
        this.isAppNecessary = isAppNecessary;
    }

    public ColorGatewayEnumDTO getMark() {
        return mark;
    }

    public void setMark(ColorGatewayEnumDTO mark) {
        this.mark = mark;
    }

    public Integer getDataZone() {
        return dataZone;
    }

    public void setDataZone(Integer dataZone) {
        this.dataZone = dataZone;
    }

    /**
     * 子节点是否隐藏
     */
    Boolean childIsHidden;
    /**
     * 父类型
      */
    List<String> parentTypeName;

    public Long getEnumId() {
        return enumId;
    }

    public void setEnumId(Long enumId) {
        this.enumId = enumId;
    }

    public JsonEnumDTO getChildEnum() {
        return childEnum;
    }

    public void setChildEnum(JsonEnumDTO childEnum) {
        this.childEnum = childEnum;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public Boolean getChildIsHidden() {
        return childIsHidden;
    }

    public void setChildIsHidden(Boolean childIsHidden) {
        this.childIsHidden = childIsHidden;
    }

    /**
     * 字段类型
     * 复杂类型有array和object，array和object可以有children节点
     * 简单类型有： long、double、string、float、file、integer、boolean
     *
     * @return
     */
    public abstract String getType();

    public BuilderJsonType getParent() {
        return parent;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }


    public void setTypeVariableName(String typeVariableName) {
        this.typeVariableName = typeVariableName;
    }

    public String getTypeVariableName() {
        return typeVariableName;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public String getMock() {
        return mock;
    }

    public void setMock(String mock) {
        this.mock = mock;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    @ApiModelProperty(hidden = true)
    public String getFullTagName() {
        if (!StringUtils.isEmpty(namespacePrefix)) return namespacePrefix + ":" + name;
        return name;
    }

    @ApiModelProperty(hidden = true)
    public String getRawNameDefaultName() {
        if (StringUtils.isNotBlank(this.getRawName())) return this.getRawName();
        return this.getName();
    }

    public Object getCompiledValue() {
        return compiledValue;
    }

    public void setCompiledValue(Object compiledValue) {
        this.compiledValue = compiledValue;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ExprType getExprType() {
        return exprType;
    }

    public void setExprType(ExprType exprType) {
        this.exprType = exprType;
    }

    public void addAttributeTo(XNode node) {
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            node.attr(entry.getKey(), entry.getValue());
        }
    }

    public List<String> getParentTypeName() {
        return parentTypeName;
    }

    public void setParentTypeName(List<String> parentTypeName) {
        this.parentTypeName = parentTypeName;
    }

    /**
     * @return
     * @hidden
     */
    @ApiModelProperty(hidden = true)
    public String getXmlns() {
        String fullTagName = getFullTagName();
        if(StringUtils.isEmpty(fullTagName)){
            return null;
        }
        if (fullTagName.indexOf(":") == -1) { // xmlns
            return attrs.get("xmlns");
        } else {
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                if (entry.getKey().equals("xmlns:" + fullTagName.substring(0, fullTagName.indexOf(":")))) {
                    return entry.getValue();
                }
            }

        }
        return null;
    }

    public String findXmlnsForPrefix(String prefix) {
        String attrName = "xmlns";
        if (StringUtils.isNotBlank(prefix)) {
            attrName = attrName + ":" + prefix;
        }

        return attrs.get(attrName);
    }

    public String getXmlnsPrefixForUri(String namespaceURI) {

        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            if ("xmlns".equals(entry.getKey()) && namespaceURI.equals(entry.getValue())) {
                return "";
            }
            if (entry.getKey().startsWith("xmlns:")
                    && entry.getValue().equals(namespaceURI)
            ) {
                return entry.getKey().substring("xmlns:".length());
            }
        }
        return null;
    }

    public void setXmlns(String namespace) {
        addXmlAttr("xmlns", namespace);
    }

    public void setParent(BuilderJsonType parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(hidden = true)
    public abstract boolean isSimpleType();

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    public void addXmlAttr(QName key, String value) {
        assert key != null;
        String fullName = key.getLocalPart();
        if (!StringUtils.isEmpty(key.getNamespaceURI())) {
            fullName = key.getNamespaceURI() + ":" + key.getLocalPart();
        }

        addXmlAttr(fullName, value);
    }

    public void addXmlAttr(String key, String value) {
        assert key != null;
        if (Collections.emptyMap().equals(this.attrs)) {
            this.attrs = new LinkedHashMap<>();
        }
        if (value == null) {
            value = "";
        }
        attrs.put(key, value);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isRequired() {
        return required;
    }

    /**
     * 根据type生成json描述
     * 如
     * {
     * "id": "String",
     * "name":"String",
     * "age": "Integer",
     * "tags": ["String"],
     * "friends": [{
     * "name": "String",
     * "id": "Long",
     * "Sub": {
     * "id": "Integer"
     * }
     * }],
     * "arrArr": [["String"]]
     * }
     *
     * @return
     */
    public abstract Object toDescJson();

    /**
     根据value，生成json
     {
     "id": "1",
     "name":"1",
     "age": 1,
     "tags": ["1"],
     "friends": [{
     "name": "1",
     "id": 2,
     "Sub": {
     "id": 1
     }
     }],
     "arrArr": [["1"]]
     * @return
     */
    //public abstract Object toJsonValue();

    /**
     * jsonType需要做值映射，因此需要将jsonType转换为map或者array表达式类型，并借助ParameterUtils去做参数映射
     * <p>
     * 根据exprValue构造jsonValue，当表达式不为空，就设置表达式的值
     *
     * @return
     */
    public abstract Object toExprValue(ValueBuilderAcceptor acceptor);

    /**
     * 获取value对应的exprValue
     *
     * @return
     */
    public Object toExprValue() {

        return toExprValue(null);
    }

    /**
     * @return
     * @hidden
     */
    @ApiModelProperty(hidden = true)
    public abstract Class getTypeClass();

    public Object getExprValue() {

        if (ObjectHelper.isEmpty(this.value)) return null;
        if (ExprType.script.equals(exprType)) {
            return this.compiledValue;
        }
        return this.value;
    }


    public void setRequired(boolean required) {
        this.required = required;
    }


    /**
     * 构造示例xml
     *
     * @return
     */
    public XNode toDemoXmlNode() {
        Object value = toExprValue(new ValueBuilderAcceptor() {
            @Override
            public Object afterSetValue(Object value, JsonType jsonType) {
                if (jsonType instanceof SimpleJsonType) {
                    return jsonType.getType();
                }
                return value;
            }
        });
        return transformToXml(value, new XmlBuilderAcceptor() {
            @Override
            public void beforeBuildNode(XNode node, JsonType jsonType) {
                if (jsonType instanceof SimpleJsonType) {
                    node.content(jsonType.getType());
                }
            }

            @Override
            public void afterBuildNode(XNode node, JsonType jsonType) {
                if (jsonType.isRequired()) {
                    node.attr("required", jsonType.isRequired() + "");
                }
                if (jsonType instanceof SimpleJsonType) {
                    node.content(jsonType.getType());
                } else {
                    node.attr("type", jsonType.getType());
                }
            }
        }).get(0);
    }

    public List<XNode> transformToXml() throws ToXmlTransformException {
        return transformToXml(null);
    }

    public List<XNode> transformToXml(XmlBuilderAcceptor acceptor) throws ToXmlTransformException {
        Object exprValue = toExprValue();

        return transformToXml(exprValue, acceptor);
    }

    public List<XNode> transformToXml(Object inputValue) throws ToXmlTransformException {

        return transformToXml(inputValue, null);
    }

    public List<XNode> transformToXml(Object inputValue, XmlBuilderAcceptor acceptor) throws ToXmlTransformException {
        List<String> currentLevel = new ArrayList<>();
        XNode root = XNode.make("root");
      /*  XNode current = XNode.make(getFullTagName());
        addAttributeTo(current);
        root.appendChild(current);*/
        transformToXml(root, inputValue, currentLevel, acceptor);
        return root.getChildren();
    }

    public abstract void transformToXml(XNode parent, Object inputValue, List<String> currentLevel, XmlBuilderAcceptor acceptor) throws ToXmlTransformException;


    public void mergeValue(Map<String, Object> args) {
        String exprType = (String) args.get("exprType");
        setExprType(ExprType.valueOf(exprType));
        setValue(args.get("value"));
    }

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.name);
        map.put("type", this.getType());
        if (!StringUtils.isEmpty(this.desc)) {
            map.put("desc", this.desc);
        }
        if(constraint != null){
            map.put("constraint",constraint);
        }

        if (StringUtils.isNotBlank(mock)) {
            map.put("mock", mock);
        }
        if (hidden != null) {
            map.put("hidden", hidden);
        }
        if (!ObjectHelper.isEmpty(this.value)) {
            map.put("value", this.value);
        }
        if (!ObjectHelper.isEmpty(this.typeVariableName)) {
            map.put("typeVariableName", this.typeVariableName);
        }
        if (!extAttrs.isEmpty()) {
            map.putAll(extAttrs);
        }
        if (!StringUtils.isEmpty(this.namespacePrefix)) {
            map.put("namespacePrefix", this.namespacePrefix);
        }
        if (!CollectionUtils.isEmpty(this.genericTypes)) {
            map.put("genericTypes", this.genericTypes);
        }
        //if(!StringUtils.isEmpty(this.className)){
        map.put("className", this.className);
        //}
        if (exprType != null) {
            map.put("exprType", exprType.name());
        }
        if (!this.attrs.isEmpty()) {
            map.put("attrs", this.attrs);
        }
        if (!StringUtils.isEmpty(rawName)) {
            map.put("rawName", rawName);
        }
        if (required) {
            map.put("required", this.required);
        }
        if (Objects.nonNull(childEnum)) {
            map.put("childEnum", childEnum);
        }
        if (Objects.nonNull(enumId)) {
            map.put("enumId", enumId);
        }
        if (Objects.nonNull(childIsHidden)) {
            map.put("childIsHidden", childIsHidden);
        }
        if (Objects.nonNull(parentTypeName)) {
            map.put("parentTypeName", parentTypeName);
        }



        if (Objects.nonNull(source)) {
            map.put("source", source);
        }
        if (Objects.nonNull(mark)) {
            map.put("mark", mark);
        }
        if (Objects.nonNull(isTransparent)) {
            map.put("isTransparent", isTransparent);
        }
        if (Objects.nonNull(isAppNecessary)) {
            map.put("isAppNecessary", isAppNecessary);
        }
        if (Objects.nonNull(dataZone)) {
            map.put("dataZone", dataZone);
        }
        if (Objects.nonNull(colorType)) {
            map.put("colorType", colorType);
        }
        if (Objects.nonNull(defaultShow)) {
            map.put("defaultShow", defaultShow);
        }
        return map;
    }

    public static JsonType newEntity(String type){
        if("array".equalsIgnoreCase(type)){
            return new ArrayJsonType();
        }else  if("object".equalsIgnoreCase(type)){
            return new ObjectJsonType();
        }else if("ref".equals(type)){
            return new RefObjectJsonType();
        }else if("map".equals(type)){
            return new MapJsonType();
        }else if("string_json".equalsIgnoreCase(type) || "string_xml".equalsIgnoreCase(type)
        ){
            StringJsonType arrType = new StringJsonType();
            arrType.setType(type);
            return arrType;
        } else {
            SimpleJsonType jsonType = new SimpleJsonType();
            jsonType.setType(type);
            return jsonType;
        }
    }

    public abstract JsonType newEntity();

    public JsonType clone() {
        JsonType jsonType = newEntity();
        this.cloneTo(jsonType);
        return jsonType;
    }

    public void cloneTo(JsonType jsonType) {
        jsonType.name = this.name;
        jsonType.value = value;
        jsonType.mock = mock;
        jsonType.hidden = hidden;
        jsonType.compiledValue = this.compiledValue;
        jsonType.className = this.className;
        jsonType.rawName = this.rawName;
        jsonType.exprType = this.exprType;
        jsonType.required = this.required;
        jsonType.namespacePrefix = this.namespacePrefix;
        jsonType.desc = this.desc;
        jsonType.typeVariableName = this.typeVariableName;
        if (this.genericTypes != null) {
            jsonType.genericTypes = new ArrayList<>(this.genericTypes);
        }
        if(constraint != null){
            jsonType.constraint = constraint.clone();
        }
        jsonType.extAttrs = new LinkedHashMap<>(extAttrs);
        jsonType.exprType = this.exprType;
        if (
                SimpleParamType.from(getType()) != null
        ) {
            if (jsonType instanceof SimpleJsonType) {
                ((SimpleJsonType) jsonType).setType(this.getType());
            }
        }

        jsonType.attrs = new LinkedHashMap<>(this.getAttrs());
        if (Objects.nonNull(this.childEnum)) {
            JsonEnumDTO jsonEnumDTO = new JsonEnumDTO();
            jsonEnumDTO.setName(this.childEnum.getName());
            jsonEnumDTO.setType(this.childEnum.getType());
            if (!ObjectHelper.isEmpty(this.childEnum.getProps())) {
                List<JsonEnumPropDTO> props = Lists.newArrayList();
                for (JsonEnumPropDTO prop : this.childEnum.getProps()) {
                    JsonEnumPropDTO copy = new JsonEnumPropDTO();
                    BeanUtils.copyProperties(prop, copy);
                    props.add(copy);
                }
                jsonEnumDTO.setProps(props);

            }
            jsonType.setChildEnum(jsonEnumDTO);

        }
        jsonType.enumId = this.enumId;
        jsonType.childIsHidden=this.childIsHidden;
        jsonType.parentTypeName=this.parentTypeName;

        jsonType.source = this.source;
        jsonType.mark = this.mark;
        jsonType.isTransparent = this.isTransparent;
        jsonType.isAppNecessary = this.isAppNecessary;
        jsonType.dataZone = this.dataZone;
        jsonType.colorType = this.colorType;
        jsonType.defaultShow = this.defaultShow;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtAttrs() {
        return extAttrs;
    }

    @JsonAnySetter
    public void setExtAttrs(Map<String, Object> extAttrs) {
        this.extAttrs = extAttrs;
    }

    public List<JsonType> getGenericTypes() {
        return genericTypes;
    }

    public void setGenericTypes(List<JsonType> genericTypes) {
        this.genericTypes = genericTypes;
    }

    protected ExprTreeNode buildCurrentExprNode(ExprTreeNode parent) {
        ExprTreeNode current = new ExprTreeNode();
        current.setLabel(this.getName());
        current.setType(this.getType());
        current.setExpr(parent.getExpr() + "." + name);
        parent.addChild(current);
        return current;
    }

    /**
     * 构造表达式树，用来做快速映射.
     * 比如
     * person(object)
     * id(string)
     * name(string)
     * 可以映射  person
     * person.name
     * person.id
     *
     * @param parent
     */
    public abstract void buildExprNode(ExprTreeNode parent);

    /**
     * 类型转换：
     * value用来做类型转换。比如输入是object，但是value是string类型，则需要将值调用toString方法转换好
     * 亦或者值是字符串类型，然后当前类型是object类型，也需要将json字符串解析为object类型
     *
     * @param value
     * @return
     */
    public Object castValue(Object value) {
        if (value == null) return value;
        return castValue(value, new ArrayList<>());
    }

    protected abstract Object castValue(Object value, List<String> currentLevels);

    @Override
    public String toString() {
        return "JsonType{type=" + getType() + "," +
                "name='" + name + '\'' +
                '}';
    }

    public static void main(String[] args) {
        ObjectJsonType jsonType = new ObjectJsonType();
        jsonType.getExtAttrs().put("isMap", true);
        final String data = JsonUtils.toJSONString(jsonType);
        final JsonType type = JsonUtils.parse(data, JsonType.class);
        System.out.println(data);
    }
}

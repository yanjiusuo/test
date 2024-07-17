package com.jd.workflow.soap.common.xml;


import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.exception.JsonTypeParseError;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.type.IStringType;
import com.jd.workflow.soap.common.type.JsonStringArray;
import com.jd.workflow.soap.common.type.JsonStringObject;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class JsonTypeUtils {
    /**
     * 解析json type
     * @param props
     * @return
     * @throws JsonTypeParseError
     */
    public static JsonType from(Map<String, Object> props) throws JsonTypeParseError {
      List<String> currentLevels = new LinkedList<>();
      return from(props,currentLevels);
    }
    /**
     * 解析json type
     * @param list
     * @return
     * @throws JsonTypeParseError
     */
    public static List<JsonType> from(List<Map<String, Object>> list) throws JsonTypeParseError {
       if(list == null){
           return Collections.emptyList();
       }
        return list.stream().map(vs->{
            return from(vs);
        }).collect(Collectors.toList());

    }

    public  static List<SimpleJsonType> parseSimpleList(List<Map<String, Object>> list) throws JsonTypeParseError {
        List<JsonType> jsonTypes = from(list);
        List<SimpleJsonType> simpleJsonTypes = new ArrayList<>();
        for (JsonType jsonType : jsonTypes) {
            if(jsonType.isSimpleType()){
               throw new JsonTypeParseError("jsontype.err_only_simple_type_is_allowed")
                        .param("level", jsonType.getName());
            }
            simpleJsonTypes.add((SimpleJsonType) jsonType);
        }
        return  simpleJsonTypes;
    }

    /**
     * 将xml转换为json。需要注意的是，由于类型不确定，转换可能会出现一点问题
     * @return
     */
    public static Object xmlNodeToJson(Node node){
        if(node == null ) return null;
        return XmlUtils.nodeToMap(node);
    }
    public static List xmlNodeToJson(List<Node> nodes){
        List result = new ArrayList<>();
        for (Node node : nodes) {
            result.add(xmlNodeToJson(node));
        }
        return result;
    }
    static boolean isTextNode(Node element){
        if( element instanceof Comment
         || element instanceof Text
         ){
            return true;
        }

        return false;
    }
    static List<Node> nodeListToListNode(NodeList nodeList){
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if(isTextNode(item)){
                continue;
            }
            list.add(item);
        }
        return list;
    }
    /**
     * 将xml节点转换为json
     * @param
     * @param jsonType
     */
    public static Object xmlNodeToJson(List<Node> nodes, JsonType jsonType){
        if(nodes.isEmpty()) return null;
        if(jsonType instanceof ObjectJsonType){
            Map<String,Object> map = new LinkedHashMap<>();
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                List<Node> childNodes = childByTag(nodes.get(0), child.getName());
                map.put(child.getName(), xmlNodeToJson(childNodes,child));
            }
            return map;
        }else if(jsonType instanceof ArrayJsonType){
            JsonType childType = ((ArrayJsonType) jsonType).getChildren().get(0);
            JsonType clone = null;
            if(childType instanceof ArrayJsonType){
                List result = new ArrayList();
                for (Node node : nodes) {
                    List<Node> childNodes = nodeListToListNode(node.getChildNodes());
                    result.add(xmlNodeToJson(childNodes,childType));
                }
                return result;
            }else{
                clone = childType.clone();
                clone.setName(jsonType.getName());
                clone.setNamespacePrefix(jsonType.getName());
                clone.setAttrs(jsonType.getAttrs());
                List result = new ArrayList();

                for (Node node : nodes) {
                    result.add(xmlNodeToJson(Collections.singletonList(node),clone));
                }
                return result;
            }


        }else if(jsonType instanceof StringJsonType){
            Node node = nodes.get(0);
            String value = node.getTextContent();
            if(StringUtils.isEmpty(value)){
                return null;
            }
            Object retValue = jsonType.castValue(value);
            if(retValue instanceof IStringType){
                retValue = ((IStringType)retValue).internalValue();
            }
            return retValue;
        }else{
            Node node = nodes.get(0);
            String value = node.getTextContent();
            if(StringUtils.isEmpty(value)){
                return null;
            }
            SimpleJsonType simpleJsonType = (SimpleJsonType) jsonType;
            return simpleJsonType.castTypeValue(value);
        }
    }
    private static List<Node> childByTag(Node node,String tagName){
        List<Node> children = new ArrayList<>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if(tagName.equals(item.getLocalName())){
                children.add(item);
            }
        }
        return children;
    }

    private static void initAttrs(Map<String,Object> props,JsonType jsonType, List<String> currentLevel){
        String name = Variant.valueOf(props.get("name")).toString();
        Object value = props.get("value");
        String className = (String) props.get("className");
        String valueType = (String) props.get("exprType");
        String desc = Variant.valueOf(props.get("desc")).toString();
        Map<String,String> attrs = (Map<String, String>) props.get("attrs");
        String namespacePrefix = Variant.valueOf(props.get("namespacePrefix")).toString();
        Boolean required = Variant.valueOf(props.get("required")).toBool(false);
        if(StringUtils.isEmpty(name)){
            throw new JsonTypeParseError("jsontype.err_miss_name_prop")
                    .param("level", StringUtils.join(currentLevel," > "));
        }
        if(attrs!= null && !attrs.isEmpty()){
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                jsonType.addXmlAttr(entry.getKey(),entry.getValue());
            }
        }
        jsonType.setClassName(className);
        if(!StringUtils.isEmpty(valueType)){
            ExprType exprType = ExprType.valueOf(valueType);
            if(exprType  == null){
                throw new JsonTypeParseError("jsontype.err_unknown_value_type")
                        .param("exprType",valueType)
                        .param("level",StringUtils.join(currentLevel," > "));
            }
            jsonType.setExprType(exprType);
        }

        jsonType.setName(name);
        jsonType.setValue(value);
        jsonType.setDesc(desc);
        jsonType.setNamespacePrefix(namespacePrefix);
        jsonType.setRequired(required);
        List<String> extAttrs = Arrays.asList("name","desc","required","namespacePrefix","value","attrs","children");
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            if(!extAttrs.contains(entry.getKey())){
                jsonType.getExtAttrs().put(entry.getKey(), entry.getValue());
            }
        }
    }
    private static JsonType from(Map<String, Object> props, List<String> currentLevel) throws JsonTypeParseError {
        if(props == null || props.isEmpty()){
            throw new JsonTypeParseError("jsontype.err_props_not_allow_empty")
                    .param("level", StringUtils.join(currentLevel," > "));
        }
        Object obj = props.get("type");
        if (ObjectHelper.isEmpty(obj) || !(obj instanceof String)) {
            throw new JsonTypeParseError("jsontype.err_type_not_allow_empty")
                    .param("type",obj)
                    .param("level",StringUtils.join(currentLevel," > "));
        }
        String type = (String) obj;
        if(type.equals("object")){
            ObjectJsonType objectJsonType = new ObjectJsonType();
            initAttrs(props,objectJsonType,currentLevel);
            List<Map<String,Object>> children = (List<Map<String, Object>>) props.get("children");
            for (Map<String, Object> child : children) {
                currentLevel.add((String) child.get("name"));
                objectJsonType.addChild(from(child,currentLevel));
                currentLevel.remove(currentLevel.size()-1);
            }
            return objectJsonType;
        }else if(type.equals("array")){
            ArrayJsonType arrayJsonType = new ArrayJsonType();
            initAttrs(props,arrayJsonType,currentLevel);
            List<Map<String,Object>> children = (List<Map<String, Object>>) props.get("children");
            for (Map<String, Object> child : children) {
                currentLevel.add("[]."+(String) child.get("name"));
                arrayJsonType.addChild(from(child,currentLevel));
                currentLevel.remove(currentLevel.size()-1);
            }
            return arrayJsonType;
        }else {
            SimpleJsonType jsonType = new SimpleJsonType();
            String typeName = Variant.valueOf(props.get("type")).toString("");
            if(SimpleParamType.from(typeName) == null){
                throw new JsonTypeParseError("jsontype.err_invalid_type")
                        .param("type",typeName)
                        .param("level", StringUtils.join(currentLevel," > "));
            }
            jsonType.setType(typeName);
            initAttrs(props,jsonType,currentLevel);
            return jsonType;
        }
    }
    public static void traverse(JsonType jsonType, BiFunction<JsonType,List<String>,Object> callback){
        if(jsonType == null){
            return ;
        }
        List<String> levels = new LinkedList<>();
        callback.apply(jsonType,levels);
        if(jsonType instanceof ObjectJsonType){
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                levels.add(child.getName());
                traverse(child,callback);
                levels.remove(levels.size()-1);
            }
        }else if(jsonType instanceof ArrayJsonType){
            for (JsonType child : ((ArrayJsonType) jsonType).getChildren()) {
                levels.add("[]."+child.getName());
                traverse(child,callback);
                levels.remove(levels.size()-1);
            }
        }
    }



    public static JsonType get(JsonType jsonType,String... levels){
        if(levels == null || levels.length == 0) return jsonType;
        if(jsonType instanceof ObjectJsonType){
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                if(child.getName().equals(levels[0])){
                    return get(child,Arrays.copyOfRange(levels,1,levels.length));
                }
            }
        }else if(jsonType instanceof ArrayJsonType){
            int index = Integer.valueOf( levels[0]);
            JsonType child = ((ArrayJsonType) jsonType).getChildren().get(index);
            return get(child,Arrays.copyOfRange(levels,1,levels.length));
        }/*else if(jsonType instanceof SimpleJsonType){ levels不为空，说明当前节点是底层节点了啊
            return jsonType;
        }*/
        return null;
    }

    /**
     将
     {
         id:"Long",
         name:"String",
         children:[{
            name:"String"
        }]
     }
     这种类型的数据解析为jsonType
     * @return
     */
    public static List<JsonType> jsonStringTypeToJson(Map<String,Object> desc){
        List ret = new ArrayList();
        for (Map.Entry<String,Object> entry : desc.entrySet()) {
            ret.add(toJsonType(entry.getValue(),entry.getKey()));
        }

        return ret;
    }
   private static JsonType toJsonType(Object value,String name){

        if(value instanceof Map){
            ObjectJsonType jsonType = new ObjectJsonType();
            jsonType.setName(name);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                jsonType.addChild(toJsonType(entry.getValue(),(String)entry.getKey()));
            }
            return jsonType;
        }else if(value instanceof String){
            SimpleJsonType jsonType = new SimpleJsonType();
            SimpleParamType paramType = SimpleParamType.from((String) value);
            if(paramType == null){
                throw new StdException("miss_type").param("value",value);
            }
            jsonType.setType(paramType);
            jsonType.setName(name);
            return jsonType;
        }else  if(value instanceof Collection){
            ArrayJsonType jsonType = new ArrayJsonType();
            jsonType.setName(name);
            for(Object o : (Collection)value){
                jsonType.addChild(toJsonType(o,null));
            }
            return jsonType;
        }
        return null;
    }

    public static void mergeSimpleValue(List<JsonType> jsonTypes,List<SimpleJsonType> list){
        for (int i = 0; i < jsonTypes.size(); i++) {
            JsonType childType = jsonTypes.get(i);
            mergeValue(childType,list.get(i));
        }
    }
    public static void mergeValue(List<JsonType> jsonTypes,List<JsonType> list){
        for (int i = 0; i < jsonTypes.size(); i++) {
            JsonType childType = jsonTypes.get(i);
            mergeValue(childType,list.get(i));
        }
    }
    public static void mergeValue(JsonType jsonType,JsonType args){

        jsonType.setValue(args.getValue());
        jsonType.setExprType(args.getExprType());

        List<JsonType> children = Collections.emptyList();
        List<JsonType> argChild = Collections.emptyList();

        if(jsonType instanceof ArrayJsonType){
            children = ((ArrayJsonType) jsonType).getChildren();
            argChild = ((ArrayJsonType) args).getChildren();

        }else if(jsonType instanceof ObjectJsonType){
            children = ((ObjectJsonType) jsonType).getChildren();
            argChild = ((ObjectJsonType) args).getChildren();
        }

        int index = 0;
        for (JsonType child : children) {
            mergeValue(child,argChild.get(index));
            index++;
        }

    }

    /**
     * 获取 soap报文的操作名称节点,获取的就是GetPerson
     *  比如 ：
     *    Envelop
     *      Body
     *        GetPerson
     *
     * @param envelopType
     * @return
     */
    public static JsonType getEnvelopOpType(JsonType envelopType){
        ObjectJsonType body = (ObjectJsonType) JsonTypeUtils.get(envelopType, "Body");
        JsonType opType = null;
        for (JsonType child : body.getChildren()) {
            opType =  child;
        }
        if(opType == null || !(opType instanceof ObjectJsonType)) return null;
        return opType;
    }

    /**
     *
     * @param envelopType
     * @return
     */
    public static JsonType getHttp2WsRespReturnType(JsonType envelopType){
        JsonType wrappedJsonType = getEnvelopOpType(envelopType);
        if(wrappedJsonType == null || !(wrappedJsonType instanceof ObjectJsonType)) return null;
        JsonType returnType =   ((ObjectJsonType)wrappedJsonType).getChildren().get(0);
        if(returnType == null || !(returnType instanceof ObjectJsonType))
            return null;
        return returnType;
    }

    public static Object parseJson(String text,JsonType jsonType){
        Object data = JsonUtils.parse(text);
        return jsonStringTypeToJson(jsonType,data);
    }
    private static void buildArrayPath(JsonType jsonType,String parentPath,List<String> arrayPaths){
        if(parentPath == null) parentPath = "";
        List<JsonType> children = new ArrayList<>();
        String currentPath = parentPath+"/"+jsonType.getFullTagName();
        if(jsonType instanceof ArrayJsonType){
            arrayPaths.add(currentPath);
            children = ((ArrayJsonType) jsonType).getChildren();
        }else if(jsonType instanceof ObjectJsonType){
            children = ((ObjectJsonType) jsonType).getChildren();
        }
        for (JsonType child : children) {
            buildArrayPath(child,currentPath,arrayPaths);
        }
    }
    public static Object parseXmlByJsonType(JsonType jsonType,String xml){
        if(jsonType instanceof StringJsonType){
            jsonType = ((StringJsonType) jsonType).getChildren().get(0);
        }
        List<String> arrayPaths = new ArrayList<>();
        buildArrayPath(jsonType,"",arrayPaths);
        return XmlUtils.parseXmlPart(xml,true,arrayPaths.toArray(new String[0]));
    }

    /**
     * 将jsonType里的json string type转换为json
     * @param jsonType
     * @param data
     * @return
     */
    private static Object jsonStringTypeToJson(JsonType jsonType, Object data) {
        if(data == null) return data;
        if(jsonType instanceof StringJsonType
        ){
            String text = (String) data;
            Object obj = null;
            if(StringJsonOrXmlType.string_xml.name().equals(jsonType.getType())){
                throw new StdException("暂时未开发完成");
            }else{
                obj = JsonUtils.parse(text);
            }

            if(obj instanceof Map){
                JsonStringObject str = new JsonStringObject<>();
                str.putAll((Map)obj);
                return str;
            }else if(obj instanceof List){
                JsonStringArray str = new JsonStringArray<>();
                str.addAll((List)obj);
                return str;
            }
            return data;

        }else if(jsonType instanceof ObjectJsonType){
            Map map = (Map) data;
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                if(map.containsKey(child.getName())){
                    map.put(child.getName(), jsonStringTypeToJson(child,map.get(child.getName())));
                }
            }
            return map;
        }else if(jsonType instanceof ArrayJsonType){
            List list = (List) data;
            if(((ArrayJsonType) jsonType).getChildren().size() == 1){
                return list.stream().map(vs->{
                    return jsonStringTypeToJson(((ArrayJsonType) jsonType).getChildren().get(0),vs);
                }).collect(Collectors.toList());
            }else{
                List ret = new ArrayList();
                int index = 0;
                for (JsonType child : ((ArrayJsonType) jsonType).getChildren()) {
                    if(list.size() <= index) break;
                    Object o = list.get(index);
                    ret.add(jsonStringTypeToJson(child,o));
                    index++;

                }
                return ret;
            }
        }
        return data;
    }




    public static Object getStringJsonTypeParameterMapper(ComplexJsonType stringJsonType){
        /*if(stringJsonType.getExprValue() != null){ // 当前字段的映射不为空，直接映射当前字段即可
            return stringJsonType.getExprValue();
        }

        if(stringJsonType.getChildren().isEmpty()){
            return null;
        }

        JsonType jsonType = stringJsonType.getChildren().get(0);

        Object exprValue = jsonType.toExprValue(null);
        if(StringJsonOrXmlType.string_json_object.name().equalsIgnoreCase(stringJsonType.getType())
         || StringJsonOrXmlType.string_json_array.name().equalsIgnoreCase(stringJsonType.getType())
        ){
            return new JsonStringParameterMapper(exprValue);
        }else{
            return new XmlStringParameterMapper(exprValue,jsonType);
        }*/
        return null;
    }

}

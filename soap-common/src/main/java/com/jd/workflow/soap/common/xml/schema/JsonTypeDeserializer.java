package com.jd.workflow.soap.common.xml.schema;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.lang.type.ObjectTypes;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class JsonTypeDeserializer extends JsonDeserializer<JsonType> {
    Map<String,PropertyDescriptor> props;
    public JsonTypeDeserializer(){
        try {
            props = new HashMap<>();
             PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] propertyDescriptors = propertyUtilsBean.getPropertyDescriptors(JsonType.class);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                props.put(propertyDescriptor.getName(),propertyDescriptor);
            }
            props.put("refName",null);
            props.put("refType",null);
        } catch (Exception e) {
           throw StdException.adapt(e);
        }
    }

    public static void main(String[] args) {
        JsonTypeDeserializer jsonTypeDeserializer = new JsonTypeDeserializer();
    }
    @Override
    public JsonType deserialize(JsonParser jsonParser, DeserializationContext ctx)
            throws IOException, JsonProcessingException {
        Map map = jsonParser.readValueAs(Map.class);
        if(map == null) return null;
        JsonType jsonType = validateJsonType(map, new ArrayList<>());

        // getBeanSerializer(AnyBean.class);
        //BeanDeserializer deserializer = new BeanDeserializer()
        validateStringJsonTypeNotAllowNest(jsonType,false);
        return jsonType;
    }

    /**
     * string_xml、string_json类型不允许嵌套，嵌套处理起来有点点复杂
     */
    private void validateStringJsonTypeNotAllowNest(JsonType jsonType,boolean parentHasStringJsonType){

        if(jsonType instanceof StringJsonType ){
            if(parentHasStringJsonType){
                throw new JsonTypeParseException("json.string_xml_or_json_type_not_allow_nest").param("name",jsonType.getName());
            }else{
                parentHasStringJsonType = true;
            }

        }
        if(jsonType instanceof ComplexJsonType){
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                validateStringJsonTypeNotAllowNest(child,parentHasStringJsonType);
            }
        }
    }
  /*  private BeanSerializer getBeanSerializer(Class<?> beanClass) {
        if (beanClass == null) {
            return null;
        }
        if (beanClass == Enum.class) {
            return null;
        }
        ObjectMapper objectMapper = JsonUtils.mapper();
        try {
            final DefaultSerializerProvider.Impl serializerProvider1 = (DefaultSerializerProvider.Impl) objectMapper.getSerializerProvider();
            final DefaultSerializerProvider.Impl serializerProvider2 = serializerProvider1.createInstance(objectMapper.getSerializationConfig(), objectMapper.getSerializerFactory());
            final JavaType simpleType = objectMapper.constructType(beanClass);
            final JsonSerializer<?> jsonSerializer = BeanDeserializerFactory.instance.createBeanDeserializer(serializerProvider2, simpleType);
            if (jsonSerializer == null) {
                return null;
            }
            if (jsonSerializer instanceof BeanSerializer) {
                return (BeanSerializer) jsonSerializer;
            } else {
                return null;
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }*/
    private JsonType validateJsonType(Map map, List<String> levels){
        if(map == null){
            return null;
        }
        levels.add(map.get("name")+"");
        String levelStr = levels.stream().collect(Collectors.joining("."));
        String type = (String) map.get("type");
        if(ObjectHelper.isEmpty(type)){
            JsonTypeParseException exception = new JsonTypeParseException("json.err_parse_jsonType_miss_type");
            exception.param("level",levelStr);
            throw exception;
        }
        JsonType jsonType = JsonType.newEntity((String) type);
        for (Object o : map.entrySet()) {
            Map.Entry<String,Object> entry = (Map.Entry<String, Object>) o;
            if("children".equals(entry.getKey()) || "type".equals(entry.getKey()) ) continue;
            if("exprType".equals(entry.getKey())){
                jsonType.setExprType(ExprType.valueOf((String) entry.getValue()));
                continue;
            }
            if(entry.getKey().equals("genericTypes")){
                if(entry.getValue() == null) continue;
                List list = (List) entry.getValue();
                List<JsonType> types = new ArrayList<>();
                for (Object genericTypes : list) {
                    final JsonType genericType = JsonUtils.cast(genericTypes, JsonType.class);
                    types.add(genericType);
                }
                jsonType.setGenericTypes(types);
                continue;
            }
            if(props.containsKey(entry.getKey())){
                Object value = entry.getValue();
                if(entry.getValue() instanceof Map || entry.getValue() instanceof Collection){
                    value = JsonUtils.cast(entry.getValue(), props.get(entry.getKey()).getPropertyType());

                }
                try {
                    BeanUtils.setProperty(jsonType,entry.getKey(),value);
                } catch (Exception e) {
                   throw StdException.adapt(e);
                }
            }else{
                jsonType.getExtAttrs().put(entry.getKey(),entry.getValue());
            }

        }

        StringJsonOrXmlType stringJsonType = null;
        if(jsonType instanceof ComplexJsonType
        ){

            List children = (List) map.get("children");
            List<JsonType> list = validateListJsonType(children, levels);
            ((ComplexJsonType)jsonType).setChildren(list);

            if(stringJsonType != null
             && stringJsonType.equals(StringJsonOrXmlType.string_xml)
            ){
                if(list.size() == 0 ){
                    //if(!list.get(0).getType().equalsIgnoreCase(stringJsonType.getRawType())){
                        throw new JsonTypeParseException("json.string_xml_or_json_type_child_not_allow_empty").param("name",jsonType.getName());
                    //}
                }
            }

        }
        levels.remove(levels.size()-1);
        return transformMapType(jsonType);

    }
    private JsonType transformMapType(JsonType jsonType){
        if(jsonType instanceof ObjectJsonType){
            if( MapJsonType.isMapType((ObjectJsonType) jsonType)
            ){
                MapJsonType mapJsonType = new MapJsonType();
                jsonType.cloneTo(mapJsonType);
                jsonType.getGenericTypes().get(0).setName("key");
                jsonType.getGenericTypes().get(1).setName("value");

                mapJsonType.setChildren(jsonType.getGenericTypes());
                return mapJsonType;
            }
        }
        return jsonType;
    }
    private List<JsonType> validateListJsonType(List<Map> children, List<String> levels){
        if(ObjectHelper.isEmpty(children)){
            return new ArrayList<>();
        }
        List<JsonType> jsonTypes = new ArrayList<>();
        int index = 0;
        for (Map child : children) {
            levels.add(index+"");
            jsonTypes.add(validateJsonType(child,levels));
            levels.remove(levels.size()-1);
            index++;
        }
        return jsonTypes;
    }
    static class AnyBean{

    }
}

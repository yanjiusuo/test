package com.jd.workflow.soap.common.xml.schema;


import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import org.apache.commons.lang.StringUtils;



import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 简单类型: string、number、integer、boolean
 */

public class SimpleJsonType extends JsonType {
    String type;




    public SimpleJsonType() {
    }
    public SimpleJsonType(SimpleParamType type) {
        this.type = type.typeName();
    }

    public SimpleJsonType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public SimpleJsonType(SimpleParamType type, String qName) {

        this.type = type.typeName();
        this.name = qName;
    }
    public void setType(SimpleParamType paramType){
        this.type = paramType.typeName();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }



    @Override
    public Object toDescJson() {
        return this.type;
    }



    @Override
    public Object toExprValue(ValueBuilderAcceptor acceptor) {

        Object value = this.getExprValue();
        if(acceptor != null){
            return acceptor.afterSetValue(value,this);
        }
        return value;
    }

    @Override
    public Class getTypeClass() {
        return null;
    }






    @Override
    public void transformToXml(XNode parent, Object inputValue, List<String> level,XmlBuilderAcceptor acceptor) throws ToXmlTransformException {
        Object val;
        if(inputValue == null) {
            /*if(this.required){
                throw new ToXmlTransformException("xml.err_miss_required_value")
                        .param("type",this.type)
                        .param("level", StringUtils.join(level,">"));
            }*/
            val = "";
            return;
        }else{
            if(inputValue instanceof Map
                    || inputValue instanceof Collection
            ){
                throw new ToXmlTransformException("xml.err_convert_type")
                        .param("level", StringUtils.join(level," > "))
                        .param("expected",this.type)
                        .param("value",inputValue)
                        .param("actual",inputValue.getClass().getName());
            }
            val =  Variant.valueOf(inputValue).toString();
        }

        XNode node = XNode.make(getFullTagName());
        if(acceptor != null)   acceptor.beforeBuildNode(node,this);

        /*if(val instanceof XmlString){
            node.appendChildren(((XmlString)val).getValue());
        }else{*/
            node.content(Variant.valueOf(val).toString());
        //}

        addAttributeTo(node);
        if(acceptor != null) acceptor.afterBuildNode(node,this);
        parent.appendChild(node);
    }


    public void setType(String type) {
        if(SimpleParamType.from(type) == null){
            throw new StdException("jsontype.err_found_invalid_type").param("type",type);
        }
        this.type = type;
    }
    public Map<String,Object> toJson(){
        Map<String,Object> map = super.toJson();
        map.put("type",this.type);
        return map;
    }

    @Override
    public JsonType newEntity() {
        return new SimpleJsonType();
    }

    @Override
    public void buildExprNode(ExprTreeNode parent) {

            ExprTreeNode current = buildCurrentExprNode(parent);

    }

    /**
     * 按照类型转换值
     * @param value
     * @return
     */
    public Object castTypeValue(Object value){
        if(value == null || StringUtils.isBlank(getType())) return value;
        return SimpleParamType.from(getType()).castValue(value);
    }

    /**
     * 字段映射的时候转换值，暂时只处理string类型，别的类型可能会有问题
     * @param value
     * @param currentLevels
     * @return
     */
    @Override
    public Object castValue(Object value, List<String> currentLevels) {
        if(StringUtils.isBlank(getType())) return value;
        currentLevels.add(this.getName());
        if(SimpleParamType.STRING.typeName().equals(getType())){// 暂时只处理string类型序列化，别的类型暂时不支持
            if(value instanceof Map || value instanceof List){
                return JsonUtils.toJSONString(value);
            }
        }
        return value;
    }

    public static void main(String[] args) {
        String data = "{\"type\":\"string\",\"name\":\"wjf\"}";
        JsonType parse = JsonUtils.parse(data, JsonType.class);
        System.out.println(parse);
    }
}

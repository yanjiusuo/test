package com.jd.workflow.soap.common.xml.schema;


import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 针对xml来说,array只能为纯array，array类型只能有一个child
 *  针对xml
 *  <children><intVar>1</intVar></children>
 *  <children><intVar>1</intVar></children>
 *  会被转换为
 *  {
 *      type:array,
 *      name:children,
 *      arrayItemType:string
 *  }
 *  针对array object类型，可以有
 *   对应:
 *   <person><id>1</id><name>aaa</name></person><person><id>2</id></person>
 *
 *   {
 *       type:array,
 *       name:person,
 *       children:[{
 *           type:object,
 *           name:person,
 *           children:[{
 *               name:"id",
 *               type:"string"
 *           },{
 *                name:"name",
 *                type:"string"
 *            }]
 *       }]
 *   }
 *
 */
@Data
public class ArrayJsonType extends ComplexJsonType{
    List<JsonType> children = new ArrayList<>();


    @Override
    public String getType() {
        return "array";
    }

    public String getArrayItemType() {
        if(children.size()== 1){
            return getChildren().get(0).getType();
        }
        return null;
    }




    @Override
    public boolean isSimpleType() {
        return false;
    }



    @Override
    public Object toDescJson() {
        List<Object> array = new ArrayList<>();
        if(this.children.isEmpty()){
            return array;
            //array.add(this.getArrayItemType());
        }else{

            for (JsonType child : children) {
                array.add(child.toDescJson());
            }
        }
        return array;
    }



    @Override
    public Object toExprValue(ValueBuilderAcceptor acceptor) {
        Object value = this.getExprValue();
        if(value == null){
            if(this.children.isEmpty()){
                value = null;
            }else{
                List<Object> array = new ArrayList<>();
                for (JsonType child : children) {
                    array.add(child.toExprValue(acceptor));
                }

                boolean isAllItemNull = array.stream().allMatch(item->{
                    return item == null;
                });
                if(isAllItemNull){
                    return new ArrayList<>();
                }else{
                    value = array;
                }

            }
        }


        if(acceptor != null)  return acceptor.afterSetValue(value,this);
        return value;
    }

    @Override
    public Class getTypeClass() {
        return List.class;
    }

    public void addChild(JsonType jsonType){
        children.add(jsonType);
    }



    @Override
    public void transformToXml(XNode parent, Object inputValue, List<String> currentLevel,XmlBuilderAcceptor acceptor) throws ToXmlTransformException {
        if(inputValue == null){
            if(this.required){
                throw new ToXmlTransformException("xml.err_miss_required_value")
                        .param("type",getType())
                        .param("level", StringUtils.join(currentLevel,">"));
            }
            return ;
        }
        if(children == null || children.isEmpty()){
            throw new ToXmlTransformException("xml.err_array_type_children_is_not_allow_empty")
                    .param("expected",getType())
                    .param("value",inputValue)
                    .param("actual",inputValue.getClass().getName())
                    .param("level", StringUtils.join(currentLevel,">"));
        }
        if(! (inputValue instanceof Collection)){
            throw new ToXmlTransformException("xml.err_convert_type")
                    .param("expected",getType())
                    .param("value",inputValue)
                    .param("actual",inputValue.getClass().getName())
                    .param("level", StringUtils.join(currentLevel,">"));
        }
        if(this.required && ((Collection<?>) inputValue).size() == 0){
            throw new ToXmlTransformException("xml.err_miss_required_value")
                    .param("type",getType())
                    .param("level", StringUtils.join(currentLevel,">"));
        }
        int index = 0;
        JsonType childJsonType = children.get(0);
        for(Object o : (Collection)inputValue) {
            currentLevel.add(index + "");





            if(childJsonType instanceof ObjectJsonType || childJsonType instanceof SimpleJsonType){ // 子节点是对象类型或者简单类型，可以跳过当前子节点
                JsonType clone = childJsonType.clone();
                clone.setName(this.name);
                clone.setAttrs(this.attrs);
                clone.setNamespacePrefix(this.namespacePrefix);
                clone.transformToXml(parent, o, currentLevel,acceptor);
            }else{ // 子节点是数组类型，数组套数组，需要将当前节点转换为对象类型
                ObjectJsonType objectJsonType = new ObjectJsonType();
                cloneTo(objectJsonType);
                Map map = new HashMap();
                map.put(childJsonType.getName(),o);
                objectJsonType.transformToXml(parent,map,currentLevel,acceptor);
            }


            currentLevel.remove(currentLevel.size() - 1);
            index++;

        }
        for (XNode child : parent.getChildren()) {
            if(acceptor != null)  acceptor.afterBuildNode(child,this);
        }

    }


    @Override
    public void buildExprNode(ExprTreeNode parent) {

            ExprTreeNode current = super.buildCurrentExprNode(parent);

            buildChildExprNode(current,this);
    }

    @Override
    public Object castValue(Object value, List<String> currentLevels) {
        if(value == null ){
            return value;
        }
        if(value instanceof String && ((String) value).startsWith("[")){
            value = JsonUtils.parse((String)value,List.class);
        }else if(!(value instanceof List)) return value;
        List list = (List) value;
        List ret = new ArrayList();
        if(getChildren().size() > 1){ // 数组里每个元素不一样
            for (int i = 0; i < list.size(); i++) {
                if( i >= children.size() ){
                    ret.addAll(list.subList(i,list.size()));
                    break;
                }
                ret.add(children.get(i).castValue(list.get(i)));
            }
        }else if(getChildren().size() == 1){
            for (Object o : list) {
                ret.add(children.get(0).castValue(o));
            }
        }
        return ret;
    }

    private static void buildChildExprNode(ExprTreeNode current,ArrayJsonType arrayJsonType){
        if(arrayJsonType.getChildren().size() > 0){
            int index = 0;
            for (JsonType child : arrayJsonType.getChildren()) {
                ExprTreeNode childNode = new ExprTreeNode();
                String label = "["+index+"]";
                childNode.setLabel(label);
                childNode.setExpr(current.getExpr()+label);
                childNode.setType(child.getType());
                current.addChild(childNode);
                index++;
                if(child instanceof ArrayJsonType){
                    buildChildExprNode(childNode, (ArrayJsonType) child);
                }else  if(child instanceof ComplexJsonType){
                    for (JsonType jsonType : ((ComplexJsonType) child).getChildren()) {
                        jsonType.buildExprNode(childNode);
                    }
                }
            }
        }
    }


    @Override
    public String toString() {
        return "JsonType{type=" +getType()+","+
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public JsonType newEntity() {
        return new ArrayJsonType();
    }
}

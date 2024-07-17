package com.jd.workflow.codegen.model;

import com.jd.workflow.codegen.GenericClassModel;
import com.jd.workflow.codegen.model.type.EntityClassModel;
import com.jd.workflow.codegen.model.type.IClassModel;
import com.jd.workflow.codegen.model.type.ReferenceClassModel;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;

import java.util.*;

public class ClassModelCollector {
    /**
     *  key类名，value是该类名对应的所有的实现classModel。若该类为非泛型类，则只会有一个实现类，否则会有多个实现类
     */
    Map<String, GenericClassModel> map = new HashMap<>();
    /**
     * key是所有的对象类， value是实现类。若key为非泛型，则value为空，否则value为非空
     */
    Map<IClassModel,List<IClassModel>> type2Impl = new HashMap<>();
    public boolean exist(String className){
        return map.containsKey(className);
    }
    public void init(){
        for (Map.Entry<String, GenericClassModel> entry : map.entrySet()) {
            entry.getValue().init();
        }
    }
    public GenericClassModel get(String name){
        return map.get(name);
    }
    public void add(String name,GenericClassModel model){
        map.put(name,model);
    }

    public Collection<IClassModel> all(){
        List<IClassModel> list = new ArrayList<>();
        for (GenericClassModel value : map.values()) {
            list.add(value.getGenericClassModel());
        }
        return list;
    }
    public List<EntityClassModel> allEntityModel(){
        List<EntityClassModel> result = new ArrayList<>();

        return result;
    }

    /**
     * 所有的泛型类
     * @return
     */
    public List<EntityClassModel> genericEntityModels(){
        List<EntityClassModel> result = new ArrayList<>();
        for (GenericClassModel value : map.values()) {
             if(!value.getGenericClassModel().getFormalParams().isEmpty()){
                 if(!(value.getGenericClassModel() instanceof EntityClassModel)){
                     continue;

                 }
                 result.add((EntityClassModel) value.getGenericClassModel());
             }
        }
        return result;
    }
}

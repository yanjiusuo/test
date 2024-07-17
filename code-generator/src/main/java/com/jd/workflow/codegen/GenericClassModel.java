package com.jd.workflow.codegen;

import com.jd.workflow.codegen.model.FieldModel;
import com.jd.workflow.codegen.model.type.*;
import com.jd.workflow.soap.common.util.ObjectHelper;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 泛型类模型。用于处理泛型类的情况。genericClassModel为泛型类，implClassModel为泛型类的实现类
 */
public class GenericClassModel {
    IClassModel genericClassModel;
    static final String[] genericTypeNames = new String[]{
            "T","K","V","E","U","W"
    };
    List<EntityClassModel> implClassModel = new ArrayList<>();

    public GenericClassModel(IClassModel genericClassModel) {
        this.genericClassModel = genericClassModel;
    }

    public IClassModel getGenericClassModel() {
        return genericClassModel;
    }

    public void setGenericClassModel(IClassModel genericClassModel) {
        this.genericClassModel = genericClassModel;
    }
    public void addImplClassModel(IClassModel model){
        if(!(genericClassModel instanceof EntityClassModel)) return;
        implClassModel.add((EntityClassModel) model);
    }
    private boolean isEmpty(Collection<String> collection){
        if(collection == null) return true;
        for (String s : collection) {
            if(StringUtils.isNotBlank(s)) return false;
        }
        return true;
    }
    public void init(){
        boolean hasFormalParams = !isEmpty(genericClassModel.getFormalParams());
        if(hasFormalParams){ // 正常扫描出来了
           if(genericClassModel instanceof EntityClassModel){
               Set<String> result = new HashSet<>();
               for (FieldModel field : ((EntityClassModel) genericClassModel).getFields()) {
                   if(field.getType().isTypeVariable()){
                      result.add(field.getName());
                   }
               }
               if(result.size() > 0){ // 变量与实际变量一致
                   return;
               }
               for (FieldModel field : ((EntityClassModel) genericClassModel).getFields()) {
                   if(field.getType().isArray()){ // 处理class Page<T> { List<T> records;}这种情况
                       ArrayClassModel arrayClassModel = (ArrayClassModel) field.getType();
                       if(ObjectHelper.isEmpty(arrayClassModel.getChildren())) continue;
                       if(!genericClassModel.getGenericTypes().isEmpty()){
                           int i = 0;
                           for (IClassModel genericType : genericClassModel.getGenericTypes()) {
                               if(!(arrayClassModel.getChildren().get(0) instanceof IClassModel)){
                                 continue;
                               }
                                IClassModel arrayChild = (IClassModel) arrayClassModel.getChildren().get(0);
                               if(ObjectHelper.equals(genericType.getClassName(),arrayChild.getClassName())){
                                   TypeVariable componentType = new TypeVariable(genericClassModel.getFormalParams().get(i));
                                   componentType.setBindType(arrayChild);

                                   arrayClassModel.setChildren(Collections.singletonList(componentType));
                                   continue;
                               }
                               i++;
                           }
                       }
                   }
               }
           }
            return;
        }
        List<String> genericFields = getGenericFields();
        if(genericFields.isEmpty()) return;
        Map<String, String> fld2TypeVariableName = initGeneFieldName(genericFields);

        EntityClassModel model = (EntityClassModel) genericClassModel;
        for (EntityClassModel entityClassModel : implClassModel) {
            for (Map.Entry<String, String> entry : fld2TypeVariableName.entrySet()) {
                entityClassModel.setFormalParams(new ArrayList<>());
                entityClassModel.setGenericTypes(new ArrayList<>());
                entityClassModel.getFormalParams().add(entry.getValue());
                FieldModel field = entityClassModel.getField(entry.getKey());
                if(field.getType() instanceof TypeVariable){
                    entityClassModel.getGenericTypes().add(((TypeVariable) field.getType()).getBindType());
                }else{
                    entityClassModel.getGenericTypes().add((IClassModel) field.getType());
                }

                field.setType(new TypeVariable(entry.getValue()));
            }
        }
    }
    private Map<String,String> initGeneFieldName(List<String> genericFields){
        Map<String,String> map = new HashMap<>();
        for (int i = 0; i < genericFields.size(); i++) {
            if(i< genericTypeNames.length){
                map.put(genericFields.get(i),genericTypeNames[i]);
            }else{

            }

        }
        return map;
    }

    /**
     * 获取所有的泛型字段
     * @return
     */
    private  List<String> getGenericFields(){
        Map<String/*fieldName*/,String /*type*/> fld2Type = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (EntityClassModel model : implClassModel) {
            for (FieldModel field : model.getFields()) {
                if(field.getType().isTypeVariable()){
                    if(!result.contains(field.getName())){
                        result.add(field.getName());
                    }

                    continue;
                }
                String fldType = fld2Type.get(field.getName());
                if(fldType == null){
                    fld2Type.put(field.getName(),field.getType().getTypeName());
                }else{
                    if(!fldType.equals(field.getType().getTypeName())){
                        if(!result.contains(field.getName())){
                            result.add(field.getName());
                        }

                    }
                }
            }
        }

        return result;
    }
    private static List<String> getGenericFields(EntityClassModel model1,EntityClassModel model2){
        Map<String, List<FieldModel>> modelMap1 = model1.getFields().stream().collect(Collectors.groupingBy(FieldModel::getName));
        Map<String, List<FieldModel>> modelMap2 = model2.getFields().stream().collect(Collectors.groupingBy(FieldModel::getName));
        List<String> fields = new ArrayList<>();
        for (FieldModel field : model1.getFields()) {
            if(field.getType() instanceof TypeVariable) {
                fields.add(field.getType().getTypeName());
            };
            List<FieldModel> field2ModelList = modelMap2.get(field.getName());
            if(field2ModelList.isEmpty()) continue;
            FieldModel field2 = field2ModelList.get(0);
            if(!field2.getType().getTypeName().equals(field.getType().getTypeName())) continue;
        }
        return fields;
    }

    public List<EntityClassModel> getImplClassModel() {
        return implClassModel;
    }

    public void setImplClassModel(List<EntityClassModel> implClassModel) {
        this.implClassModel = implClassModel;
    }


}

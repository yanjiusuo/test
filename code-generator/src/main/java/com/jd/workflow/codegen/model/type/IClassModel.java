package com.jd.workflow.codegen.model.type;

import com.jd.workflow.codegen.model.MethodModel;
import com.jd.workflow.soap.common.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class IClassModel implements IType{

    private String className;
    private String desc;
    private String jsType;

    /**
     * 为了方便调试，看下当前类关联的方法是什么
     */
    private MethodModel relatedMethod;
    /**
     * 形参泛型
     */
    List<String> formalParams = new ArrayList<>();
    /**
     * 泛型实际绑定的类型
     */
    List<IClassModel> genericTypes = new ArrayList<>();

    public MethodModel getRelatedMethod() {
        return relatedMethod;
    }

    public void setRelatedMethod(MethodModel relatedMethod) {
        this.relatedMethod = relatedMethod;
    }

    public String getTypeName(){
        return className;
    }

    /**
     * 是否为类型变量类型
     * @return
     */
    public boolean isTypeVariable(){
        return false;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getFormalParams() {
        return formalParams;
    }

    public void setFormalParams(List<String> formalParams) {
        this.formalParams = formalParams;
    }

    public List<IClassModel> getGenericTypes() {
        return genericTypes;
    }

    public void setGenericTypes(List<IClassModel> genericTypes) {
        this.genericTypes = genericTypes;
    }
    public String getPkgName(){
        if(className.indexOf('.')==-1){
            return null;
        }
        return className.substring(0,className.lastIndexOf('.'));
    }
    public String getName(){
        return StringHelper.lastPart(className,'.');
    }
    public abstract IClassModel clone();

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }



    public String getJsType() {
        return jsType;
    }
    public abstract  boolean isArray();
    public boolean isGenericType(){
        return !formalParams.isEmpty();
    }
    public void setJsType(String jsType) {
        this.jsType = jsType;
    }

    public String getReference(){
        String name = StringHelper.lastPart(className,'.');
        if(genericTypes.isEmpty()){
            return name;
        }
        name+='<';
        for (IClassModel genericType : genericTypes) {
            name+= genericType.getReference()+",";
        }
        name = name.substring(0,name.length() - 1);
        name+=">";
        return name;
    }
    public String getClassDefRefence(){
        String name = StringHelper.lastPart(className,'.');
        if(formalParams == null || formalParams.isEmpty()){
            return name;
        }
        name+='<';
        for (String formalParam : formalParams) {
            name+= formalParam+",";
        }
        name = name.substring(0,name.length() - 1);
        name+=">";
        return name;
    }

    public String getFullReference(){
        String name = className;
        if(genericTypes.isEmpty()){
            return name;
        }
        name+='<';
        for (IClassModel genericType : genericTypes) {
            name+= genericType.getFullReference()+",";
        }
        name = name.substring(0,name.length() - 1);
        name+=">";
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IClassModel model = (IClassModel) o;
        return Objects.equals(getFullReference(), model.getFullReference());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFullReference());
    }


    public String toString(){
        return className;
    }
}

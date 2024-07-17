package com.jd.workflow.codegen.model.type;

public class TypeVariable implements IType{
    private String name;
    /**
     * 类型变量实际绑定的类型
     */
    IClassModel bindType;
    public TypeVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    @Override
    public boolean isTypeVariable() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @Override
    public String getReference() {
        return name;
    }

    @Override
    public String getJsType() {
        return name;
    }

    public IClassModel getBindType() {
        return bindType;
    }

    public void setBindType(IClassModel bindType) {
        this.bindType = bindType;
    }
}

package com.jd.workflow.codegen.model.type;

/**
 * 类型，包括泛型
 */
public interface IType {
   public String getTypeName();

    /**
     * 是否为类型变量类型
     * @return
     */
   public boolean isTypeVariable();
    public   boolean isArray();
    public   boolean isObject();

    /**
     * 是否简单类型
     * @return
     */
    public boolean isSimpleType();

    public String getReference();

    public String getJsType();
}

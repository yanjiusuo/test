<#if classModel.pkgName??>package ${classModel.pkgName};</#if>
<#list classModel.imports as item>
<#if item??>import ${item};</#if>
</#list>
<#if classModel.desc??>/**
* ${classModel.desc}
*/</#if>
public class ${classModel.classDefRefence} {
<#list classModel.fields as field>
<#if field.desc??>/**
    * ${field.desc}
    */</#if>
 private ${field.reference} ${field.name};
</#list>

<#list classModel.fields as field>
 public ${field.reference} ${field.getMethodName}(){
    return this.${field.name};
 }
 public void ${field.setMethodName}(${field.reference} ${field.name}){
    this.${field.name} = ${field.name};
 }
</#list>
}
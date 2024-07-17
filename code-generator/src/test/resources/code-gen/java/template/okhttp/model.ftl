package ${pkgName};
<#list imports as item>import ${item};
</#list>
public class ${name} {
<#list fields as field>
    <#if field.desc??>/**
    * ${field.desc}
    */</#if>
    private ${field.type.reference} ${field.name};
</#list>
<#list fields as field>
    public ${field.type.reference} ${field.getMethodName}(){
      return ${field.name};
    }
    public void ${field.setMethodName}(${field.type.reference} ${field.name}){
    this.${field.name} = ${field.name};
    }
</#list>

}
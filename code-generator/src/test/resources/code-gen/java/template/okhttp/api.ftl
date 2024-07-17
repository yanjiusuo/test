package ${pkgName};
<#list imports as item >import ${item};
</#list>
public interface ${name} {
<#list methods as method>
    public ${method.returnType.reference} ${method.methodName}}(${method.inputString});
</#list>
}
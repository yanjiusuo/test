<#list api.imports as item>
    import ${item};
</#list>

public interface ${api.name}{
    <#list api.methods as method>
        public ${method.returnType.reference} ${method.methodName}(
             <#list 0..(method.inputs?size -1) as index>
                   <#if method.inputs[index]??>
                       <#assign input = method.inputs[index]>
                       ${input.type.reference} ${input.name}<#if index!=method.inputs?size-1>, </#if>
                   </#if>
             </#list>
        );
    </#list>
}


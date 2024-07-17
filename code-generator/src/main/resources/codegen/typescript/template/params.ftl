<#include './interface.ftl' >
<#macro formalParams params><#if params?size gt 0><#list 0..(params?size-1) as index>
    <#local param=params[index]>
    <#if param.paramType == 'query'>params:<@typeChildren type=param.type></@typeChildren><#if index!=params?size-1>,</#if></#if><#t>
    <#if param.paramType == 'header'>headers:<@typeChildren type=param.type></@typeChildren><#if index!=params?size-1>,</#if></#if><#t>
    <#if param.paramType == 'body'>body:${param.type.jsType}<#if index!=params?size-1>,</#if><#if param.desc??>//${param.desc}</#if></#if><#t>
</#list></#if></#macro>

<#macro actualParams method><#compress >
    <#local params=method.inputs>
    {method:"${method.httpMethod}"<#t>
    <#if params?size gt 0>,<#t>
        <#list 0..(params?size-1) as index>
            <#local param=params[index]>
            <#if param.paramType == 'query'>params<#if index!=params?size-1>,</#if></#if><#t>
            <#if param.paramType == 'header'>headers<#if index!=params?size-1>,</#if></#if><#t>
            <#if param.paramType == 'body'>body<#if index!=params?size-1>,</#if></#if><#t>
            <#t></#list>
    </#if>}
</#compress></#macro>


<#macro typeDef type>
    export interface ${type.name}<#if type.isGenericType()><${type.formalParams?join(",")}></#if>{
    <#list type.fields as field>
        ${field.name} : ${field.jsType}
    </#list>
    }
</#macro>

<#macro typeChildren type>{
<#list type.fields as field>
  <#if field.desc??> /**${field.desc}*/</#if>
  ${field.name}<#if !field.required>?</#if> : ${field.jsType}
</#list>
}
</#macro>

<#macro typeDef type>
<#if type.desc??>//${type.desc}</#if>
export interface ${type.name}<#if type.isGenericType()><${type.formalParams?join(",")}></#if><@typeChildren type=type></@typeChildren>
</#macro>


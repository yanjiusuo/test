<#include './template.ftl'>


<#list interfaceModels as interfaceModel>
<#list interfaceModel.groups as groupModel>
# <#if groupModel.group.name??>${groupModel.group.name?html}<#else >${groupModel.group.id?html}</#if>
<#list groupModel.methods as method>
<#if method.type==1>
<@http methodObj=method></@http>
<#else>
<@jsf methodObj=method></@jsf>
</#if>
</#list>
</#list>
</#list>


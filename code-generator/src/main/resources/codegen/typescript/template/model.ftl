<#include "./interface.ftl" >
<#if config.generateImport>
<#list group.jsImports?keys as path><#assign varNames=group.jsImports[path]>
     import {${varNames?join(',')}} from '${path}';
</#list>
</#if>

<#list group.models as model>
     <@typeDef type=model></@typeDef>
</#list>
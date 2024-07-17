<#if config.generateImport>
import { ONLINE_URL } from '@/utils/constants'
import request from '@/utils/request'
<#list api.jsImports?keys as path><#assign varNames=api.jsImports[path]>
    import {${varNames?join(',')}} from '${path}';
</#list>
</#if>


<#include './params.ftl' >
<#list api.methods as method>
/**
* <#if method.desc??>${method.desc}</#if>
* <#list method.inputs as param>@param ${param.name} <#if param.desc??>${param.desc}</#if></#list>
*/
export async function ${method.methodName}(<@formalParams params=method.inputs></@formalParams>):Promise<#if method.returnType??><${method.returnType.jsType}></#if>{
    return  request.request(`<#noparse>${ONLINE_URL}</#noparse>${method.url}`<#if method.inputs?size gt 0>,</#if><@actualParams  method=method></@actualParams> );
}
</#list>


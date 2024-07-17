<#macro jsf methodObj>
<#local method=methodObj.contentObject>
## ${methodObj.name?html}(${methodObj.methodCode})
<a id=${methodObj.name?html}> </a>
### 基本信息


**接口描述：**
<#if methodObj.desc??>${methodObj.desc?html}</#if>
${service.escapeMarkdown(methodObj)}
### 请求参数
${service.outputTable(method.input)}
<#if methodObj.docConfig.inputExample??>

### 请求示例

```json
${methodObj.docConfig.inputExample}
```
</#if>
### 响应参数
<#if method.output??>
${service.outputSingleTable(method.output)}
</#if>

<#if methodObj.docConfig.outputExample??>
### 响应示例

```json
${methodObj.docConfig.outputExample}
```
</#if>
</#macro>





<#macro http methodObj>
    <#local method=methodObj.contentObject>

## ${methodObj.name?html} <#if methodObj.version??>(版本号：${methodObj.version})</#if>
<a id=${methodObj.name?html}> </a>
### 基本信息

**Path：** ${methodObj.path}

**Method：** ${methodObj.httpMethod}

**接口描述：**
<#if methodObj.desc??>${methodObj.desc?html}</#if>
${service.escapeMarkdown(methodObj)}
### 请求参数
<#if method.input.params?? && (method.input.params?size>0)>
**Query**
${service.outputTable(method.input.params)}
</#if>

<#if method.input.path?? && (method.input.path?size>0)>
**path**
${service.outputTable(method.input.path)}
</#if>
<#if method.input.headers?? && (method.input.headers?size>0)>
**headers**
${service.outputTable(method.input.headers)}
</#if>

<#if method.input.body??>   

**Body**   
${service.outputTable(method.input.body)}
</#if>
<#if methodObj.docConfig.inputExample??>
### 请求示例

```json
${methodObj.docConfig.inputExample}
```
</#if>
### 响应参数
<#if method.output.body??>
**Body**
${service.outputTable(method.output.body)}
</#if>
<#if methodObj.docConfig.outputExample??>

### 响应示例

```json
${methodObj.docConfig.outputExample}
```
</#if>
</#macro>



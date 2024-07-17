package com.jd.workflow.console.service.doc.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.doc.SwaggerParserService;
import com.jd.workflow.console.utils.DigestUtils;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.*;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {"apiData":{"baseInfo":{"apiRequestType":0,"apiName":"复制签名","apiURI":"/application/copy","apiUpdateTime":1672049412303,"curVersion":11,"apiID":61597,"apiStatus":1},"headerInfo":[{"headerName":"token","headerValue":"123"}],"resultBodyInfo":[{"paramKey":"root","paramType":"2","wikiID":1393267,"paramPack":{"pactID":1185417},"wikiCount":0,"paramName":"\n引用自:Result«string»","paramDefault":""},{"paramKey":"root>>code","paramType":"1","wikiID":1393268,"paramPack":{"pactID":1185418},"wikiCount":0,"paramName":"[:INTEGER]","paramDefault":"12112"},{"paramKey":"root>>data","paramType":"0","wikiID":1393269,"paramPack":{"pactID":1185419},"wikiCount":0,"paramName":"","paramDefault":"@datetime()"},{"paramKey":"root>>message","paramType":"0","wikiID":1393270,"paramPack":{"pactID":1185420},"wikiCount":0,"paramName":"","paramDefault":""},{"paramKey":"root>>success","paramType":"4","wikiID":1393271,"paramPack":{"pactID":1185421},"wikiCount":0,"paramName":"","paramDefault":"@boolean()"}],"requestQueryInfo":[{"paramKey":"id","paramRequired":false,"wikiID":1393266,"isJsonString":false,"wikiCount":0,"paramName":"应用id","paramDefault":""},{"paramKey":"name","paramRequired":false,"wikiID":1761447,"isJsonString":false,"wikiCount":0,"paramName":"","paramDefault":""}],"responseHeaderInfo":[{"headerName":"respHeader","headerValue":"32"}],"requestBodyInfo":{"requestType":"form","requestForm":[{"paramKey":"sss","paramRequired":false,"wikiID":1761448,"isJsonString":false,"wikiCount":0,"paramName":"sdfsd","paramDefault":""},{"paramKey":"ttt","paramRequired":false,"wikiID":1761449,"isJsonString":false,"wikiCount":0,"paramName":"sdfdsfdsfds","paramDefault":""}],"requestJson":[]},"restfulInfo":[],"apiDes":"### gfgfddfg\n\ngldfkljkgfdjlkgdf","mailInfo":{"allUsers":[{"erp":"wangjingfang3","checked":false,"userName":"王井方","userID":5500},{"erp":"qisi","checked":false,"userName":"齐思","userID":220},{"erp":"zhouqiang49","checked":false,"userName":"周强","userID":8013},{"erp":"jiangxin101","checked":false,"userName":"姜欣","userID":8796},{"erp":"linjin3","checked":false,"userName":"林瑾","userID":11190},{"erp":"tangqianqian11","checked":false,"userName":"唐倩倩","userID":8947}],"sendMail":false,"mailAll":false},"classifyInfo":{"groupID":13272,"projectID":1614}},"statusCode":"000000"}
 */
@Data
public class JapiData {
    String apiDes;
    BaseInfo baseInfo;
    List<HeaderInfo> headerInfo;
    List<RequestParamInfo> resultBodyInfo;
    List<RequestParamInfo> requestQueryInfo;
    RequestBodyInfo requestBodyInfo;
    List<HeaderInfo> responseHeaderInfo;
    /**
     * path参数信息
     */
    List<RequestParamInfo> restfulInfo;

    @Data
    public static class BaseInfo{
        Long apiID;
        String apiName;
        Integer apiRequestType;
        Integer apiStatus;
        String apiURI;
        Long apiUpdateTime;
        String curVersion;
    }
    public static class ClassifyInfo{}
    @Data
    public static class HeaderInfo{
        String headerName;
        String headerValue;
    }
    public static class MailInfo{}
    @Data
    public static class RequestBodyInfo{
        List<RequestParamInfo> requestForm;
        List<RequestParamInfo> requestJson;
        /**
         * json、form
         */
        String requestType;
    }
    public static class RequestQueryInfo{}
    public static class ResponseHeaderInfo{}
    public static class RestfulInfo{}
    @Data
    public static class ParamPack{
        String min;
        String max;
        Boolean enumEnable;
        Long pactID;
        String rule;
        @JsonProperty("enum")
        List<String> enumData;

        public boolean isEmpty(){
            return StringUtils.isEmpty(min) && StringUtils.isEmpty(max) && StringUtils.isEmpty(rule) && (enumData == null || enumData.isEmpty());
        }
    }
    @Data
    public static class RequestParamInfo{
        String paramKey;
        String paramDefault;
        ParamPack paramPack;
        String paramType;
        Integer wikiCount;
        String wikiID;
        Boolean paramRequired;
        String paramName;
        Boolean isJsonString;
        List<RequestParamInfo> jsonInfo;
        //String paramPack;

    }
    public MethodManage toMethod(){
        MethodManage methodManage = new MethodManage();
        methodManage.setName(SwaggerParserService.truncateStr(baseInfo.getApiName(),128));
        methodManage.setPath(SwaggerParserService.truncateStr(baseInfo.getApiURI(),256) );
        if(SwaggerParserService.getActualLength(apiDes) <=65535 ){
            methodManage.setDocInfo(StringHelper.emojiFilter(apiDes));
        }
        methodManage.setRelatedId(baseInfo.apiID);
        methodManage.setCreated(new Date()); // 创建时间按照当前时间来处理吧，修改时间与j-api的时间保持一致
        methodManage.setModified(new Date(baseInfo.getApiUpdateTime()));
        methodManage.setType(InterfaceTypeEnum.HTTP.getCode());
        methodManage.setYn(DataYnEnum.VALID.getCode());
        methodManage.setStatus(baseInfo.getApiStatus());
        methodManage.setHttpMethod(JapiHttpMethod.fromType(baseInfo.getApiRequestType()).name());
        HttpMethodModel methodModel = toHttpModel();
        methodManage.setDigest(DigestUtils.getHttpMethodMd5(methodModel));
         String content = StringHelper.emojiFilter(JsonUtils.toJSONString(methodModel));
        methodManage.setContent(content);
        methodManage.setContentObject(methodModel);
        return methodManage;
    }
    public HttpMethodModel toHttpModel(){
        HttpMethodModel model = new HttpMethodModel();
        HttpMethodModel.HttpMethodInput input = new HttpMethodModel.HttpMethodInput();
        model.setInput(input);
        input.setMethod(JapiHttpMethod.fromType(baseInfo.getApiRequestType()).name());
        input.setUrl(baseInfo.getApiURI());
        input.setHeaders(getHeaders(headerInfo));
        input.setPath(jsonTypeToSimple(toJsonType(restfulInfo)));

        input.setParams(toJsonType(requestQueryInfo));
        input.setReqType(requestBodyInfo.getRequestType());
        if("json".equals(requestBodyInfo.getRequestType())){
            input.setBody(toJsonType(requestBodyInfo.getRequestJson()));
        }else{
            input.setBody(toJsonType(requestBodyInfo.getRequestForm()));
        }
        HttpMethodModel.HttpMethodOutput output = new HttpMethodModel.HttpMethodOutput();
        model.setOutput(output);
        output.setHeaders(getHeaders(responseHeaderInfo));
        output.setBody(toJsonType(resultBodyInfo));
        return model;
    }
    private List<JsonType> getHeaders(List<HeaderInfo> headerInfos){
        List<JsonType> list = new LinkedList<>();
        if(headerInfo == null) return list;
        for (HeaderInfo info : headerInfos) {
            if("Content-type".equalsIgnoreCase(info.getHeaderName())) continue;
            SimpleJsonType jsonType = new SimpleJsonType();
            jsonType.setType(SimpleParamType.STRING.typeName());
            jsonType.setName(info.getHeaderName());
            jsonType.setValue(info.getHeaderValue());
            list.add(jsonType);
        }
        return list;
    }
    private List<SimpleJsonType> jsonTypeToSimple(List<JsonType> jsonTypes){
        return jsonTypes.stream().map(jsonType -> (SimpleJsonType)jsonType).collect(Collectors.toList());
    }
    public List<JsonType> toJsonType(List<RequestParamInfo> requestParamInfo){
         List<BuilderJsonType> jsonTypes = toJsonTypeInternal(requestParamInfo);
        List<JsonType> types = jsonTypes.stream().map(builderJsonType -> builderJsonType.toJsonType()).collect(Collectors.toList());
        return types;
    }
    public List<BuilderJsonType> toJsonTypeInternal(List<RequestParamInfo> requestParamInfo){
        List<BuilderJsonType> jsonTypes = new ArrayList<>();
        if(requestParamInfo == null) return new ArrayList<>();
        for (RequestParamInfo paramInfo : requestParamInfo) {
            String[] params = StringUtils.split(paramInfo.getParamKey(),">>");
            BuilderJsonType jsonType = new BuilderJsonType();
            jsonType.setDesc(paramInfo.getParamName());
            jsonType.setValue(paramInfo.getParamDefault());
            jsonType.setMock(paramInfo.getParamDefault()); // 同步mock信息
            ParamPack paramPack = paramInfo.getParamPack();
            if(paramPack != null && !paramPack.isEmpty()){

                Constraint constraint = new Constraint();
                if(StringUtils.isNotBlank(paramPack.getMin())){
                    constraint.setMin(Variant.valueOf(paramPack.getMin()).toNumber(null));
                }
                if(StringUtils.isNotBlank(paramPack.getMax())){
                    constraint.setMax(Variant.valueOf(paramPack.getMax()).toNumber(null));
                }
                constraint.setPattern(paramPack.getRule());
                constraint.setEnumValue(paramPack.getEnumData());
                jsonType.setConstraint(constraint);
            }
            if(paramInfo.getParamRequired() != null){
                jsonType.setRequired(paramInfo.getParamRequired() );
            }
            String paramType = getParamType(paramInfo.getParamType(),paramInfo.getParamName());
            if(paramInfo.isJsonString != null && paramInfo.isJsonString){
                paramType = "string_json";
                final List<BuilderJsonType> children = toJsonTypeInternal(paramInfo.getJsonInfo());
                jsonType.setChildren(children);
            }

            if(params.length == 1){
                jsonType.setName(params[0]);
            }else{
                String paramName = params[params.length-1];
                jsonType.setName(paramName);
            }


            jsonType.setType(paramType);
            insertJsonType(jsonTypes,Arrays.asList(params),jsonType);
        }


        return jsonTypes;
    }
    private String getParamType(String paramType,String paramName){
        JapiParamType type = JapiParamType.fromType(paramType,paramName);
        if(type == null) return "string";
        return type.toParamType();
    }
    private void insertJsonType(List<BuilderJsonType> jsonTypes,List<String> names,BuilderJsonType child){
        if(names.size() == 1){
            jsonTypes.add(child);
        }else{
            for (BuilderJsonType jsonType : jsonTypes) {
                if(names.get(0).equals(jsonType.getName())){

                    insertJsonType(jsonType.getChildren(), names.subList(1,names.size()),child);
                }
            }
        }
    }

}

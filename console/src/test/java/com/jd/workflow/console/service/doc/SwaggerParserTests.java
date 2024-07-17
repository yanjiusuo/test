package com.jd.workflow.console.service.doc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.doc.GroupHttpData;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.soap.common.Md5Utils;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.parser.SwaggerParser;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;
@Slf4j
public class SwaggerParserTests extends BaseTestCase {
    private static SwaggerParserService swaggerParserService = new SwaggerParserService();
   /* @Test
    public void testParseSwagger3(){
        String swaggerJson = getResourceContent("classpath:swagger/swagger3-openapi.json");
        SwaggerParseResult swaggerParseResult = new OpenAPIParser().readContents(swaggerJson, null, null);
        final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();
            pathItem.get
        }

    }*/
   static ObjectMapper objectMapper = new ObjectMapper();
    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    static {
        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, false);
        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);



        //忽略空Bean转json的错误
        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));
        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
    }
    @Test
    public void testParseSwagger2(){

        String swaggerJson = getResourceContent("classpath:swagger/swagger2-openapi.json");
      /*  ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(swaggerJson,null,parseOptions);
        parseResult.getOpenAPI().getPaths();*/
        List<GroupHttpData<MethodManage>>  listResource = swaggerParserService.parseSwagger(swaggerJson);
       /* SwaggerDeserializationResult result = swaggerParser.readWithInfo(swaggerJson,true);
        if(result.getMessages() != null && !result.getMessages().isEmpty()){
            System.out.println(result.getMessages());
            //return;
        }
        Swagger swagger= result.getSwagger();*/

        System.out.println("--------------------------123-----------------------");

        System.out.println(JsonUtils.toJSONString(listResource));
        System.out.println("--------------------------end-----------------------");
    }
    @Test
    public void testComposedModel(){

        String swaggerJson = getResourceContent("classpath:swagger/swagger2-composedModel.json");
      /*  ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(swaggerJson,null,parseOptions);
        parseResult.getOpenAPI().getPaths();*/
        List<GroupHttpData<MethodManage>>  listResource = swaggerParserService.parseSwagger(swaggerJson);
       /* SwaggerDeserializationResult result = swaggerParser.readWithInfo(swaggerJson,true);
        if(result.getMessages() != null && !result.getMessages().isEmpty()){
            System.out.println(result.getMessages());
            //return;
        }
        Swagger swagger= result.getSwagger();*/

        System.out.println("--------------------------123-----------------------");

        System.out.println(JsonUtils.toJSONString(listResource));
        System.out.println("--------------------------end-----------------------");
    }
    @Test
    public void testComposedModelYml(){

        String swaggerJson = getResourceContent("classpath:swagger/swagger2-composedModel.yml");

        List<GroupHttpData<MethodManage>>  listResource = swaggerParserService.parseSwagger(swaggerJson);

        System.out.println("--------------------------123-----------------------");

        System.out.println(JsonUtils.toJSONString(listResource));
        System.out.println("--------------------------end-----------------------");
    }


    @Test
    public void testParseJsf(){
        String json = getResourceContent("classpath:swagger/jsf/jsf-demo1.json");
        List<ClassMetadata> classMetadata = swaggerParserService.parseJsfMetadata(json);
        log.info("jsf_step_metas:json={}",JsonUtils.toJSONString(classMetadata));
    }

    @Test
    public void parseMd5(){

    }
}

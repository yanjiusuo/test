package com.jd.workflow.export;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkdownExportTests  extends BaseTestCase {
    Map<String,Object> newProject(){
        Map<String,Object> project = new HashMap<>();
        project.put("name","工程1");
        return project;
    }
    JsfStepMetadata newJsf(){
        String httpContent = getResourceContent("classpath:json_schema/jsf.json");
        return JsonUtils.parse(httpContent,JsfStepMetadata.class);
    }
    HttpMethodModel newHttp(){
        HttpMethodModel model = new HttpMethodModel();
        String httpContent = getResourceContent("classpath:json_schema/http-method.json");
        return
                JsonUtils.parse(httpContent, HttpMethodModel.class);
      /*  HttpMethodModel.HttpMethodInput input = new HttpMethodModel.HttpMethodInput();
        input.setUrl("/a/b");
        input.setMethod("post");
        model.setSummary("接口简述");
        List<JsonType> headers = new ArrayList<>();
        SimpleJsonType jsonType = new SimpleJsonType();
        jsonType.setRequired(true);
        jsonType.setType("string");
        jsonType.setName("token");
        headers.add(jsonType);
        input.setHeaders(headers);
        model.setInput(input);
        return model;*/
    }
    @Test
    public void testMustache() throws IOException {
        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("name", "Mustache");
        scopes.put("project",newProject());
        scopes.put("method",newHttp());

       // scopes.put("feature", new Feature("Perfect!"));
        String content = getResourceContent("classpath:template/api.mustache");
        Writer writer = new OutputStreamWriter(System.out);
        Handlebars handlebars = new Handlebars();

        Template template = handlebars.compileInline(content);
        String result = template.apply(scopes);
        FileWriter fileWriter = new FileWriter("fileName01");
        fileWriter.write(result);
        fileWriter.close();
      //  System.out.println(result);

    }
    @Test
    public void testGenerate() throws IOException {
        Configuration configuration = new Configuration(Configuration.getVersion());
        //3.设置模板所在的路径
        configuration.setDirectoryForTemplateLoading(new File("D:\\github-git\\interface-transform\\dev-data-flow\\console\\src\\main\\resources\\template"));
        //4.设置模板的字符集，一般utf-8
        configuration.setDefaultEncoding("utf-8");
        //5.使用Configuration对象加载一个模板文件，需要指定模板文件的文件名。
        freemarker.template.Template apiTemplate = configuration.getTemplate("template.ftl");
        /*{
            Map<String,Object> vars = new HashMap<>();
            vars.put("method",newHttp());
            vars.put("service",new DocExportService());
            String result = generate(apiTemplate, vars);
            System.out.println("==================");
            System.out.println(result);
        }*/
        {
            Map<String,Object> vars = new HashMap<>();
            vars.put("method",newJsf());
            vars.put("service",new DocExportService());
            String result = generate(apiTemplate, vars);
            System.out.println("==================");
            System.out.println(result);
        }

    }
    private String generate(freemarker.template.Template template, Object model){
        StringWriter writer = new StringWriter();
        try {
            template.process(model, writer);
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

}

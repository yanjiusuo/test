package com.jd.workflow.codegen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.codegen.model.*;
import com.jd.workflow.codegen.model.type.*;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JsGenTests extends BaseTestCase {
    CodeGenerator generator = new CodeGenerator();

    @Test
    public void generateInterface() throws Exception {
        String content = getResourceContent("classpath:code-gen/models/http-method.json");
        HttpMethodModel httpMethodModel = JsonUtils.parse(content, HttpMethodModel.class);
        ApiModel apiModel = new ApiModel();

        apiModel.setClassName("com.jd.workflow.client.RpcCaller");
        MethodModel methodModel = toMethod(httpMethodModel, apiModel);
        Map<String,Object> map = new HashMap<>();
        map.put("typeModel",methodModel.getReturnType().getType());
        String code = generateCode(map, "interface.ftl");
        System.out.println("--------------------------------------------------");
        System.out.println(code);
        Map<String,Object> apiData = new HashMap<>();
        apiData.put("api",apiModel);
        apiData.put("helper",new StringHelper());
        String interfaceCode = generateCode(apiData, "api.ftl");

        System.out.println("===============================");
        System.out.println(interfaceCode);

    }
    MethodModel toMethod(HttpMethodModel httpMethodModel,ApiModel apiModel){
        return ApiModelHelper.toMethod(httpMethodModel,apiModel,generator.getCollector());
    }

   public String generateCode(Object model,String templateName) throws Exception {
       //1.创建一个模板文件
       //2.创建一个Configuration对象
       Configuration configuration = new Configuration(Configuration.getVersion());
       //configuration.setObjectWrapper(new BeansWrapper());
       //允许使用内建函数
       //configuration.setAPIBuiltinEnabled(true);
       //3.设置模板所在的路径
       configuration.setDirectoryForTemplateLoading(new File("D:\\github-git\\interface-transform\\example\\code-generator\\src\\test\\resources\\code-gen\\typescript\\template"));
       //4.设置模板的字符集，一般utf-8
       configuration.setDefaultEncoding("utf-8");
       //5.使用Configuration对象加载一个模板文件，需要指定模板文件的文件名。
       Template modelTemplate = configuration.getTemplate(templateName);
//		Template template = configuration.getTemplate("student.ftl");
       //6.创建一个数据集，可以是pojo也可以是map，推荐使用map

       return generate(modelTemplate,model);
   }

    public void generateCode(ApiModel apiModel) throws IOException, TemplateException {
        //1.创建一个模板文件
        //2.创建一个Configuration对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //3.设置模板所在的路径
        configuration.setDirectoryForTemplateLoading(new File("D:\\github-git\\interface-transform\\dev-data-flow\\console\\src\\test\\resources\\code-gen\\typescript\\template"));
        //4.设置模板的字符集，一般utf-8
        configuration.setDefaultEncoding("utf-8");
        //5.使用Configuration对象加载一个模板文件，需要指定模板文件的文件名。
        Template apiTemplate = configuration.getTemplate("api.ftl");
        Template modelTemplate = configuration.getTemplate("model.ftl");
//		Template template = configuration.getTemplate("student.ftl");
        //6.创建一个数据集，可以是pojo也可以是map，推荐使用map


        Map<String,String> pkg2ClassFile= new HashMap<>();
        String result = generate(apiTemplate,apiModel);

        pkg2ClassFile.put(apiModel.getClassName() ,result);
        List<EntityClassModel> entityClassModels = generator.getCollector().allEntityModel();
        for (EntityClassModel classModel : entityClassModels) {
            pkg2ClassFile.put(classModel.getClassName(),generate(modelTemplate,classModel));
        }
        File output = new File("D:\\tmp\\aaa\\output");
        for (Map.Entry<String, String> entry : pkg2ClassFile.entrySet()) {
            String className = StringHelper.lastPart(entry.getKey(),'.');
            String pkgName = entry.getKey().substring(0,entry.getKey().lastIndexOf('.'));
            String filePath = StringHelper.replace(pkgName,".","/");
            File dir = new File(output,filePath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            File file = new File(dir,className+".java");
            FileWriter fileWriter = new FileWriter(file);
            IOUtils.write(entry.getValue(), fileWriter);
            fileWriter.flush();

        }
    }
    private void write(String code,File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        IOUtils.write(code, fileWriter);
        fileWriter.flush();

    }
    private String generate(Template template,Object model){
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
    private void copyDir(File source,File target){
        try {
            FileUtils.copyDirectory(source,target);
        } catch (IOException e) {
            throw new StdException("file.err_copy_dir",e);
        }
    }
    @Test
    public void generateAll() throws Exception {
        String content = getResourceContent("classpath:code-gen/models/codegen-all.json");
        Map<String, List<HttpMethodModel>> parse = JsonUtils.parse(content, new TypeReference<Map<String, List<HttpMethodModel>>>() {
        });

        List<GroupInterfaceData> groupData = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, List<HttpMethodModel>> entry : parse.entrySet()) {
            GroupInterfaceData data = new GroupInterfaceData();
            data.setGroupName("api"+i);
            data.setPkgName("com.jd.test.flow");
            groupData.add(data);
                    i++;
        }
        CodeGenerator generator = new CodeGenerator();
        File rootFile = new File("d:/tmp/tsscript");
        generator.generateCode(rootFile, groupData, new GenerateConfig());
    }
    @Test
    public void generateData() throws Exception {
        List<GroupInterfaceData> groupData = JsonUtils.parseArray(getResourceContent("classpath:code-gen/models/codegen-multi.json"), GroupInterfaceData.class);
        CodeGenerator generator = new CodeGenerator();
        File rootFile = new File("d:/tmp/tsscript");
        generator.generateCode(rootFile, groupData, new GenerateConfig());
        /*generator.init();
        generator.setGroupModelPathPrefix("/type");
        generator.setCommonModelPathPrefix("/common");



        copyDir(new File("D:\\github-git\\interface-transform\\example\\code-generator\\src\\test\\resources\\code-gen\\typescript\\template\\config"),rootFile);
        StringBuilder apiCode = new StringBuilder();
        for (ApiModel apiModel : generator.getApiGroups()) {
            apiModel.setSavedPath("/api/"+apiModel.getName()+"Api.ts");
            Map<String, Object> apiData = new HashMap<>();
            apiData.put("api", apiModel);
            String interfaceCode = generateCode(apiData, "api.ftl");
            apiCode.append(interfaceCode);
            apiCode.append("\n");
            File file = new File(rootFile,apiModel.getSavedPath());
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            write(interfaceCode,file);
            System.out.println("====================start====================");
            System.out.println(interfaceCode);
            System.out.println("===============================");
        }

        {
            StringBuilder sb = new StringBuilder();
            Map<String, Object> map = new HashMap<>();
            map.put("group", generator.getGenericModels());
            String code = generateCode(map, "model.ftl");
            sb.append(code);
            sb.append("\n");
            System.out.println("===============================");
            System.out.println(sb.toString());
            File file = new File(rootFile,"/common/common.ts");
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            write(sb.toString(),file);
        }

        for (Map.Entry<ApiModel,GroupModels> entry : generator.getGroupModels().entrySet()) {
            StringBuilder sb = new StringBuilder();
            Map<String,Object> map = new HashMap<>();
            map.put("group",entry.getValue());
            entry.getKey().setSavedPath("/type/"+entry.getKey().getName()+".ts");
            String code = generateCode(map, "model.ftl");
            sb.append(code);
            sb.append("\n");
            System.out.println("===============================");
            File file = new File(rootFile,entry.getKey().getSavedPath());
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            write(sb.toString(),file);
            System.out.println(sb.toString());
        }
        System.out.println("===================================");



    }*/
    }
}

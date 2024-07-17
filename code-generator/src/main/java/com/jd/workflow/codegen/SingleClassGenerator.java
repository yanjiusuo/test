package com.jd.workflow.codegen;

import com.jd.workflow.codegen.directive.TrimLastComma;
import com.jd.workflow.codegen.dto.GeneratedCodeDto;
import com.jd.workflow.codegen.language.Language;
import com.jd.workflow.codegen.model.ClassModelCollector;
import com.jd.workflow.codegen.model.GroupInterfaceData;
import com.jd.workflow.codegen.model.MethodModel;
import com.jd.workflow.codegen.model.type.IClassModel;
import com.jd.workflow.codegen.model.type.ReferenceClassModel;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.RefObjectJsonType;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
public class SingleClassGenerator {
    private void collectGenericType(JsonType jsonType, Set<String> genericTypes){
        if(jsonType instanceof RefObjectJsonType && StringUtils.isBlank(((RefObjectJsonType) jsonType).getRefName())
         && StringUtils.isNotBlank(jsonType.getTypeVariableName())
        ){
            genericTypes.add(jsonType.getTypeVariableName());
        }
        if(jsonType instanceof ComplexJsonType){
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                collectGenericType(child,genericTypes);
            }
        }
    }
    public String generateModel(ObjectJsonType jsonType,String type){
        Set<String> typeNames = new HashSet<>();
        collectGenericType(jsonType,typeNames);
        ClassModelCollector collector = new ClassModelCollector();
        MethodModel methodModel = new MethodModel();
        IClassModel classModel = ApiModelHelper.makeNew(jsonType.getClassName(), jsonType, collector, methodModel);
        for (String typeName : typeNames) {
            ReferenceClassModel typeModel = new ReferenceClassModel();
            typeModel.setClassName(typeName);
            classModel.getGenericTypes().add(typeModel);
        }
        classModel.getFormalParams().addAll(typeNames);
        Map<String,String> typeMap = new HashMap<>();
        typeMap.put("ts","typescript");
        typeMap.put("java","java");
        try {
            Map<String,Object> variableMap = new HashMap<>();
            variableMap.put("classModel",classModel);
            return  generateCode(variableMap,typeMap.get(type));
        } catch (Exception e) {
            throw new BizException("生成代码失败:"+e.getMessage(),e);
        }
    }

    /**
     * 生成单个方法的代码
     * @return
     */
    public List<FileCode> generateSingleMethodCode(HttpMethodModel methodModel, String sdkType){
        CodeGenerator codeGenerator = new CodeGenerator();
         String languageType = Language.from(sdkType).getType();
         codeGenerator.setType(languageType);
        List<GroupInterfaceData> list = new ArrayList<>();
        GroupInterfaceData groupInterfaceData = new GroupInterfaceData();
        list.add(groupInterfaceData);
        groupInterfaceData.setMethods(Collections.singletonList(methodModel));
        groupInterfaceData.setGroupName("DefaultInterface");
        groupInterfaceData.setGroupDesc("默认");
        StringBuilder sb = new StringBuilder();
        try {
            List<FileCode> codes = codeGenerator.generateCode(list, new GenerateConfig());


            return codes;
        } catch (Exception e) {
            log.error("生成代码失败:methodModel={}",methodModel,e);
            throw new BizException("生成代码失败:",e);
        }
    }

    public String generateCode(Object model,String type) throws Exception {
        //1.创建一个模板文件
        //2.创建一个Configuration对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setSharedVariable("trimLastComma",new TrimLastComma());
        //configuration.setObjectWrapper(new BeansWrapper());
        //允许使用内建函数
        //configuration.setAPIBuiltinEnabled(true);
        //3.设置模板所在的路径

        //configuration.setDirectoryForTemplateLoading(getTemplateDir());
        configuration.setClassLoaderForTemplateLoading(this.getClass().getClassLoader(), "codegen/"+type+"/template");
        //4.设置模板的字符集，一般utf-8
        configuration.setDefaultEncoding("utf-8");
        //5.使用Configuration对象加载一个模板文件，需要指定模板文件的文件名。
        Template modelTemplate = configuration.getTemplate("class-model.ftl");
//		Template template = configuration.getTemplate("student.ftl");
        //6.创建一个数据集，可以是pojo也可以是map，推荐使用map

        return generate(modelTemplate,model);
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
}

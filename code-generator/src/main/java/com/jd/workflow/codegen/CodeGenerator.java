package com.jd.workflow.codegen;

import com.jd.workflow.codegen.directive.TrimLastComma;
import com.jd.workflow.codegen.language.Language;
import com.jd.workflow.codegen.model.*;
import com.jd.workflow.codegen.model.type.*;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class CodeGenerator {


    private List<ApiModel> apiGroups;
    //通用泛型类放到这个里面
    private List<EntityClassModel> genericClassModels = new ArrayList<>();
    // 类按照分组重新排列后放到这个里面
    private  Map<ApiModel, Set<EntityClassModel>> groupClassModels = new HashMap<>();

    private String type;

    GroupModels genericModels;
    Map<ApiModel,GroupModels> groupModels;

    Map<String,String> className2SavedPath = new HashMap();

    ClassModelCollector collector = new ClassModelCollector();

    public ClassModelCollector getCollector() {
        return collector;
    }

    private GroupModels getGenericModels() {
        return genericModels;
    }

    private void setGenericModels(GroupModels genericModels) {
        this.genericModels = genericModels;
    }

    private Map<ApiModel, GroupModels> getGroupModels() {
        return groupModels;
    }

    private void setGroupModels(Map<ApiModel, GroupModels> groupModels) {
        this.groupModels = groupModels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private void init() {
        collector.init();

        groupClassModels = buildGroupClassModel();

        for (Map.Entry<ApiModel, Set<EntityClassModel>> apiModelSetEntry : groupClassModels.entrySet()) {
            Iterator<EntityClassModel> iterator = apiModelSetEntry.getValue().iterator();
            while (iterator.hasNext()){
                EntityClassModel classModel = iterator.next();
                if(!ObjectHelper.isEmpty(classModel.getFormalParams()) || classModel.isHidden()){ // 移除遍历
                    iterator.remove();
                }
            }
        }
        for (Map.Entry<ApiModel, Set<EntityClassModel>> entry : groupClassModels.entrySet()) {
            for (EntityClassModel entityClassModel : entry.getValue()) {
                entityClassModel.setApiModel(entry.getKey());
            }
        }
        genericClassModels = collector.genericEntityModels();
        genericModels = new GroupModels(genericClassModels);


        groupModels = new HashMap<>();
        for (Map.Entry<ApiModel, Set<EntityClassModel>> entry : groupClassModels.entrySet()) {
            GroupModels model = new GroupModels(entry.getValue());
            groupModels.put(entry.getKey(), model);
        }

    }


    private  void setCommonModelPathPrefix(String prefix){
        for (EntityClassModel genericClassModel : genericClassModels) {
            String savedPath = prefix+"/common.ts";
            className2SavedPath.put(genericClassModel.getClassName(),savedPath);
            //setEntityModelSavedPath(genericClassModel,savedPath);
        }
    }

    private void setGroupModelPathPrefix(String prefix) {
        for (Map.Entry<ApiModel, Set<EntityClassModel>> entry : groupClassModels.entrySet()) {
            for (EntityClassModel entityClassModel : entry.getValue()) {
                String savedPath = prefix+"/"+entry.getKey().getOriginalName()+".ts";
                className2SavedPath.put(entityClassModel.getClassName(),savedPath);
            }
        }

    }

    private void setApiPathPrefix(String prefix){
        for (ApiModel apiGroup : apiGroups) {
            apiGroup.setSavedPath(prefix+"/"+apiGroup.getOriginalName()+"Api.ts" );
        }
       /* for (Map.Entry<ApiModel, GroupModels> entry : groupModels.entrySet()) {
            entry.getKey().setSavedPath(prefix+"/"+entry.getKey().getOriginalName()+"Api.ts" );
        }*/
    }

    public Map<String, List<EntityClassModel>> getImportedClassModel() {

        return null;
    }

    /**
     * 將classModel重新分组。按照分组引用数量重新排序
     *
     * @return
     */
    private Map<ApiModel, Set<EntityClassModel>> buildGroupClassModel() {
        Map<EntityClassModel, Map<ApiModel, Integer>> apiGroupReferenceCount = new HashMap<>();
        for (ApiModel api : apiGroups) {

            for (MethodModel method : api.getMethods()) {
                if (method.getReturnType() != null) {
                    collectReference(apiGroupReferenceCount, api, method.getReturnType().getType(), new HashSet<>());
                }
                for (Param input : method.getInputs()) {
                    collectReference(apiGroupReferenceCount, api, input.getType(), new HashSet<>());
                }
            }

        }
        Map<ApiModel, Set<EntityClassModel>> result = new HashMap<>();
        for (Map.Entry<EntityClassModel, Map<ApiModel, Integer>> entry : apiGroupReferenceCount.entrySet()) {
            int value = 0;
            ApiModel max = null;
            for (Map.Entry<ApiModel, Integer> groupEntry : entry.getValue().entrySet()) {
                if (value < groupEntry.getValue()) {
                    max = groupEntry.getKey();
                    value = groupEntry.getValue();
                }
            }
            Set<EntityClassModel> set = result.computeIfAbsent(max, vs -> new HashSet<>());
            set.add(entry.getKey());
        }
        return result;
    }

    public boolean isGenericEntityClass(EntityClassModel classModel) {
        return classModel.getGenericTypes().isEmpty();
    }

    private void collectReference(Map<EntityClassModel, Map<ApiModel, Integer>> apiGroupReferenceCount, ApiModel group, IClassModel classModel, Set<IClassModel> processed) {
        if (processed.contains(classModel)) return;
        processed.add(classModel);

        if (classModel instanceof EntityClassModel) {
            GenericClassModel genericClassModel = collector.get(classModel.getClassName());

            EntityClassModel entityRelatedModel = (EntityClassModel) genericClassModel.getGenericClassModel();
           /* if(apiGroupReferenceCount.get(entityRelatedModel) != null){
                EntityClassModel item = (EntityClassModel) ((HashMap) apiGroupReferenceCount).keySet().toArray(new EntityClassModel[0])[0];
                boolean equals = item.equals(entityRelatedModel);
            }*/
                Map<ApiModel, Integer> ref = apiGroupReferenceCount.computeIfAbsent(entityRelatedModel, key -> {
                    return new HashMap<>();
                });
                Integer count = ref.computeIfAbsent(group, key -> {
                    return 0;
                });
                count++;
                ref.put(group, count);
            for (IClassModel genericType : classModel.getGenericTypes()) {
                collectReference(apiGroupReferenceCount, group, (IClassModel) genericType, processed);
            }
            for (FieldModel field : ((EntityClassModel) classModel).getFields()) {
                if (field.getType() instanceof IClassModel) {
                    collectReference(apiGroupReferenceCount, group, (IClassModel) field.getType(), processed);
                }else if(field.getType() instanceof TypeVariable){
                    collectReference(apiGroupReferenceCount, group, ((TypeVariable) field.getType()).getBindType(), processed);
                }

            }

        }else if(classModel instanceof ArrayClassModel){
            for (IType child : ((ArrayClassModel) classModel).getChildren()) {
                if(child instanceof IClassModel){
                    collectReference(apiGroupReferenceCount, group, (IClassModel) child, processed);
                }



            }
        }else if(classModel instanceof ReferenceClassModel){
            for (IClassModel genericType : classModel.getGenericTypes()) {
                collectReference(apiGroupReferenceCount, group, (IClassModel) genericType, processed);
            }
        }
    }
    /**
     * Copies a directory from a jar file to an external directory.
     */
    private static void copyResourcesToDirectory(JarFile fromJar, String jarDir, String destDir)
            throws IOException {
        for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(jarDir + "/") && !entry.isDirectory()) {
                File dest = new File(destDir + "/" + entry.getName().substring(jarDir.length() + 1));
                File parent = dest.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                FileOutputStream out = new FileOutputStream(dest);
                InputStream in = fromJar.getInputStream(entry);

                try {
                    byte[] buffer = new byte[8 * 1024];

                    int s = 0;
                    while ((s = in.read(buffer)) > 0) {
                        out.write(buffer, 0, s);
                    }
                } catch (IOException e) {
                    throw new IOException("Could not copy asset from jar file", e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                    try {
                        out.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private List<ApiModel> getApiGroups() {
        return apiGroups;
    }

    public String generateCode(Object model,String templateName) {
        try{
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
            Template modelTemplate = configuration.getTemplate(templateName);
//		Template template = configuration.getTemplate("student.ftl");
            //6.创建一个数据集，可以是pojo也可以是map，推荐使用map

            return generate(modelTemplate,model);
        }catch (Exception e){
            log.error("code.err_generate_code",e);
            throw new BizException("生成代码失败",e);
        }

    }
    private String generate(Template template,Object model){
        StringWriter writer = new StringWriter();
        try {
            template.process(model, writer);
        } catch (TemplateException e) {
            log.error("code.err_generate_code",e);
        } catch (IOException e) {
            log.error("code.err_generate_code",e);
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
    private void write(String code,File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        IOUtils.write(code, fileWriter);
        fileWriter.flush();

    }
    private ClassPathResource getTemplateDir() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("codegen/typescript/template/interface.ftl");
        return classPathResource;
    }
    public void generateCode(File rootFile,List<GroupInterfaceData> groupData,GenerateConfig config) throws Exception {
        List<FileCode> codes = generateCode(groupData, config);

        ClassPathResource templateDir = getTemplateDir();
        if(templateDir.getURL().getPath().contains(".jar!")){
            String urlPath = StringHelper.firstPart(templateDir.getURL().getPath(),'!');
            if(urlPath.startsWith("file:")){
                urlPath = urlPath.substring("file:".length());
            }
            copyResourcesToDirectory(new JarFile(urlPath),"codegen/typescript/template/config",rootFile.getAbsolutePath());
        }else{
            copyDir(new File(templateDir.getFile(),"config"),rootFile);
        }
        for (FileCode code : codes) {
            File file = new File(rootFile,code.getFilePath());
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            write(code.getCode(),file);
        }





    }


    public List<FileCode> generateCode(List<GroupInterfaceData> groupData,GenerateConfig config) throws Exception {
        Guard.notEmpty(type,"类型不可为空");
        apiGroups = new ArrayList<>();
        for (GroupInterfaceData group : groupData) {
            if(group.getMethods().isEmpty()) continue;
            ApiModel api = new ApiModel();
            if(StringHelper.isEmpty(group.getPkgName())) {
                api.setClassName(group.getGroupName());
            }else{
                api.setClassName(group.getPkgName()+"."+group.getGroupName());
            }

            api.setName(group.getGroupName());
            api.setDesc(group.getGroupDesc());
            for (HttpMethodModel method : group.getMethods()) {
                api.getMethods().add(ApiModelHelper.toMethod(method, api,collector));
            }
            apiGroups.add(api);
        }
        init();
        setGroupModelPathPrefix(config.getGroupModelPath());
        setCommonModelPathPrefix(config.getCommonModelPath());
        setApiPathPrefix(config.getApiPath());
        genericModels.setClassName2SavedPath(className2SavedPath);
        for (Map.Entry<ApiModel, GroupModels> entry : groupModels.entrySet()) {
            entry.getValue().setClassName2SavedPath(className2SavedPath);
            entry.getValue().getJsImports();
        }
        for (ApiModel apiGroup : apiGroups) {
            for (MethodModel method : apiGroup.getMethods()) {
                method.setClassName2SavedPath(className2SavedPath);
                method.getJsImports();
            }
        }
        List<FileCode> result = LanguageFactory.getLanguageRender(Language.from(type),this).render(
                getApiGroups(),genericModels,getGroupModels(),config
        );



        return result;
    }

}

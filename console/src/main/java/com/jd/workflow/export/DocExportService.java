package com.jd.workflow.export;

import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.Lists;
import com.jd.common.util.StringUtils;
import com.jd.security.watermark.docwatermarksdk.config.DocWatermarkSdkConfig;
import com.jd.security.watermark.docwatermarksdk.entity.TextTheme;
import com.jd.security.watermark.docwatermarksdk.service.WatermarkPdfService;
import com.jd.security.watermark.docwatermarksdk.service.impl.WatermarkPdfServiceImpl;
import com.jd.workflow.codegen.CodeGenerator;
import com.jd.workflow.codegen.GenerateConfig;
import com.jd.workflow.codegen.language.Language;
import com.jd.workflow.codegen.model.GroupInterfaceData;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.entity.ExampleScence;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.RefJsonTypeService;
import com.jd.workflow.console.service.impl.ExampleScenceServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DocExportService {
    @Autowired
    IInterfaceMethodGroupService groupService;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    ExampleScenceServiceImpl exampleScenceService;
    @Autowired
    IInterfaceVersionService interfaceVersionService;
    @Autowired
    RefJsonTypeService refJsonTypeService;

    @Autowired
    private ScheduledThreadPoolExecutor defaultScheduledExecutor;

    private void collectModel(GroupSortModel group, List<GroupExportModel> result) {

        if (ObjectHelper.isEmpty(group.getChildren())) return;

        GroupExportModel exportModel = new GroupExportModel();
        exportModel.setGroup(group);
        exportModel.setMethodModels(group.childMethods());
        result.add(exportModel);

        List<GroupSortModel> groupSortModels = group.childGroups();
        for (GroupSortModel groupSortModel : groupSortModels) {
            collectModel(groupSortModel, result);
        }


    }

    public String replaceLevel(String docInfo, int level) {
        if (StringHelper.isEmpty(docInfo)) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docInfo.length(); i++) {

            char c = docInfo.charAt(i);
            sb.append(c);
            if (c == '#' && (
                    i == docInfo.length() - 1 || docInfo.charAt(i + 1) != '#'
            )) {
                sb.append(StringHelper.repeat("#", level));
            }
        }
        return sb.toString();
    }

    public String escapeMarkdown(MethodManageDTO dto) {
        if ("md".equals(dto.getDocConfig().getDocType())) {
            return replaceLevel(dto.getDocInfo(), 3);
        }
        return dto.getDocInfo() == null ? "" : dto.getDocInfo();
    }

    public String md2html(String markdown, String htmlTemplatePath) {

        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "template");
        //3.设置模板所在的路径
        //4.设置模板的字符集，一般utf-8
        configuration.setDefaultEncoding("utf-8");

        List<Markdown2Html.MdAchor> archors = new ArrayList<>();
        String right = Markdown2Html.render(markdown, archors);
        try {
            freemarker.template.Template anchorsTemplate = configuration.getTemplate("anchors.ftl");
            freemarker.template.Template htmlTemplate = configuration.getTemplate(htmlTemplatePath);
            Map<String, Object> vars = new HashMap<>();
            vars.put("anchors", archors);
            String left = generate(anchorsTemplate, vars);

            Map<String, Object> newVars = new HashMap<>();
            newVars.put("left", left);
            newVars.put("right", right);
            return generate(htmlTemplate, newVars);
        } catch (Exception e) {
            throw new BizException("导出失败,请联系管理员", e);
        }

    }

    public String exportHtml(List<InterfaceSortModel> sortModels) {
        String md = exportMd(sortModels);
        return md2html(md, "html.ftl");
    }

    public void exportPdf(List<InterfaceSortModel> sortModels, Function<File, Void> processor) {
        String md = exportMd(sortModels);
        String html = md2html(md, "pdf.ftl");
        File pdfFile = null;
        File watermarkPdfFile = null;
        try {
            File file = ResourceUtils.getFile("classpath:font/simsun.ttc");
            String fontPath = file.getPath() + ",0";
            Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), ".pdf");
            pdfFile = tempFile.toFile();

            Html2PdfUtils.convertToPdf(new ByteArrayInputStream(html.getBytes("utf-8")),
                    "藏经阁-在线联调", fontPath, new FileOutputStream(pdfFile));

            Path watermarkPdfPath = Files.createTempFile(UUID.randomUUID().toString(), ".pdf");
            watermarkPdfFile = watermarkPdfPath.toFile();
            addWatermark(pdfFile, new FileOutputStream(watermarkPdfFile));


            processor.apply(watermarkPdfFile);


        } catch (IOException e) {
            throw new StdException("导出失败", e);
        } finally {
            if (pdfFile != null && pdfFile.exists()) {
                pdfFile.delete();
            }

            if (watermarkPdfFile != null && watermarkPdfFile.exists()) {
                watermarkPdfFile.delete();
            }
        }
    }

    public void exportPdf(List<InterfaceSortModel> sortModels, OutputStream os) {
        exportPdf(sortModels, file -> {
            try {
                IOUtils.copy(new FileInputStream(file), os);
            } catch (IOException e) {
                throw new StdException("导出失败", e);
            }
            return null;
        });
    }

    private void addWatermark(File file, OutputStream os) {
        try {
            DocWatermarkSdkConfig sdkConfig = new DocWatermarkSdkConfig();
            sdkConfig.setUrl("http://watermark-internal.jd.com/api/data/watermark");
            sdkConfig.setAppCode("data-flow");
            sdkConfig.setToken("91ex9Xdo5j4Y2RDdtBqaNF");
            WatermarkPdfService service = new WatermarkPdfServiceImpl(sdkConfig);
            FileInputStream inputStream = new FileInputStream(file);

            TextTheme textTheme = TextTheme.builder()
                    .font(new Font("Times New Romans", Font.PLAIN, 12))
                    .fontColor(new Color(0xbfbfbf))
                    .rotation(-25.0)
                    .density(1.0) // 密度
                    .textOpacity(0.1f)
                    .isBranch(true)
                    .additionalText("藏经阁一体化")
                    .build();
            service.addWatermark(inputStream, os,
                    UserSessionLocal.getUser().getUserId(), textTheme
            );
        } catch (Exception e) {
            throw new BizException("添加水印失败", e);
        }

    }

    private List<GroupInterfaceData> getGroupInterfaces(List<TreeSortModel> sortModels) {
        List<GroupExportModel> result = new ArrayList<>();

        GroupSortModel sortModel = new GroupSortModel();
        sortModel.setName("默认分组");
        sortModel.getChildren().addAll(sortModels);
        collectModel(sortModel, result);
        List<Long> methodIds = result.stream().map(vs -> vs.getMethodModels()).flatMap(new Function<List<MethodSortModel>, Stream<MethodSortModel>>() {
            @Override
            public Stream<MethodSortModel> apply(List<MethodSortModel> methodSortModels) {
                return methodSortModels.stream();
            }
        }).map(vs -> vs.getId()).collect(Collectors.toList());
        if (methodIds.isEmpty()) {
            throw new BizException("未选中任何方法");
        }
        List<MethodManage> methodManages = methodManageService.listByIds(methodIds);

        for (MethodManage methodManage : methodManages) {
//            methodManageService.initMethodDocConfig(methodManage,true);
            methodManageService.initContentObject(methodManage);
            InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());
            refJsonTypeService.initMethodRefInfos(methodManage, interfaceManage.getAppId());
        }
        methodManageService.initMethodDeltaInfos(methodManages);
        List<GroupInterfaceData> list = new ArrayList<>();
        for (MethodManage methodManage : methodManages) {

            GroupInterfaceData groupInterfaceData = new GroupInterfaceData();
            list.add(groupInterfaceData);
            groupInterfaceData.setMethods(Collections.singletonList((HttpMethodModel)methodManage.getContentObject()));
            groupInterfaceData.setGroupName("DefaultInterface");
            groupInterfaceData.setGroupDesc("默认");

        }
        return list;
//        Map<String, List<MethodManageDTO>> id2Methods = methodManages.stream().map(method -> MethodManageDTO.from(method)).collect(Collectors.groupingBy(MethodManageDTO::getId));
//        for (GroupExportModel item : result) {
//            item.init(id2Methods);
//        }
//        return result.stream().map(vs -> {
//            GroupInterfaceData data = new GroupInterfaceData();
//            data.setPkgName("com.jd.test.flow");
//            String groupName = "apiDefault";
//            if (vs.getGroup().getId() != null) {
//                groupName = vs.getGroup().getName();
//            }
//            data.setGroupName(groupName);
//            data.setGroupDesc(vs.getGroup().getName());
//            List<HttpMethodModel> methods = vs.getMethods().stream().map(item -> {
//                return (HttpMethodModel) item.getContentObject();
//            }).collect(Collectors.toList());
//            data.setMethods(methods);
//            return data;
//        }).collect(Collectors.toList());

    }

    public File exportSdk(String sdkType, List<TreeSortModel> sortModels) {
        String uuid = UUID.randomUUID().toString();
        File rootFile = null;
        CodeGenerator generator = new CodeGenerator();
        generator.setType(Language.from(sdkType).getType());
        try {
            Path tempDirectory = Files.createTempDirectory(uuid);
            rootFile = tempDirectory.toFile();
            System.out.println(rootFile.getAbsolutePath());
            generator.generateCode(rootFile, getGroupInterfaces(sortModels), new GenerateConfig());
            File zipData = ZipUtil.zip(rootFile);
            return zipData;
        } catch (Exception e) {
            throw new BizException("导出失败", e);
        } finally {
            if (rootFile != null) {
                rootFile.delete();
            }
        }
    }

    public List<InterfaceExportModel> collectVarModels(GroupResolveDto dto, List<InterfaceSortModel> sortModels) {
        List<InterfaceExportModel> exportModels = new ArrayList<>();
        for (InterfaceSortModel sortModel : sortModels) {
            exportModels.add(collectMethodModel(sortModel));
        }
        return exportModels;
    }

    public List<MethodManageDTO> collectSpecialVersionMethods(List<IdAndVersion> idAndVersions) {
        List<Future> futures = new ArrayList<>();
        List<MethodManageDTO> result = new ArrayList<>();
        for (IdAndVersion idAndVersion : idAndVersions) {
            Future<?> future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        MethodManageDTO method = interfaceVersionService.getVersionMethod(idAndVersion.getVersion(), idAndVersion.getId());
                        if (method == null) return;
                        result.add(method);
                    } catch (Exception e) {
                        log.error("doc.err_collect_method:methodId={},version={}", idAndVersion.getId(), idAndVersion.getVersion(), e);
                    }
                }
            });
            futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new BizException("获取方法失败", e);
            }
        }
        return result;
    }

    private InterfaceExportModel collectMethodModel(InterfaceSortModel interfaceSortModel) {
        InterfaceManage manage = interfaceManageService.getById(interfaceSortModel.getId());
        InterfaceExportModel exportModel = new InterfaceExportModel();

        // 将interfaceSortModel展平
        List<GroupExportModel> result = new ArrayList<>();

        GroupSortModel groupSortModel = new GroupSortModel();
        groupSortModel.setName("默认分组");
        if (StringUtils.isNotBlank(interfaceSortModel.getName())) {
            groupSortModel.setName(interfaceSortModel.getName());
        }
        groupSortModel.getChildren().addAll(interfaceSortModel.getChildren());
        collectModel(groupSortModel, result);

        List<IdAndVersion> methodIdAndVersions = result.stream().map(vs -> vs.getMethodModels()).flatMap(new Function<List<MethodSortModel>, Stream<MethodSortModel>>() {
            @Override
            public Stream<MethodSortModel> apply(List<MethodSortModel> methodSortModels) {
                return methodSortModels.stream();
            }
        }).map(vs -> {
            return new IdAndVersion(vs.getId(), vs.getVersion());
        }).collect(Collectors.toList());



        if (methodIdAndVersions.isEmpty()) {
            throw new BizException("未选中任何方法");
        }

        List<Long> nonVersionMethodIds = methodIdAndVersions.stream().filter(item -> StringUtils.isBlank(item.getVersion())).map(item -> item.getId()).collect(Collectors.toList());
        List<IdAndVersion> versionMethods = methodIdAndVersions.stream().filter(item -> StringUtils.isNotBlank(item.getVersion())).collect(Collectors.toList());


        List<Long> methodIds = methodIdAndVersions.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<ExampleScence> exampleScences = exampleScenceService.getAllScene(methodIds);
        Map<Long, List<ExampleScence>> methodId2Scene = exampleScences.stream().collect(Collectors.groupingBy(ExampleScence::getMethodId));


        List<MethodManage> methodManages = new ArrayList<>();
        if (!nonVersionMethodIds.isEmpty()) {
            methodManages = methodManageService.listByIds(nonVersionMethodIds);
            methodManageService.initMethodRefAndDelta(methodManages, manage.getAppId());
            for (MethodManage methodManage : methodManages) {
                methodManageService.initMethodDocConfig(methodManage, true);
                List<ExampleScence> scenes = methodId2Scene.get(methodManage.getId());
                if(!ObjectHelper.isEmpty(scenes)){
                    methodManage.getDocConfig().setInputExample(scenes.get(0).getInputExample());
                    methodManage.getDocConfig().setOutputExample(scenes.get(0).getOutputExample());
                }
            }
        }


        List<MethodManageDTO> allMethods = methodManages.stream().map(method -> MethodManageDTO.from(method)).collect(Collectors.toList());
        if (!versionMethods.isEmpty()) {
            allMethods.addAll(collectSpecialVersionMethods(versionMethods));
        }



        Map<String, List<MethodManageDTO>> id2Methods = allMethods.stream().collect(Collectors.groupingBy(MethodManageDTO::getId));
        for (GroupExportModel item : result) {
            item.init(id2Methods);
        }
        List<GroupExportModel> notEmptyGroups = result.stream().filter(vs -> vs.getMethods() != null).collect(Collectors.toList());
        exportModel.setGroups(notEmptyGroups);
        if (manage.getDocConfig() == null
        ) {
            exportModel.setDocType("md");
        } else {
            exportModel.setDocType(manage.getDocConfig().getDocType());
        }

        exportModel.setDocInfo(manage.getDocInfo());
        return exportModel;
    }

    public String exportMd(List<InterfaceSortModel> sortModels) {

        List<InterfaceExportModel> exportModels = sortModels.stream().map(item -> collectMethodModel(item)).collect(Collectors.toList());
        Map<String, Object> vars = new HashMap<>();
        vars.put("projectInfoName", "接口文档列表");
        /*if("md".equals(manage.getDocConfig().getDocType())){
            vars.put("projectDesc",replaceLevel(manage.getDocInfo(),2));
        }else{
            vars.put("projectDesc",manage.getDocInfo());
        }*/
        vars.put("projectDesc", "");

        vars.put("service", this);
        vars.put("interfaceModels", exportModels);
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "template");
        //3.设置模板所在的路径
        //4.设置模板的字符集，一般utf-8
        configuration.setDefaultEncoding("utf-8");
        //5.使用Configuration对象加载一个模板文件，需要指定模板文件的文件名。
        try {
            freemarker.template.Template apiTemplate = configuration.getTemplate("api.ftl");
            String ret = generate(apiTemplate, vars);
            /*System.out.println("===========================");
            System.out.println(ret);
            Writer fileWriter = new BufferedWriter(new FileWriter(new File("d:/tmp/docConfig/a.md")));
            fileWriter.write(ret);
            fileWriter.flush();*/
            return ret;
        } catch (IOException e) {
            throw new BizException("导出失败,请联系管理员", e);
        }


    }

    private String generate(freemarker.template.Template template, Object model) {
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

    private String tableHeader() {
        String thead = "  <thead class=\"ant-table-thead\">\n" +
                "    <tr>\n" +
                "        <th key=name>名称</th><th key=type>类型</th><th key=required>是否必须</th><th key=default>长度</th><th key=desc>备注</th>" +
                "    </tr>\n" +
                "    </thead>";
        return thead;
    }

    public String outputSingleTable(JsonType body) {
        return outputTable(Collections.singletonList(body));
    }

    public String outputTable(List<JsonType> body) {
        if (body == null || body.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String thead = "<table>\n" +

                tableHeader();
        String tbody = "<tbody className='ant-table-tbody'>" + tableBody(body, 0) + "</tbody></table>";
        sb.append(thead);
        sb.append(tbody);
        return sb.toString();
    }

    private static String nvl(String str) {
        return str == null ? "" : str;
    }

    private String tableCol(JsonType jsonType, int level) {
        String text = "";
        text += "<td><span style=\"padding-left:" + 20 * level + "px\"><span style=\"color: #8c8a8a\">" + (level > 0 ? "├─" : "") + jsonType.getName() + "</span></span></td>";
        text += "<td><span>" + jsonType.getType() + "</span></td>";
        text += "<td>" + (jsonType.isRequired() ? "是" : "否") + "</td>";
        String length = "";
        if (jsonType.getConstraint() != null) {
            if (jsonType.getConstraint().getMin() != null && jsonType.getConstraint().getMax() != null) {
                length = jsonType.getConstraint().getMin() + "~" + jsonType.getConstraint().getMax();
            } else if (jsonType.getConstraint().getMin() != null) {
                length = "> " + jsonType.getConstraint().getMin();
            } else if (jsonType.getConstraint().getMax() != null) {
                length = "< " + jsonType.getConstraint().getMax();
            }
        }
        text += "<td>" + length + "</td>";
        text += "<td><span style=\"white-space: pre-wrap\">" + nvl(jsonType.getDesc()) + "</span></td>";
        return text;
    }

    private String tableBody(List<JsonType> jsonTypes, int level) {
        String tpl = "";
        for (JsonType jsonType : jsonTypes) {
            tpl += "<tr key=" + jsonType.getName() + ">" + tableCol(jsonType, level) + "</tr>";
            if (jsonType instanceof ComplexJsonType) {
                tpl += tableBody(((ComplexJsonType) jsonType).getChildren(), level + 1);
            }
        }
        return tpl;
    }

    public static void main(String[] args) throws Exception {
       /* String mdContent = IOUtils.toString(new FileInputStream("D:\\tmp\\docConfig\\a.md"));
        DocExportService exportService = new DocExportService();
        String content = exportService.md2html(mdContent,"html.ftl");
        System.out.println("=================================================");
        System.out.println(content);*/
        String s = new StringBuilder().toString();
        System.out.println(s);
    }
}

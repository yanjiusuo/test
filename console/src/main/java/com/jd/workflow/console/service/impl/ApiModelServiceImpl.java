package com.jd.workflow.console.service.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.collect.Sets;
import com.jd.common.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.ApiModelMapper;

import com.jd.workflow.console.dto.ApiModelDTO;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.QueryModelsResponseDTO;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.model.ApiModelPageQuery;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.ApiModelDelta;
import com.jd.workflow.console.entity.model.ModelRefRelation;
import com.jd.workflow.console.entity.model.dto.ApiModelCountDto;
import com.jd.workflow.console.entity.requirement.RequirementAppModelSnapshot;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.service.ICodeGenerator;
import com.jd.workflow.console.service.RefJsonTypeService;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.console.service.model.*;
import com.jd.workflow.console.service.requirement.RequirementAppModelSnapshotService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.utils.ClassReference;
import com.jd.workflow.console.utils.DeltaHelper;
import com.jd.workflow.console.utils.DigestUtils;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */
@Slf4j
@Service
public class ApiModelServiceImpl extends ServiceImpl<ApiModelMapper, ApiModel> implements IApiModelService {

    @Autowired
    private IApiModelGroupService apiModelGroupService;
    @Autowired
    private IApiModelDeltaService apiModelDeltaService;

    @Autowired
    private IModelManageService modelManageService;

    @Autowired
    MethodModifyDeltaInfoService methodModifyDeltaInfoService;

    @Autowired
    RefJsonTypeService refJsonTypeService;
    @Autowired
    IModelRefRelationService modelRefRelationService;

    @Autowired
    List<ICodeGenerator> codeGenerators;
    @Autowired
    private RequirementInfoService requirementInfoService;

    @Autowired
    private RequirementAppModelSnapshotService requirementAppModelSnapshotService;

    @Override
    public Long addModel(ApiModelDTO apiModelDTO) {

        //校验唯一性
        checkName(apiModelDTO);
        //保存对象
        ApiModel apiModel = new ApiModel();
        BeanUtils.copyProperties(apiModelDTO, apiModel);
        apiModel.setYn(1);
        apiModel.setModifier(UserSessionLocal.getUser().getUserId());

        apiModel.setAutoReport(0);
        apiModel.setRefNames(refJsonTypeService.collectRefNames(apiModel.getContent()));
        apiModel.setDigest(DigestUtils.getJsonTypeDigest(apiModelDTO.getContent()));
        boolean result = save(apiModel);
        //去除引用的对象的字段
        if (Objects.nonNull(apiModelDTO.getContent())) {
            ApiModel before = getRefBeforeModel(apiModel);
            saveApiModelDelta(before, apiModel);
//            removeRefChild(apiModelDTO.getContent());
        }
        if (!result) {
            return 0L;
        }


        //保存目录结构
        MethodGroupTreeDTO methodGroupTreeDTO = apiModelGroupService.findMethodGroupTree(apiModelDTO.getAppId());

        methodGroupTreeDTO.insertMethod(apiModelDTO.getGroupId(), apiModel.getId());
        apiModelGroupService.modifyMethodGroupTree(methodGroupTreeDTO);

        return apiModel.getId();
    }

    public boolean initApiRef(ApiModel apiModel) {
        ApiModel before = getRefBeforeModel(apiModel);
        boolean hasRef = refJsonTypeService.initRefJsonType(before.getContent(),apiModel.getAppId());
        return hasRef;
    }

    public void saveApiModelDelta(ApiModel before, ApiModel apiModel) {
        //ApiModel before = getRefBeforeModel(apiModel);
        boolean hasRef = refJsonTypeService.initRefJsonType(before.getContent(),apiModel.getAppId());
        boolean autoReport = apiModel.getAutoReport() != null && apiModel.getAutoReport() == 1;
        if (hasRef || autoReport) {
            List<String> refNames = refJsonTypeService.collectRefNames(apiModel.getContent());
            List<String> beforeRefNames = before.getRefNames();

            modelRefRelationService.merge(beforeRefNames, refNames, apiModel.getAppId(), apiModel.getId(), ModelRefRelation.TYPE_MODEL);
            methodModifyDeltaInfoService.saveApiModelDelta(before, apiModel, !autoReport);
        }
    }

    private ApiModel getRefBeforeModel(ApiModel apiModel) {
        ApiModel before = new ApiModel();
        before.setId(apiModel.getId());
        before.setName(apiModel.getName());
        before.setContent(JsonUtils.parse(JsonUtils.toJSONString(apiModel.getContent()), JsonType.class));
        return before;
    }

    private void removeRefChild(List<JsonType> jsonTypeList) {

        for (JsonType jsonType : jsonTypeList) {
            if (jsonType instanceof RefObjectJsonType) {
                ((RefObjectJsonType) jsonType).getChildren().clear();
            }
            if (jsonType instanceof ObjectJsonType) {
                removeRefChild(((ObjectJsonType) jsonType).getChildren());
            }
        }

    }

    private void checkName(ApiModelDTO apiModelDTO) {
        LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper();
        lqw.eq(BaseEntity::getYn, 1)
                .eq(ApiModel::getAppId, apiModelDTO.getAppId())
                .eq(ApiModel::getName, apiModelDTO.getName())
                .ne(Objects.nonNull(apiModelDTO.getId()), ApiModel::getId, apiModelDTO.getId());

        List<ApiModel> apiModelList = list(lqw);
        if (CollectionUtils.isNotEmpty(apiModelList)) {
            throw new BizException("已存在同名对象");
        }

    }

    @Override
    public boolean editModel(ApiModelDTO apiModelDTO) {
        //校验
        if (Objects.isNull(apiModelDTO.getId()) || apiModelDTO.getId() == 0) {
            throw new BizException("id不能为空");
        }
        checkName(apiModelDTO);


        ApiModel beforeModel = getById(apiModelDTO.getId());
        if (Objects.isNull(beforeModel)) {
            throw new BizException("查不到对象");
        }

        ApiModel apiModel = new ApiModel();
        BeanUtils.copyProperties(apiModelDTO, apiModel);
        apiModel.setAutoReport(beforeModel.getAutoReport());
        apiModel.setModifier(UserSessionLocal.getUser().getUserId());
        List<ApiModel> apiModelList = Lists.newArrayList();
        apiModelList.add(apiModel);
        boolean autoReport = apiModel.getAutoReport() != null && apiModel.getAutoReport() == 1;
        // 初始化api模型的差量信息
        saveApiModelDelta(beforeModel, apiModel);
        if (autoReport) { // 自动上报的接口,内容不需要更新
            apiModel.setContent(null);
        } else {
            apiModel.setDigest(DigestUtils.getJsonTypeDigest(apiModelDTO.getContent()));
        }
        //对象保存
        return updateById(apiModel);
    }

    @Override
    public boolean removeModel(ApiModelDTO apiModelDTO) {
        //校验
        if (Objects.isNull(apiModelDTO.getId()) || apiModelDTO.getId() == 0) {
            throw new BizException("id不能为空");
        }
        ApiModel beforeModel = getById(apiModelDTO.getId());
        if (Objects.isNull(beforeModel)) {
            throw new BizException("查不到对象");
        }

        //删掉差量
        modelManageService.removeDeltaModel(beforeModel);

        modelRefRelationService.removeModel(beforeModel.getAppId(), beforeModel.getName());
        //保存目录结构
        MethodGroupTreeDTO methodGroupTreeDTO = apiModelGroupService.findMethodGroupTree(apiModelDTO.getAppId());
        GroupSortModel groupSortModel = methodGroupTreeDTO.getTreeModel().findMethodParent(apiModelDTO.getId());
        if (Objects.nonNull(groupSortModel)) {
            methodGroupTreeDTO.getTreeModel().removeMethod(apiModelDTO.getId(), groupSortModel.getId());
        }
        else {
            methodGroupTreeDTO.getTreeModel().removeMethod(apiModelDTO.getId(), null);
        }

        apiModelGroupService.modifyMethodGroupTree(methodGroupTreeDTO);

        //删除模型
        beforeModel.setYn(0);
        beforeModel.setModifier(UserSessionLocal.getUser().getUserId());
        return updateById(beforeModel);


    }

    @Override
    public ApiModelDTO getModelById(Long id, Long requireId) {
        if (Objects.isNull(id) || id == 0) {
            throw new BizException("查不到对象");
        }
        ApiModelDTO result = new ApiModelDTO();
        if (Objects.nonNull(requireId)) {

            RequirementInfo requirementInfo = requirementInfoService.getById(requireId);
            if (requirementInfo.getStatus() == 2) {
                LambdaQueryWrapper<RequirementAppModelSnapshot> lqw = new LambdaQueryWrapper();
                lqw.eq(RequirementAppModelSnapshot::getRequirementId, requireId).eq(RequirementAppModelSnapshot::getModelId, id);
                List<RequirementAppModelSnapshot> requirementAppModelSnapshotList = requirementAppModelSnapshotService.list(lqw);
                if (CollectionUtils.isNotEmpty(requirementAppModelSnapshotList)) {
                    BeanUtils.copyProperties(requirementAppModelSnapshotList.get(0), result);
                    return result;
                }
            }
        }


        ApiModel beforeModel = getById(id);

        if (Objects.isNull(beforeModel)) {
            throw new BizException("查不到对象");
        }
        boolean hasRef = initApiRef(beforeModel);
        if (hasRef || beforeModel.getAutoReport() != null && beforeModel.getAutoReport() == 1) {
            methodModifyDeltaInfoService.initApiModelDelta(beforeModel);
        }
        BeanUtils.copyProperties(beforeModel, result);

        return result;
    }

    @Override
    public Page<QueryModelsResponseDTO> queryModels(ApiModelPageQuery apiModelPageQuery) {
        Page<QueryModelsResponseDTO> result = getApiModelPage();
        MethodGroupTreeDTO methodGroupTreeDTO = apiModelGroupService.findMethodGroupTree(apiModelPageQuery.getAppId());
        Page<ApiModel> pvEntity = new Page<ApiModel>(apiModelPageQuery.getCurrent(), apiModelPageQuery.getSize());
        LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper();
        lqw.eq(BaseEntity::getYn, 1).eq(ApiModel::getAppId, apiModelPageQuery.getAppId());
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(apiModelPageQuery.getModelName())){
            lqw.like(ApiModel::getName,apiModelPageQuery.getModelName());
        }
        //查询所有指定group的子节点
        if (Objects.nonNull(apiModelPageQuery.getGroupId()) && apiModelPageQuery.getGroupId() > 0) {

            GroupSortModel groupSortModel = methodGroupTreeDTO.getTreeModel().findGroup(apiModelPageQuery.getGroupId());
            log.info("queryModels groupSortModel:{}", JSON.toJSONString(groupSortModel));
            if (Objects.nonNull(groupSortModel)) {
                List<MethodSortModel> methodSortModelList = groupSortModel.allChildMethods();
                if (CollectionUtils.isNotEmpty(methodSortModelList)) {
                    List<Long> idLists = methodSortModelList.stream().map(TreeSortModel::getId).collect(Collectors.toList());
                    lqw.in(ApiModel::getId, idLists);
                } else {
                    return result;
                }
            } else {
                return result;
            }
        }
        Page<ApiModel> page = this.page(pvEntity, lqw);
        List<QueryModelsResponseDTO> queryModelsResponseDTOList = Lists.newArrayList();
        for (ApiModel record : page.getRecords()) {
            QueryModelsResponseDTO queryModelsResponseDTO = new QueryModelsResponseDTO();
            BeanUtils.copyProperties(record, queryModelsResponseDTO);
            GroupSortModel groupSortModel = methodGroupTreeDTO.getTreeModel().findMethodParent(queryModelsResponseDTO.getId());
            if (Objects.nonNull(groupSortModel)) {
                queryModelsResponseDTO.setGroupId(groupSortModel.getId());
                queryModelsResponseDTO.setGroupName(groupSortModel.getName());
            }

            queryModelsResponseDTOList.add(queryModelsResponseDTO);
        }
        result.setRecords(queryModelsResponseDTOList);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());

        return result;


    }

    @Override
    public ObjectJsonType parseSql(String sql) {
        List<JsonType> jsonTypeList = Lists.newArrayList();
        Set<String> intSet = Sets.newHashSet("INT", "TINYINT");
        Set<String> longSet = Sets.newHashSet("BIGINT");
        Set<String> doubleSet = Sets.newHashSet("DECIMAL");
        ObjectJsonType tableObj = new ObjectJsonType();
        String dbType = JdbcConstants.MYSQL;
        try {
            List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
            for (SQLStatement sqlStatement : statementList) {
                if (sqlStatement instanceof MySqlCreateTableStatement) {
                    MySqlCreateTableStatement mySqlCreateTableStatement = (MySqlCreateTableStatement) sqlStatement;
                    if (mySqlCreateTableStatement.getTableSource().getExpr() instanceof SQLPropertyExpr) {
                        tableObj.setName(((SQLPropertyExpr) mySqlCreateTableStatement.getTableSource().getExpr()).getName().replaceAll("`", ""));
                        if (Objects.nonNull(mySqlCreateTableStatement.getComment())) {
                            if (mySqlCreateTableStatement.getComment() instanceof SQLCharExpr) {
                                tableObj.setDesc(((SQLCharExpr) mySqlCreateTableStatement.getComment()).getText().replaceAll("`", ""));
                            }
                        }
                    } else if (mySqlCreateTableStatement.getTableSource().getExpr() instanceof SQLIdentifierExpr) {
                        tableObj.setName(((SQLIdentifierExpr) mySqlCreateTableStatement.getTableSource().getExpr()).getName());
                        if (Objects.nonNull(mySqlCreateTableStatement.getComment())) {
                            if (mySqlCreateTableStatement.getComment() instanceof SQLCharExpr) {
                                tableObj.setDesc(((SQLCharExpr) mySqlCreateTableStatement.getComment()).getText().replaceAll("`", ""));
                            }
                        }
                    }

                    List<SQLColumnDefinition> sqlColumnDefinitionList = parseSQLColumnDefs(mySqlCreateTableStatement);
                    for (SQLColumnDefinition sqlColumnDefinition : sqlColumnDefinitionList) {
                        log.info("dataType:{}",sqlColumnDefinition.getDataType().getName().toUpperCase());
                        if (intSet.contains(sqlColumnDefinition.getDataType().getName().toUpperCase())) {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "integer", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        } else if (longSet.contains(sqlColumnDefinition.getDataType().getName().toUpperCase())) {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "long", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        } else if (doubleSet.contains(sqlColumnDefinition.getDataType().getName().toUpperCase())) {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "double", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        } else {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "string", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        }
                    }
                    tableObj.setChildren(jsonTypeList);
                    return tableObj;
                }
            }
        } catch (Exception ex) {
            log.error("parseSql error", ex);
        }
        tableObj.setChildren(jsonTypeList);
        return tableObj;
    }

    private boolean isMap(String typeStr) {
        typeStr = getActualTypeName(typeStr);
        return typeStr.endsWith("Map");
    }

    private boolean isArray(String typeStr) {
        typeStr = getActualTypeName(typeStr);
        return typeStr.endsWith("[]");
    }

    private boolean isCollection(String typeStr) {
        typeStr = getActualTypeName(typeStr);
        return typeStr.endsWith("Collection") || typeStr.endsWith("List") || typeStr.endsWith("Set");
    }

    private String getActualTypeName(String typeStr) {
        int index = typeStr.indexOf('<');
        if (index != -1) {
            typeStr = typeStr.substring(0, index);
        }
        return typeStr;
    }

    private String getType(String typeStr) {
        int index = typeStr.indexOf('<');
        if (index != -1) {
            typeStr = typeStr.substring(0, index);
        }
        String className = StringHelper.lastPart(typeStr, '.');
        String shortTypeName = TypeUtils.getSimpleTypeByShortTypeName(className);
        if (StringUtils.isNotBlank(shortTypeName)) {
            return shortTypeName;
        }
        if (isArray(typeStr)
                || isCollection(typeStr)
        ) {
            return "array";
        }
        return "object";
    }

    private String getComment(String comment) {
        if (StringUtils.isBlank(comment)) return "";
        return comment.replace("*", "").trim();
    }

    private String getFullClassName(String pkgName, String className) {
        if (StringUtils.isBlank(pkgName)) {
            return className;
        }
        return pkgName + "." + className;
    }

    // code参数是java类，以换行分割，请将code分割成多个java类
    private List<String> splitJavaBeans(String code) {
        List<String> javaBeanList = new ArrayList<>();
        String[] lines = code.split("\n");
        StringBuilder javaBean = new StringBuilder();
        for (String line : lines) {
            if (line.trim().startsWith("package")) {
                if (!javaBean.toString().isEmpty()) {
                    javaBeanList.add(javaBean.toString());
                    javaBean = new StringBuilder();
                }
                continue;
            }
            javaBean.append(line).append("\n");

        }
        if (!javaBean.toString().isEmpty()) {
            javaBeanList.add(javaBean.toString());
        }
        return javaBeanList;
    }

    private ObjectJsonType parseSingleJavaBean(String javaBean) {
        StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11);
        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        CompilationUnit unit = StaticJavaParser.parse(javaBean);
        Set<String> fields = new HashSet<>();
        List<ObjectJsonType> objectJsonTypes = new ArrayList<>();
        try {
            unit.findAll(ClassOrInterfaceDeclaration.class).forEach(classOrInterfaceDeclaration -> {
                ObjectJsonType jsonType = new ObjectJsonType();
                objectJsonTypes.add(jsonType);
                String classComment = getComment(classOrInterfaceDeclaration.getComment().isPresent() ? classOrInterfaceDeclaration.getComment().get().getContent() : "");
                ResolvedReferenceTypeDeclaration classType = classOrInterfaceDeclaration.resolve();
                String className = getFullClassName(classType.getPackageName(), classType.getName());
                jsonType.setClassName(className);
                jsonType.setName("root");
                jsonType.setDesc(classComment);

                List<JsonType> jsonTypes = new ArrayList<>();
                jsonType.setChildren(jsonTypes);
                classOrInterfaceDeclaration.getMethods().forEach(methodDeclaration -> {
                    if (methodDeclaration.getNameAsString().startsWith("get")
                            || methodDeclaration.getNameAsString().startsWith("is")
                            && (methodDeclaration.getTypeAsString().startsWith("boolean") || methodDeclaration.getTypeAsString().startsWith("Boolean"))
                    ) {
                        String name = methodDeclaration.getNameAsString().substring(3);
                        name = StringHelper.decapitalize(name);
                        String type = methodDeclaration.getTypeAsString();

                        String comment = methodDeclaration.getComment().isPresent() ? methodDeclaration.getComment().get().getContent() : "";
                        log.info("name:{},type:{},comment:{}", name, type, comment);
                        BuilderJsonType builderJsonType = new BuilderJsonType();

                        fields.add(name);
                        builderJsonType.setName(name);
                        ResolvedType resolvedType = null;
                        try {
                            resolvedType = methodDeclaration.resolve().getReturnType();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        initTypeInfoAndChild(resolvedType, type, builderJsonType);
                        builderJsonType.setDesc(getComment(comment));
                        jsonTypes.add(builderJsonType.toJsonType());
                    }

                });
                // 获取所有的字段，包括私有的(private、protected、默认以及public,非静态的以及native、final)
                classOrInterfaceDeclaration.getFields().forEach(fieldDeclaration -> {
                    String name = fieldDeclaration.getVariable(0).getNameAsString();
                    ResolvedType resolvedType = null;
                    try {
                        resolvedType = fieldDeclaration.resolve().getType();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String type = fieldDeclaration.getVariable(0).getTypeAsString();
                    String comment = fieldDeclaration.getComment().isPresent() ? fieldDeclaration.getComment().get().getContent() : "";
                    if (fields.contains(name)) {
                        return;
                    }
                    BuilderJsonType builderJsonType = new BuilderJsonType();
                    builderJsonType.setName(name);
                    initTypeInfoAndChild(resolvedType, type, builderJsonType);
                    builderJsonType.setDesc(getComment(comment));
                    jsonTypes.add(builderJsonType.toJsonType());
                    log.info("name:{},type:{},comment:{}", name, type, comment);
                });
            });
            if (objectJsonTypes.size() == 1) {
                return objectJsonTypes.get(0);
            }
            throw new BizException("java bean解析失败");
        } catch (Exception e) {
            log.error("apiModel.err_parse_java_bean:content={}", javaBean, e);
            throw new BizException("解析java bean失败", e);
        }
    }

    @Override
    public ObjectJsonType parseJavaBean(String javaBean) {
        Guard.notEmpty(javaBean, "内容不可为空");
        List<String> javaBeans = splitJavaBeans(javaBean);
        if (javaBeans.size() == 1) {
            return parseSingleJavaBean(javaBeans.get(0));
        }

        try {
            Map<String, ObjectJsonType> typeMap = new LinkedHashMap<>();
            for (String bean : javaBeans) {
                ObjectJsonType jsonType = parseSingleJavaBean(bean);
                typeMap.put(jsonType.getClassName(), jsonType);
            }

            ObjectJsonType jsonType = typeMap.values().iterator().next();
            initTypeReference(jsonType, typeMap);
            return jsonType;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("apiModel.err_parse_java_bean:content={}", javaBean, e);
            throw new BizException("解析java bean失败", e);
        }
    }

    private ICodeGenerator getCodeGenerator(String type) {
        for (ICodeGenerator codeGenerator : codeGenerators) {
            if (type.equals(codeGenerator.getType())) {
                return codeGenerator;
            }
        }
        return null;
    }

    @Override
    public String generateCode(Long modelId, String type) {
        Guard.notEmpty(modelId, "模型id不可为空");
        Guard.notEmpty(type, "type不可为空");
        ApiModelDTO apiModelDto = getModelById(modelId, null);
        JsonType content = apiModelDto.getContent();
        ICodeGenerator codeGenerator = getCodeGenerator(type);
        if (content == null) {
            return null;
        }
        if (StringUtils.isEmpty(content.getClassName())) {
            content.setClassName(apiModelDto.getName());
        }
        if (codeGenerator == null) {
            throw new BizException("不支持的类型：" + type);
        }

        return codeGenerator.generateEntityModel(content);
    }

    @Override
    public String generateCode(JsonType content,String name, String type) {
        if(!(content instanceof ObjectJsonType)){
            throw new BizException("只有对象类型才能生成代码" );
        }
        ICodeGenerator codeGenerator = getCodeGenerator(type);
        if (content == null) {
            return null;
        }
        if (StringUtils.isEmpty(content.getClassName())) {
            content.setClassName("Demo");
        }
        if (codeGenerator == null) {
            throw new BizException("不支持的类型：" + type);
        }

        return codeGenerator.generateEntityModel(content);
    }
    private List<ApiModel> queryAppModel(Long appId,String name){
        LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ApiModel::getName,name);
        lqw.eq(ApiModel::getAppId,appId);
        lqw.eq(ApiModel::getYn,1);
        lqw.orderByDesc(ApiModel::getModified);
        lqw.select(ApiModel::getId,ApiModel::getName,ApiModel::getAppId,ApiModel::getModified);
        return list(lqw);
    }

    /**
     * 校验查出来的结果是有效的
     * @param apiModels
     */
    private void validateValidateModels(List<ApiModel> apiModels){
        if(apiModels.size()<=1){
            throw new BizException("无效的模型");
        }
        String name = null;
        Long modified = null;

        for (ApiModel apiModel : apiModels) {
            if(name == null){
                name = apiModel.getName();
                modified = apiModel.getModified().getTime();
            }else{
                Guard.assertTrue(ObjectHelper.equals(name.toLowerCase(),apiModel.getName().toLowerCase()));
                Guard.assertTrue(modified>=apiModel.getModified().getTime());
            }

        }
    }
    @Override
    public boolean removeDuplicatedModel() {
        List<ApiModel> removedModels = new ArrayList<>();
        List<ApiModelCountDto> apiModelCountDtos = getBaseMapper().queryDuplicatedModels();
        for (ApiModelCountDto apiModelCountDto : apiModelCountDtos) {
            List<ApiModel> apiModels = queryAppModel(apiModelCountDto.getAppId(), apiModelCountDto.getName());
            validateValidateModels(apiModels);
            removedModels.addAll(apiModels.subList(1, apiModels.size()));

        }
        log.info("apiModel.remove_model:ids={}",removedModels.stream().map(item->item.getId()).collect(Collectors.toList()));
        for (ApiModel apiModel : removedModels) {
            apiModel.setYn(0);
            updateById(apiModel);
        }
        return true;
    }


    @Override
    public List<ApiModel> queryModels(List<String> names,Long appId) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper<>();
        lqw.in(ApiModel::getName, names);
        lqw.eq(ApiModel::getYn, 1);
        lqw.eq(ApiModel::getAppId, appId);
        List<ApiModel> models = list(lqw);
        List<Long> modelIds = models.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<ApiModelDelta> deltas = apiModelDeltaService.getByModelIds(modelIds);
        Map<Long, List<ApiModelDelta>> deltaMap = deltas.stream().collect(Collectors.groupingBy(ApiModelDelta::getApiModelId));
        for (ApiModel model : models) {
            List<ApiModelDelta> modelDelta = deltaMap.get(model.getId());
            if (modelDelta == null) {
                continue;
            }
            model.setContent(DeltaHelper.mergeDelta(model.getContent(), modelDelta.get(0).getContent()));
        }
        return models;
    }

    private void initTypeReference(JsonType jsonType, Map<String, ObjectJsonType> typeReference) {
        if (jsonType instanceof ObjectJsonType && typeReference.containsKey(jsonType.getClassName())) {
            ((ObjectJsonType) jsonType).setChildren(typeReference.get(jsonType.getClassName()).getChildren());
            if (StringUtils.isBlank(jsonType.getDesc())) {
                jsonType.setDesc(typeReference.get(jsonType.getClassName()).getDesc());
            }
        }
        if (jsonType.getGenericTypes() != null) {
            jsonType.getGenericTypes().forEach(genericType -> initTypeReference(genericType, typeReference));
        }
        if (jsonType instanceof ComplexJsonType) {
            ((ComplexJsonType) jsonType).getChildren().forEach(child -> initTypeReference(child, typeReference));
        }
    }

    public void initTypeInfoAndChild(ResolvedType resolvedType, String type, BuilderJsonType jsonType) {
      /*  if(resolvedType != null){
            type = resolvedType.describe();
        }*/
        String actualType = getType(type);
        jsonType.setType(actualType);
        if (resolvedType != null) {
            jsonType.setClassName(getActualTypeName(resolvedType.describe()));
        } else {
            SimpleParamType simpleParamType = SimpleParamType.from(actualType);
            if (simpleParamType != null) {
                jsonType.setClassName(simpleParamType.getType().getName());
            } else if (isCollection(type)) {
                jsonType.setClassName("java.util.List");
            } else {
                jsonType.setClassName(getActualTypeName(type));
            }

        }
        ClassReference reference = ClassReference.parse(type);

        List<JsonType> genericTypes = new ArrayList<>();
        for (ClassReference child : reference.getChildren()) {
            BuilderJsonType childType = new BuilderJsonType();
            childType.setType(getType(child.getClassName()));
            childType.setName("root");
            initTypeInfoAndChild(null, child.getClassName(), childType);
            genericTypes.add(childType);
        }
        jsonType.setGenericTypes(genericTypes);

        if (isMap(type)) { // 忽略了
            if (!genericTypes.isEmpty()) {
                genericTypes.get(0).setTypeVariableName("K");
                genericTypes.get(1).setTypeVariableName("V");
            }
        } else if (isArray(type)) {
            String actualArrTypeName = getActualTypeName(type);
            String arrComponentType = actualArrTypeName.substring(0, actualArrTypeName.indexOf('['));
            int arrLength = StringHelper.countChar(actualArrTypeName, '[');
            BuilderJsonType currentType = jsonType;
            for (int i = 0; i < arrLength - 1; i++) {
                BuilderJsonType arrItem = new BuilderJsonType();
                arrItem.setName("$$0");
                arrItem.setType("array");
                currentType.addChild(arrItem);
                currentType = arrItem;

            }
            BuilderJsonType componentType = new BuilderJsonType();
            componentType.setName("$$0");
            initTypeInfoAndChild(null, arrComponentType, componentType);
            currentType.addChild(componentType);
        } else if (isCollection(type)) {
            if (!genericTypes.isEmpty()) {
                genericTypes.get(0).setTypeVariableName("E");
            }
            for (JsonType genericType : genericTypes) {
                jsonType.addChild((BuilderJsonType) genericType);
            }
            //System.out.println(actualType);
        }
    }

    public void initParserTypeInfoAndChild(ResolvedType resolvedType, String type, BuilderJsonType jsonType) {
      /*  if(resolvedType != null){
            type = resolvedType.describe();
        }*/
        String actualType = getType(type);
        jsonType.setType(actualType);
        if (resolvedType != null) {
            jsonType.setClassName(getActualTypeName(resolvedType.describe()));
        } else {
            jsonType.setClassName(getActualTypeName(type));
        }
        ClassReference reference = ClassReference.parse(type);

        List<JsonType> genericTypes = new ArrayList<>();
        for (ClassReference child : reference.getChildren()) {
            BuilderJsonType childType = new BuilderJsonType();
            childType.setType(getType(child.getClassName()));
            childType.setName("root");
            initParserTypeInfoAndChild(null, child.getClassName(), childType);
            genericTypes.add(childType);
        }
        jsonType.setGenericTypes(genericTypes);

        if (isMap(type)) { // 忽略了
            if (!genericTypes.isEmpty()) {
                genericTypes.get(0).setTypeVariableName("K");
                genericTypes.get(1).setTypeVariableName("V");
            }
        } else if (isArray(type)) {
            String actualArrTypeName = getActualTypeName(type);
            String arrComponentType = actualArrTypeName.substring(0, actualArrTypeName.indexOf('['));
            int arrLength = StringHelper.countChar(actualArrTypeName, '[');
            BuilderJsonType currentType = jsonType;
            for (int i = 0; i < arrLength - 1; i++) {
                BuilderJsonType arrItem = new BuilderJsonType();
                arrItem.setName("$$0");
                arrItem.setType("array");
                currentType.addChild(arrItem);
                currentType = arrItem;

            }
            BuilderJsonType componentType = new BuilderJsonType();
            componentType.setName("$$0");
            initParserTypeInfoAndChild(null, arrComponentType, componentType);
            currentType.addChild(componentType);
        } else if (isCollection(type)) {
            if (!genericTypes.isEmpty()) {
                genericTypes.get(0).setTypeVariableName("E");
            }
            for (JsonType genericType : genericTypes) {
                jsonType.addChild((BuilderJsonType) genericType);
            }
            //System.out.println(actualType);
        }
    }

    private JsonType createJsonType(String name, String type, String comment) {
        SimpleJsonType simpleJsonType = new SimpleJsonType();
        simpleJsonType.setType(type);
        simpleJsonType.setName(name);
        simpleJsonType.setDesc(comment);
        simpleJsonType.setClassName(type);
        return simpleJsonType;
    }

    private List<SQLColumnDefinition> parseSQLColumnDefs(MySqlCreateTableStatement createTable) {
        return createTable.getTableElementList().stream()
                .filter(i -> i instanceof SQLColumnDefinition)
                .map(i -> (SQLColumnDefinition) i)
                .collect(Collectors.toList());
    }


    private Page<QueryModelsResponseDTO> getApiModelPage() {
        Page<QueryModelsResponseDTO> page = new Page<>();
        page.setRecords(null);
        page.setTotal(0);
        return page;
    }

    /**
     * 根据appId获取模型列表
     *
     * @param appId
     * @return
     */
    @Override
    public List<ApiModel> getModelsByAppId(Long appId) {
        LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper();
        lqw.select(ApiModel::getId, ApiModel::getName, ApiModel::getAppId, ApiModel::getAutoReport, ApiModel::getRefNames, ApiModel::getDigest, ApiModel::getPackagePath, ApiModel::getYn, ApiModel::getModified
                , ApiModel::getCreator, ApiModel::getCreated, ApiModel::getModifier);
        lqw.eq(BaseEntity::getYn, 1).eq(ApiModel::getAppId, appId);
        return list(lqw);
    }

    @Override
    public int queryModelCount(Long appId) {
        LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper();
        lqw.eq(BaseEntity::getYn, 1).eq(ApiModel::getAppId, appId);
        return count(lqw);

    }

    @Override
    public ApiModelDTO getByRefName(Long appId,String refName) {
        LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper();
        lqw.eq(BaseEntity::getYn, 1);
        lqw.eq(ApiModel::getAppId, appId);
        lqw.eq(ApiModel::getName, refName);
        List<ApiModel> models= list(lqw);
        if(CollectionUtils.isNotEmpty(models)){
            ApiModelDTO result=new ApiModelDTO();
            BeanUtils.copyProperties(models.get(0), result);
            return result;
        }
        return null;
    }


}

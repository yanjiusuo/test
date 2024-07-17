package com.jd.workflow.console.controller;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/19
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

import com.jd.matrix.generic.spi.SPI;
import com.jd.matrix.generic.spi.func.SimpleSPIReducer;
import com.jd.workflow.console.ability.ModelContentParseExtAbility;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.model.ApiModelPageQuery;
import com.jd.workflow.console.dto.model.RequireModelPageQuery;
import com.jd.workflow.console.entity.model.ApiModelTree;
import com.jd.workflow.console.service.model.IApiModelGroupService;
import com.jd.workflow.console.service.model.IApiModelService;
import com.jd.workflow.console.service.model.IApiModelTreeService;
import com.jd.workflow.domain.ModelContentParseModel;
import com.jd.workflow.matrix.ext.spi.ModelContentParseSPI;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/19
 */
@Slf4j
@RestController
@RequestMapping("/modelManage")
@UmpMonitor
@Api(value = "模型管理", tags = "模型管理")
public class ModelManageController {


    @Autowired
    private IApiModelGroupService apiModelGroupService;

    @Autowired
    IApiModelTreeService apiModelTreeService;

    @Autowired
    private IApiModelService apiModelService;

    @Autowired
    private ModelContentParseExtAbility modelContentParseExtAbility;

    @PostMapping("/addGroup")
    @ApiOperation(value = "新增分组")
    public CommonResult<Long> addGroup(@RequestBody ModelGroupDTO modelGroupDTO) {//,int type
        log.info("addGroup modelGroupDTO:{}", JsonUtils.toJSONString(modelGroupDTO));
        Long id = apiModelGroupService.addGroup(modelGroupDTO.getAppId(), modelGroupDTO.getName(), modelGroupDTO.getEnName(), modelGroupDTO.getParentId());
        return CommonResult.buildSuccessResult(id);
    }

    /**
     * 修改分组名称
     *
     * @param modelGroupDTO
     * @return
     */
    @PostMapping("/modifyGroupName")
    public CommonResult<Boolean> modifyGroupName(@RequestBody ModelGroupDTO modelGroupDTO) {
        boolean result = apiModelGroupService.modifyGroupName(modelGroupDTO);
        return CommonResult.buildSuccessResult(result);
    }

    @PostMapping("updateModelTree")
    public CommonResult<Boolean> updateModelTree(Long id,@RequestBody MethodGroupTreeModel treeModel) {
        ApiModelTree tree = apiModelTreeService.getById(id);
        tree.setTreeModel(treeModel);
        apiModelTreeService.updateById(tree);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 移除分组
     *
     * @return 是否移除成功：true-成功，false-失败
     */
    @RequestMapping("/removeGroup")
    public CommonResult<Boolean> removeNode(Long appId, Long groupId) {

        return CommonResult.buildSuccessResult(apiModelGroupService.removeGroup(appId, groupId));
    }

    @PostMapping("/addModel")
    @ApiOperation(value = "新增模型")
    public CommonResult<Long> addModel(@RequestBody ApiModelDTO apiModelDTO) {
        if (apiModelDTO.getAppId() == null || apiModelDTO.getAppId() <= 0) {
            throw new BizException("应用不能为空");
        }
        apiModelDTO.setAutoReport(0);

        return CommonResult.buildSuccessResult(apiModelService.addModel(apiModelDTO));
    }

    @PostMapping("/editModel")
    @ApiOperation(value = "修改模型")
    public CommonResult<Boolean> editModel(@RequestBody ApiModelDTO apiModelDTO) {
        if (apiModelDTO.getAppId() == null || apiModelDTO.getAppId() <= 0) {
            throw new BizException("应用不能为空");
        }

        return CommonResult.buildSuccessResult(apiModelService.editModel(apiModelDTO));
    }

    @PostMapping("/removeModel")
    @ApiOperation(value = "删除模型")
    public CommonResult<Boolean> removeModel(@RequestBody ApiModelDTO apiModelDTO) {
        if (apiModelDTO.getAppId() == null || apiModelDTO.getAppId() <= 0) {
            throw new BizException("应用不能为空");
        }
        if (apiModelDTO.getType() == 0) {
            return CommonResult.buildSuccessResult(apiModelService.removeModel(apiModelDTO));
        } else {

            return CommonResult.buildSuccessResult(apiModelGroupService.removeGroup(apiModelDTO.getAppId(), apiModelDTO.getId()));
        }
    }

    /**
     * @param id        模型id
     * @param requireId 需求id
     * @return
     */
    @GetMapping("/getById")
    @ApiOperation(value = "获取模型")
    public CommonResult<ApiModelDTO> getById(@RequestParam(required = true) Long id, @RequestParam(required = false) Long requireId) {

        return CommonResult.buildSuccessResult(apiModelService.getModelById(id, requireId));
    }

    @GetMapping("/getByRefName")
    @ApiOperation(value = "获取模型")
    public CommonResult<ApiModelDTO> getByRefName(@RequestParam(required = true) Long appId,@RequestParam(required = true) String refName) {

        return CommonResult.buildSuccessResult(apiModelService.getByRefName(appId,refName));
    }

    @GetMapping("/findModelChildTree")
    @ApiOperation(value = "获取目录树")
    public CommonResult<MethodGroupTreeDTO> findModelChildTree(@RequestParam(required = true) Long appId, @RequestParam(required = false) String modelName) {

        MethodGroupTreeDTO methodGroupTreeDTO = apiModelGroupService.findMethodGroupTree(appId, modelName,true);

        return CommonResult.buildSuccessResult(methodGroupTreeDTO);
    }

    @GetMapping("/findHasChildModelTree")
    @ApiOperation(value = "获取有子节点的目录树")
    public CommonResult<MethodGroupTreeDTO> findHasChildModelTree(@RequestParam(required = true) Long appId) {

        MethodGroupTreeDTO methodGroupTreeDTO = apiModelGroupService.findMethodGroupTree(appId);
        List<TreeSortModel> treeItems = methodGroupTreeDTO.getTreeModel().getTreeItems();
        List<TreeSortModel> resultTreeItems = new ArrayList<>();

        mergeNoChildTreeItems(treeItems);
        /*while (true){
            if(treeItems.size() == 1){
                if(treeItems.get(0) instanceof GroupSortModel){
                    treeItems = ((GroupSortModel) treeItems.get(0)).getChildren();
                    continue;
                }
            }
            resultTreeItems = treeItems;
            break;

        }*/
        methodGroupTreeDTO.getTreeModel().setTreeItems(treeItems);
        return CommonResult.buildSuccessResult(methodGroupTreeDTO);
    }

    private void mergeNoChildTreeItems(List<TreeSortModel> treeItems) {
        for (TreeSortModel treeItem : treeItems) {
            if (treeItem instanceof GroupSortModel) {
                List<TreeSortModel> children = ((GroupSortModel) treeItem).getChildren();
                if (children.size() == 1) {
                    if (children.get(0) instanceof GroupSortModel) {
                        mergeNoChildTreeItems(children);
                        treeItem.setName(treeItem.getName() + "/" + children.get(0).getName());
                        ((GroupSortModel) treeItem).setChildren(((GroupSortModel) children.get(0)).getChildren());

                    }
                }
            }
        }
    }

    @PostMapping("/queryModels")
    @ApiOperation(value = "分页查询模型列表")
    public CommonResult<Page<QueryModelsResponseDTO>> queryModels(@RequestBody ApiModelPageQuery apiModelPageQuery) {
        log.info("queryModels apiModelPageQuery:{}", JsonUtils.toJSONString(apiModelPageQuery));
        if (apiModelPageQuery.getCurrent() == null || apiModelPageQuery.getCurrent() <= 0) {
            apiModelPageQuery.setCurrent(1L);
        }
        if (apiModelPageQuery.getSize() == null || apiModelPageQuery.getSize() <= 0) {
            apiModelPageQuery.setSize(20L);
        }
        if (apiModelPageQuery.getAppId() == null || apiModelPageQuery.getAppId() <= 0) {
            throw new BizException("应用不能为空");
        }

        Page<QueryModelsResponseDTO> page = apiModelService.queryModels(apiModelPageQuery);
        return CommonResult.buildSuccessResult(page);

    }


    @PostMapping("/parseSql")
    @ApiOperation(value = "解析sql语句")
    public CommonResult<com.jd.workflow.common.ObjectJsonType> parseSql(@RequestBody ParseDTO parseDTO) {
        log.info("parseSql parseDTO:{}", JsonUtils.toJSONString(parseDTO));
        if (StringUtils.isEmpty(parseDTO.getContent())) {
            return CommonResult.buildSuccessResult(null);
        }
        try {
            log.info("parseSql spi.parseContent:{}", parseDTO.getContent());
            com.jd.workflow.common.ObjectJsonType objectJsonType = SPI.of(ModelContentParseSPI.class, spi -> spi.parseContent(parseDTO.getContent()))
                    .filter(spec -> spec.getBizCode().equals("sql"))
                    .reduce(SimpleSPIReducer::first)
                    .call();
            log.info("parseSql spi.parseContent:{}  ,objectJsonType:{}", parseDTO.getContent(), JsonUtils.toJSONString(objectJsonType));
            if (Objects.nonNull(objectJsonType)) {
                return CommonResult.buildSuccessResult(objectJsonType);
            }
        } catch (Exception ex) {
            log.error("ModelContentParseSPI error", ex);
        }
        try {
            ModelContentParseModel modelContentParseModel = new ModelContentParseModel();
            modelContentParseModel.setContent(parseDTO.getContent());
            log.info("parseSql modelContentParseExtAbility.parseContent:{}", parseDTO.getContent());
            com.jd.workflow.common.ObjectJsonType objectJsonType = modelContentParseExtAbility.parseContent(modelContentParseModel);
            log.info("parseSql modelContentParseExtAbility.parseContent:{}  ,objectJsonType:{}", parseDTO.getContent(), JsonUtils.toJSONString(objectJsonType));
            if (Objects.nonNull(objectJsonType)) {
                return CommonResult.buildSuccessResult(objectJsonType);
            }
        } catch (Exception ex) {
            log.error("modelContentParseExtAbility error", ex);
        }
        log.info("parseSql apiModelService.parseSql:{}", parseDTO.getContent());
        ObjectJsonType objectJsonType = apiModelService.parseSql(parseDTO.getContent());
        log.info("parseSql apiModelService.parseSql:{},objectJsonType:{}", parseDTO.getContent(), JsonUtils.toJSONString(objectJsonType.toJson()));
        com.jd.workflow.common.ObjectJsonType objectJsonType1 = convertObjectJsonType(objectJsonType);

        log.info("parseSql apiModelService.parseSql:{},objectJsonType1:{}", parseDTO.getContent(), JsonUtils.toJSONString(objectJsonType1));
        return CommonResult.buildSuccessResult(objectJsonType1);
    }

    private com.jd.workflow.common.ObjectJsonType convertObjectJsonType(ObjectJsonType objectJsonType) {
        com.jd.workflow.common.ObjectJsonType objectJsonType1 = new com.jd.workflow.common.ObjectJsonType();
        objectJsonType1.setType(objectJsonType.getType());
        objectJsonType1.setName(objectJsonType.getName());
        objectJsonType1.setDesc(objectJsonType.getDesc());
        objectJsonType1.setClassName(objectJsonType.getClassName());
        if (CollectionUtils.isNotEmpty(objectJsonType.getChildren())) {
            List<com.jd.workflow.common.JsonType> childList = Lists.newArrayList();
            for (JsonType objectJsonTypeChild : objectJsonType.getChildren()) {
                com.jd.workflow.common.JsonType child = new com.jd.workflow.common.JsonType();
                child.setType(objectJsonTypeChild.getType());
                child.setName(objectJsonTypeChild.getName());
                child.setDesc(objectJsonTypeChild.getDesc());
                child.setClassName(objectJsonTypeChild.getClassName());
                childList.add(child);
            }
            objectJsonType1.setChildren(childList);
        }
        return objectJsonType1;

    }

    @PostMapping("/parseJavaBean")
    @ApiOperation(value = "解析java bean")
    public CommonResult<JsonType> parseJavaBean(@RequestBody ParseDTO parseDTO) {
        ObjectJsonType result = apiModelService.parseJavaBean(parseDTO.getContent());

        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/findRequireModelChildTree")
    @ApiOperation(value = "获取需求下的模型树")
    public CommonResult<MethodGroupTreeDTO> findRequireModelChildTree(@RequestParam(required = true) Long requirementId, @RequestParam(required = false) String modelName) {

        MethodGroupTreeDTO methodGroupTreeDTO = apiModelGroupService.findRequireModelChildTree(requirementId, modelName);

        return CommonResult.buildSuccessResult(methodGroupTreeDTO);
    }

    /**
     * 生成代码
     *
     * @param modelId 模型id
     * @param type    代码类型,目前支持java、ts
     * @return 生成的代码
     */
    @GetMapping("/generateCode")
    public CommonResult<String> generateCode(Long modelId, String type) {

        return CommonResult.buildSuccessResult(apiModelService.generateCode(modelId, type));


    }

    /**
     * 生成代码
     *
     * @param jsonType 模型
     * @param name     模型名称
     * @param type     代码类型,目前支持java、ts
     * @return
     */
    @PostMapping("/generateCodeByType")
    public CommonResult<String> generateCodeByType(@RequestBody JsonType jsonType, String name, String type) {

        return CommonResult.buildSuccessResult(apiModelService.generateCode(jsonType, name, type));


    }
    @GetMapping("/removeDuplicatedModelTree")
    public CommonResult<Boolean> removeDuplicatedModelTree() {

        return CommonResult.buildSuccessResult(apiModelTreeService.removeDuplicated());
    }

    @GetMapping("/removeDuplicatedModel")
    public CommonResult<Boolean> removeDuplicatedModel() {

        return CommonResult.buildSuccessResult(apiModelService.removeDuplicatedModel());
    }

    @PostMapping("/findRequireModelList")
    @ApiOperation(value = "获取需求下的模型列表")
    public CommonResult<Page<QueryModelsResponseDTO>> findRequireModelList(@RequestBody RequireModelPageQuery requireModelPageQuery) {
        log.info("findRequireModelList requireModelPageQuery:{}", JsonUtils.toJSONString(requireModelPageQuery));
        if (Objects.isNull(requireModelPageQuery.getRequirementId())) {
            throw new BizException("需求id不能为空");
        }
        if (requireModelPageQuery.getCurrent() == null || requireModelPageQuery.getCurrent() <= 0) {
            requireModelPageQuery.setCurrent(1L);
        }
        if (requireModelPageQuery.getSize() == null || requireModelPageQuery.getSize() <= 0) {
            requireModelPageQuery.setSize(20L);
        }
        Page<QueryModelsResponseDTO> queryModelsResponseDTOPage = apiModelGroupService.findRequireModelPage(requireModelPageQuery);

        return CommonResult.buildSuccessResult(queryModelsResponseDTOPage);
    }


}

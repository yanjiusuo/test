package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.tag.TagParam;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.TagInfo;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.impl.OpenApiService;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wubaizhao1
 * @date: 2022/5/16 20:47
 */
@Slf4j
@RestController
@RequestMapping("/tagManage")
@UmpMonitor
@Api(value = "标签管理", tags = "标签管理")
public class TagManageController {


    @Resource
    RelationMethodTagService relationMethodTagService;

    @Resource
    TagService tagService;
    @Resource
    RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    IMethodManageService methodManageService;

    @Autowired
    IInterfaceManageService interfaceManageService;

    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    OpenApiService openApiService;

    @RequestMapping(value = "/saveBatchTags", method = RequestMethod.POST)
    @ApiOperation(value = "批量打标")
    public CommonResult<Boolean> saveBatchTags(@RequestBody TagParam param) {

        List<MethodManage> methods = methodManageService.listMethods(new ArrayList<>(param.getMethodIds()));
        Set<Long> interfaceIds = methods.stream().map(item -> item.getInterfaceId()).collect(Collectors.toSet());
        List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(new ArrayList<>(interfaceIds));
        Map<Long, List<InterfaceManage>> interfaceId2Manages = interfaceManages.stream().collect(Collectors.groupingBy(InterfaceManage::getId));
        Set<Long> appIds = interfaceManages.stream().filter(item -> item.getAppId() != null).map(item -> item.getAppId()).collect(Collectors.toSet());

        Map<Long, List<MethodManage>> appId2Methods = new HashMap<>();
        for (MethodManage method : methods) {
            List<InterfaceManage> interfaces = interfaceId2Manages.get(method.getInterfaceId());
            if (interfaces != null) {
                appId2Methods.computeIfAbsent(interfaces.get(0).getAppId(), vs -> {
                    return new ArrayList<>();
                }).add(method);
            }
        }
        for (Map.Entry<Long, List<MethodManage>> entry : appId2Methods.entrySet()) {
            tagService.saveBatchTags(entry.getKey(), param.getTagNames());
            relationMethodTagService.saveBatchTags(entry.getKey(), entry.getValue().stream().map(item -> item.getId()).collect(Collectors.toSet()), param.getTagNames());
        }

        return CommonResult.buildSuccessResult(true);
    }


    @GetMapping("/queryTags")
    @ApiOperation(value = "查询标签列表根据appId")
    public CommonResult<List<Map<String, Object>>> queryTags(@Validated Long appId) {
        log.info("InterfaceManageController queryTags 入参{} ", appId);
        Guard.notNull(appId, "appID不能为空");
        List<Long> appIds = new ArrayList<Long>();
        appIds.add(appId);
        List<Map<String, Object>> counts = getTagMap(appIds);
        return CommonResult.buildSuccessResult(counts);
    }

    @NotNull
    private List<Map<String, Object>> getTagMap(List<Long> appIds) {
        List<TagInfo> tagInfoList = tagService.queryTagNames(appIds);
        Map<String, TagInfo> tagInfoMap = Maps.newHashMap();
        for (TagInfo tagInfo : tagInfoList) {
            if (!tagInfoMap.containsKey(tagInfo.getName())) {
                tagInfoMap.put(tagInfo.getName(), tagInfo);
            }
        }

        List<Map<String, Object>> counts = relationMethodTagService.countMethodNum(appIds);
        for (Map<String, Object> count : counts) {

            if (tagInfoMap.containsKey(count.get("tagName"))) {
                if (tagInfoMap.get(count.get("tagName")).getYn() == 1) {

                    count.put("tagId", tagInfoMap.get(count.get("tagName")).getId());
                    count.put("candelete", !(tagInfoMap.get(count.get("tagName")).getAppId() == 0));
                }
                tagInfoMap.remove(count.get("tagName"));
            }
        }
        counts.removeIf(tag -> !tag.containsKey("tagId"));

        if (CollectionUtils.isNotEmpty(tagInfoMap.keySet())) {
            for (String name : tagInfoMap.keySet()) {
                Map<String, Object> nameAndCount = new HashMap<>();
                nameAndCount.put("tagName", name);
                nameAndCount.put("sum", 0);
                nameAndCount.put("tagId", tagInfoMap.get(name).getId());
                nameAndCount.put("candelete", !(tagInfoMap.get(name).getAppId() == 0));
                counts.add(nameAndCount);
            }
        }
        return counts;
    }

    @GetMapping("/queryTagsByRequirementId")
    @ApiOperation(value = "查询标签列表根据关联id")
    public CommonResult<List<Map<String, Object>>> queryTagsByRequirementId(@Validated Long requirementId) {
        log.info("InterfaceManageController queryTagsByRequirementId 入参{} ", requirementId);
        Guard.notNull(requirementId, "需求id不能为空");
        List<AppInfoDTO> dto = requirementInterfaceGroupService.getRequirementInterfaceId(requirementId);
        if (CollectionUtils.isEmpty(dto)) {
            log.info("findRequireModelChildTree requirementId:{} 查不到appInfo", requirementId);
            return CommonResult.buildSuccessResult(null);
        }
        List<Long> appIds = dto.stream().map(AppInfoDTO::getId).collect(Collectors.toList());
        List<Map<String, Object>> counts = getTagMap(appIds);
        return CommonResult.buildSuccessResult(counts);
    }

    /**
     * 删除标签信息
     *
     * @param id
     * @return
     */
    @GetMapping("/delTags")
    public CommonResult<Boolean> delTags(Long id) {
        return CommonResult.buildSuccessResult(tagService.delTags(id));
    }

    /**
     * 删除标签关系
     *
     * @param id
     * @return
     */
    @GetMapping("/delTagRelation")
    public CommonResult<Boolean> delTagRelation(Long id) {
        return CommonResult.buildSuccessResult(relationMethodTagService.delTagRelation(id));
    }

    @PostMapping("/testOpenAPi")
    @ResponseBody
    public CommonResult<String> testOpenAPi(@RequestBody JSONObject param) {
        return CommonResult.buildSuccessResult(openApiService.testDataExecute(param));
    }



}

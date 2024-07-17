package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.dao.mapper.plusmapper.RelationMethodTagMapper;
import com.jd.workflow.console.entity.RelationMethodTag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RelationMethodTagService extends ServiceImpl<RelationMethodTagMapper, RelationMethodTag> {

    /**
     * 获取标签名称集合 根据方法id
     *
     * @param methodId
     * @return
     */
    public List<String> queryTagNamesByMethodId(Long methodId) {

        LambdaQueryWrapper<RelationMethodTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelationMethodTag::getMethodId, methodId);
        wrapper.eq(RelationMethodTag::getYn, 1);
        List<String> tagNames = this.list(wrapper).stream().map(RelationMethodTag::getTagName).collect(Collectors.toList());
        return tagNames;
    }

    ;

    public Set<String> queryTagNames(Long methodId, Long appId) {

        LambdaQueryWrapper<RelationMethodTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelationMethodTag::getMethodId, methodId);
        wrapper.eq(RelationMethodTag::getAppId, appId);
        wrapper.eq(RelationMethodTag::getYn, 1);
        Set<String> tagNames = this.list(wrapper).stream().map(RelationMethodTag::getTagName).collect(Collectors.toSet());
        return tagNames;
    }

    ;

    public List<Long> queryMethodIdByTagName(Long appId, List<String> tagName) {
        LambdaQueryWrapper<RelationMethodTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(appId != null, RelationMethodTag::getAppId, appId);
        wrapper.in(CollectionUtils.isNotEmpty(tagName), RelationMethodTag::getTagName, tagName);
        wrapper.eq(RelationMethodTag::getYn, 1);
        List<Long> methodIds = this.list(wrapper).stream().map(RelationMethodTag::getMethodId).collect(Collectors.toList());
        return methodIds;
    }

    ;

    public List<Long> queryMethodIdByAppIdsAndTagName(List<Long> appIds, List<String> tagNames) {
        LambdaQueryWrapper<RelationMethodTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RelationMethodTag::getAppId, appIds);
        wrapper.in(CollectionUtils.isNotEmpty(tagNames), RelationMethodTag::getTagName, tagNames);
        wrapper.eq(RelationMethodTag::getYn, 1);
        List<Long> methodIds = this.list(wrapper).stream().map(RelationMethodTag::getMethodId).collect(Collectors.toList());
        return methodIds;
    }

    ;

    /**
     * 获取标签数量
     *
     * @return
     */
    public List<Map<String, Object>> countMethodNum(List<Long> appIds) {
        QueryWrapper<RelationMethodTag> wrapper2 = new QueryWrapper<>();
        wrapper2.in("app_id", appIds);
        wrapper2.select("tag_name as 'tagName',IFNULL(sum(1),0) as 'sum'").groupBy("tag_name");
        return this.listMaps(wrapper2);
    }

    public List<RelationMethodTag> getMethodTags(List<Long> methodIds, Long appId) {
        if (methodIds.isEmpty()) return new ArrayList<>();
        LambdaQueryWrapper<RelationMethodTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RelationMethodTag::getMethodId, methodIds);
        wrapper.eq(RelationMethodTag::getAppId, appId);
        wrapper.eq(RelationMethodTag::getYn, 1);
        return this.list(wrapper);
    }

    /**
     * 批量保存标签信息
     *
     * @param appId
     * @param methodIds
     * @param tags
     * @return
     */
    public Boolean saveBatchTags(Long appId, Set<Long> methodIds, Set<String> tags) {
        List<RelationMethodTag> res = new ArrayList<>();
        List<RelationMethodTag> delete = new ArrayList<>();
        log.info("保存标签关系信息{},{},{}", appId, methodIds, tags);
        for (Long methodId : methodIds) {
            LambdaQueryWrapper<RelationMethodTag> wrapper = new LambdaQueryWrapper<RelationMethodTag>();
            wrapper.eq(RelationMethodTag::getMethodId, methodId);
            wrapper.eq(RelationMethodTag::getAppId, appId);
            wrapper.eq(RelationMethodTag::getYn, 1);
            List<RelationMethodTag> tagInfos = this.list(wrapper);
            Map<String, RelationMethodTag> methodTagMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(tagInfos)) {
                for (RelationMethodTag tagInfo : tagInfos) {
                    methodTagMap.put(tagInfo.getTagName(), tagInfo);
                }
            }

            if (CollectionUtils.isNotEmpty(tags)) {
                for (String tag : tags) {

                    if(methodTagMap.containsKey(tag)) {
                        methodTagMap.remove(tag);
                        continue;
                    }
                    RelationMethodTag relation = new RelationMethodTag();
                    relation.setMethodId(methodId);
                    relation.setAppId(appId);
                    relation.setTagName(tag);
                    relation.setCreator(UpLoginContextHelper.getUserPin());
                    res.add(relation);

                }
            }
            if(CollectionUtils.isNotEmpty(methodTagMap.keySet())){
                for (RelationMethodTag value : methodTagMap.values()) {
                    value.setYn(0);
                    res.add(value);
                }
            }

        }

        return this.saveOrUpdateBatch(res);
    }

    public Boolean delTagRelation(Long id) {
        return this.removeById(id);
    }


    public void saveOrUpdateTag(List<String> tagNames) {
        for (String tagName : tagNames) {

        }
    }


}

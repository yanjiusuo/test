package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.dao.mapper.plusmapper.TagInfoMapper;
import com.jd.workflow.console.entity.TagInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TagService extends ServiceImpl<TagInfoMapper, TagInfo> {


    public List<TagInfo> queryTagNames(List<Long> appIds) {
        LambdaQueryWrapper<TagInfo> infos = new LambdaQueryWrapper<TagInfo>();
        infos.in(CollectionUtils.isNotEmpty(appIds), TagInfo::getAppId, appIds);
        infos.or().eq(TagInfo::getAppId, 0);
//        infos.eq(TagInfo::getYn, 1);
        return this.list(infos);

    }


    public Boolean saveTags(Long appId, String name) {
        TagInfo info = new TagInfo();
        info.setAppId(appId);
        info.setCreator(UpLoginContextHelper.getUserPin());
        info.setYn(1);
        info.setName(name);
        return this.save(info);
    }

    public Boolean saveBatchTags(Long appId, Set<String> names) {
        try {
            LambdaQueryWrapper<TagInfo> wrapper = new LambdaQueryWrapper<TagInfo>();
            wrapper.in(TagInfo::getName, names);
            wrapper.eq(TagInfo::getAppId, appId);
            wrapper.or().eq(TagInfo::getAppId, 0);
            wrapper.eq(TagInfo::getYn, 1);
            List<TagInfo> tagInfos = new ArrayList<>();
            Set<String> nameCopy = new HashSet<>();
            if (CollectionUtils.isNotEmpty(names)) {
                tagInfos = this.list(wrapper);
                nameCopy.addAll(names);
            }
            if (CollectionUtils.isNotEmpty(tagInfos)) {
                List<String> tagNames = tagInfos.stream().map(TagInfo::getName).collect(Collectors.toList());
                nameCopy.removeAll(tagNames);
            }
            List<TagInfo> infos = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(nameCopy)) {
                for (String name : nameCopy) {
                    TagInfo info = new TagInfo();
                    info.setAppId(appId);
                    info.setCreator(UpLoginContextHelper.getUserPin());
                    info.setYn(1);
                    info.setName(name);
                    infos.add(info);
                }
                return this.saveBatch(infos);
            }
        } catch (Exception e) {
            log.error("保存标签信息异常", e);
        }
        return false;
    }

    public Boolean delTags(Long id) {
        TagInfo tagInfo = getById(id);
        if (tagInfo.getAppId() == 0) {
            return false;
        }
        tagInfo.setYn(0);
        return updateById(tagInfo);

    }

}

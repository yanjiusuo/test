package com.jd.workflow.console.service.requirement.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.requirement.RequirementInfoLogMapper;
import com.jd.workflow.console.dto.requirement.InterfaceSpaceLogParam;
import com.jd.workflow.console.dto.requirement.RequirementInfoLogDTO;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.requirement.RequirementInfoLog;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.requirement.RequirementInfoLogService;
import com.jd.workflow.matrix.service.SpaceLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/28
 */
@Slf4j
@Service
public class RequirementInfoLogServiceImpl extends ServiceImpl<RequirementInfoLogMapper, RequirementInfoLog> implements RequirementInfoLogService, SpaceLogService {


    @Autowired
    private RequirementInterfaceGroupService requirementInterfaceGroupService;

    public static final String PATTERN_DEFAULT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DESC_FORMAT = "%s在%s：%s";

    public void createLog(Long requirementId, String erp, Date date, String desc) {

        RequirementInfoLog requirementInfoLog = new RequirementInfoLog();
        requirementInfoLog.setRequirementId(requirementId);
        String time = new SimpleDateFormat(PATTERN_DEFAULT_DATETIME).format(date);
        requirementInfoLog.setDesc(String.format(DESC_FORMAT, erp, time, desc));
        requirementInfoLog.setYn(1);
        requirementInfoLog.setCreated(new Date());
        save(requirementInfoLog);
    }

    @Override
    public Page<RequirementInfoLog> queryLog(InterfaceSpaceLogParam interfaceSpaceLogParam) {

        LambdaQueryWrapper<RequirementInfoLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BaseEntity::getYn, 1);
        lqw.eq(RequirementInfoLog::getRequirementId, interfaceSpaceLogParam.getSpaceId());
        lqw.orderByDesc(BaseEntityNoDelLogic::getCreated);
        Page page = new Page<>(interfaceSpaceLogParam.getCurrent(), interfaceSpaceLogParam.getSize());

        Page<RequirementInfoLog> requirementInfoLogPage = this.page(page, lqw);
        return requirementInfoLogPage;
    }

    /**
     * 添加日志
     *
     * @param interfaceId
     * @param erp
     * @param doSomething
     */
    @Override
    public void createSpaceMethodLog(Long interfaceId, String erp, String doSomething) {
        List<Long> spaceIdList = getSpaceId(interfaceId);
        if (CollectionUtils.isEmpty(spaceIdList)) {
            return;
        }
        for (Long spaceId : spaceIdList) {
            createLog(spaceId, erp, new Date(), doSomething);
        }
    }
    private List<Long> getSpaceId(Long interfaceId) {
        LambdaQueryWrapper<RequirementInterfaceGroup> lqwGroup = new LambdaQueryWrapper<>();
        lqwGroup.eq(RequirementInterfaceGroup::getInterfaceId, interfaceId);
        lqwGroup.select(RequirementInterfaceGroup::getRequirementId);
        List<RequirementInterfaceGroup> requirementInterfaceGroupList = requirementInterfaceGroupService.list(lqwGroup);
        if (CollectionUtils.isNotEmpty(requirementInterfaceGroupList)) {
            return requirementInterfaceGroupList.stream().map(RequirementInterfaceGroup::getRequirementId).collect(Collectors.toList());
        }
        return null;

    }
}

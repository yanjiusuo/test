package com.jd.workflow.console.service.listener.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/4
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.service.requirement.RequirementInfoLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenyufeng18
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/4
 * @date 2023-09-04 15:07
 */
@Slf4j
@Service
public class SpaceMethodLogChangeListener implements InterfaceChangeListener {

    @Autowired
    private RequirementInfoLogService requirementInfoLogService;


    @Autowired
    private RequirementInterfaceGroupService requirementInterfaceGroupService;

    @Autowired
    private MethodManageServiceImpl methodManageService;



    @Override
    public void onMethodAfterUpdate(InterfaceManage interfaceManage, List<MethodManage> afters,List<MethodManage> before) {
        for (MethodManage after : afters) {
            log.info("SpaceMethodLogChangeListener onMethodAfterUpdate:{}", after.getId());
            List<Long> spaceIdList = getSpaceId(interfaceManage.getId());
            if (CollectionUtils.isEmpty(spaceIdList)) {
                return;
            }
            for (Long spaceId : spaceIdList) {
                requirementInfoLogService.createLog(spaceId, UserSessionLocal.getUser().getUserId(), new Date(), "修改了接口：" + after.getName() + ",id:" + after.getId());
            }
        }

    }

    @Override
    public  void onMethodRemove(InterfaceManage interfaceManage, List<MethodManage> methods) {
        for (MethodManage method : methods) {
            log.info("SpaceMethodLogChangeListener onMethodRemove:{}", method.getId());
            List<Long> spaceIdList = getSpaceId(interfaceManage.getId());
            if (CollectionUtils.isEmpty(spaceIdList)) {
                log.info("SpaceMethodLogChangeListener onMethodRemove {} is empty", method.getId());
                return;
            }
            for (Long spaceId : spaceIdList) {
                requirementInfoLogService.createLog(spaceId, UserSessionLocal.getUser().getUserId(), new Date(), "删除了接口：" + method.getName() + ",id:" + method.getId());
            }
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

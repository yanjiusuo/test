package com.jd.workflow.console.service.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/31
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.RequirementAppModelSnapshotMapper;
import com.jd.workflow.console.entity.requirement.RequirementAppModelSnapshot;
import com.jd.workflow.console.service.requirement.RequirementAppModelSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/31 
 */
@Slf4j
@Service
public class RequirementAppModelSnapshotServiceImpl extends ServiceImpl<RequirementAppModelSnapshotMapper,RequirementAppModelSnapshot> implements RequirementAppModelSnapshotService {
}

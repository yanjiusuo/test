package com.jd.workflow.console.service.usecase.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.InterfaceManageMapper;
import com.jd.workflow.console.dao.mapper.group.RequirementInterfaceGroupMapper;
import com.jd.workflow.console.dao.mapper.usecase.CaseSetMapper;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.dto.usecase.CaseInterfaceManageDTO;
import com.jd.workflow.console.dto.usecase.CaseParamBuilderDTO;
import com.jd.workflow.console.dto.usecase.CaseRequirementInterfaceGroupDTO;
import com.jd.workflow.console.dto.usecase.CaseSetDTO;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.param.ParamBuilder;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.entity.usecase.CaseSet;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.param.IParamBuilderService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.console.service.usecase.CaseSetExeLogManager;
import com.jd.workflow.console.service.usecase.CaseSetManager;
import com.jd.workflow.console.service.usecase.CaseSetService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 用例集表 服务实现类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Service
public class CaseSetManagerImpl extends ServiceImpl<CaseSetMapper, CaseSet> implements CaseSetManager {


    @Autowired
    private CaseSetExeLogManager caseSetExeLogManager;

    @Transactional
    @Override
    public Boolean delById(Long Id) {
        CaseSet caseSet = new CaseSet();
        caseSet.setId(Id);
        caseSet.setYn(DataYnEnum.INVALID.getCode());
        updateById(caseSet);
        caseSetExeLogManager.delById(Id);
        return true;
    }
}

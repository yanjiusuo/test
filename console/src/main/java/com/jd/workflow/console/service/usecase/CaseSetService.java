package com.jd.workflow.console.service.usecase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jd.workflow.console.dto.usecase.CaseInterfaceManageDTO;
import com.jd.workflow.console.dto.usecase.CaseSetDTO;
import com.jd.workflow.console.dto.usecase.RequiremenUnderInterfacesDTO;
import com.jd.workflow.console.dto.usecase.TreeItem;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.entity.usecase.CaseSet;

import java.util.List;

/**
 * @description: 用例集表 服务类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetService extends IService<CaseSet> {

    IPage<CaseSetDTO> pageList(Long current, Long pageSize, String name, Long requirementId);

    RequiremenUnderInterfacesDTO getRequiremenUnderInterfaces(Long requirementId, Long appId);

    boolean checkAuth(Long requirementId, String userId);

    /**
     * 保存用例集
     *
     * @param caseSet
     */
    void add(CaseSet caseSet);

    /**
     * 通过id删除用例集相关数据
     *
     * @param id
     * @return
     */
    Boolean delById(Long id);

    /**
     * 通过id 获取详情
     *
     * @param id
     * @return
     */
    CaseSetDTO detailById(Long id);

    /**
     * 通过ID获取用例集
     *
     * @param id
     * @return
     */
    CaseSet obtainById(Long id);
}

package com.jd.workflow.console.service.usecase;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.usecase.CaseInterfaceManageDTO;
import com.jd.workflow.console.dto.usecase.CaseSetDTO;
import com.jd.workflow.console.entity.usecase.CaseSet;

import java.util.List;

/**
 * @description:
 * 用例集表 服务类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetManager extends IService<CaseSet> {
    /**
     * 通过id删除用例集相关数据
     * @param Id
     * @return
     */
    Boolean delById(Long Id);

}

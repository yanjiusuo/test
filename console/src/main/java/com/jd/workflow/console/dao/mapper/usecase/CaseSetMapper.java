package com.jd.workflow.console.dao.mapper.usecase;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.dto.usecase.CaseSetDTO;
import com.jd.workflow.console.entity.usecase.CaseSet;
import org.apache.ibatis.annotations.Param;

/**
 * @description: 用例集表 Mapper 接口
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetMapper extends BaseMapper<CaseSet> {

    Page<CaseSetDTO> pageList(Page page, @Param("name") String name, @Param("requirementId") Long requirementId);
}
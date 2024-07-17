package com.jd.workflow.console.dao.mapper;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/18
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.dto.ApiModelCountDto;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/18 
 */
public interface ApiModelMapper extends BaseMapper<ApiModel> {
    public List<ApiModelCountDto> queryDuplicatedModels();
}

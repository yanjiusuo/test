package com.jd.workflow.console.service.model;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.ApiModelDelta;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20 
 */
public interface IApiModelDeltaService extends IService<ApiModelDelta> {
    public ApiModelDelta getByModelId(Long apiModelId);
    public List<ApiModelDelta> getByModelIds(List<Long> apiModelId);
}

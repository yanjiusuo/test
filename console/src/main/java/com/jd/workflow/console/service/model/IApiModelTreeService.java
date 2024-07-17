package com.jd.workflow.console.service.model;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */

import com.baomidou.mybatisplus.extension.service.IService;

import com.jd.workflow.console.entity.model.ApiModelTree;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20 
 */
public interface IApiModelTreeService extends IService<ApiModelTree> {
    public ApiModelTree getTreeByAppId(Long appId);
    public boolean removeDuplicated();
}

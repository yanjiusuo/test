package com.jd.workflow.console.dto;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

import java.util.List;

@Data
public class InterfacePageQuery extends PageParam {
    String tenantId;
    /**
     * 负责人code
     */
    String adminCode;
    /**
     * 应用id
     */
    Long appId;
    /**
     * 类型过滤条件，以,分割
     */
    String type;
    /**
     * 接口名称
     */
    String name;
    /**
     * 编排类型 0-默认 1-单节点 2-多节点
     * @date: 2022/6/1 16:03
     * @author wubaizhao1
     */
    Integer nodeType;
    /**
     * 接口分级
     */
    private Integer level;
    /**
     * 是否自动上报
     */
    private Integer autoReport;

    /**
     * 查询人erp
     */
    String erp;


    private List<Long> ids;
}

package com.jd.workflow.console.dto.manage;

import com.jd.workflow.console.entity.MethodManage;
import lombok.Data;

@Data
public class MethodSearchResult extends MethodManage {
    /**
     * 所属接口编码
     */
    String interfaceCode;
    /**
     * 所属接口名称
     */
    String interfaceName;
    /**
     * 应用编码
     */
    String appCode;
    /**
     * 应用名称
     */
    String appName;
    /**
     * 部门名称
     */
    String deptName;
    /**
     * 管理员编码
     */
    String adminCode;
    /**
     * 管理员名称
     */
    String adminName;
}

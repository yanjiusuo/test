package com.jd.workflow.console.dto.usecase;

import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CaseInterfaceManageDTO implements Serializable {


    /** 主键 */
    private Long id;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     * {@link InterfaceTypeEnum}
     */
    private Integer type;

    /** 接口名称 */
    private String name;

    /** 接口描述 */
    private String desc;

    /** 地址 */
    private String path;

    /** 服务编码 */
    private String serviceCode;

    /** 所属部门名称 */
    private String deptName;

    /** 前端使用 */
    private String key;

    private List<CaseRequirementInterfaceGroupDTO> children;
}

package com.jd.workflow.console.dto.client;

import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

import java.util.List;

/***
 * 对外输出接口dto
 */
@Data
public class InterfaceOutDto extends BaseEntity {
    /**
     * 接口id
     */
    Long id;
    String serviceCode;
    /**
     * 接口名称
     */
    String name;
    /**
     * 接口成员
     */
    String members;
    /**
     * 接口描述
     */
    String desc;
    /**
     * 是否有效
     */
    Integer yn;
    /**
     * 接口环境
     */
    private List<EnvModel> envList;
}

package com.jd.workflow.console.dto.manage;

import com.jd.workflow.console.dto.group.GroupResolveDto;
import lombok.Data;

import java.util.List;

@Data
public class InterfaceOrMethodSearchDto extends GroupResolveDto {
    /**
     * 接口类型：{@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    Integer interfaceType;
    /**
     * 接口状态：{@link com.jd.workflow.console.base.enums.InterfaceTypeStatus}
     */
    Integer status;
    /**
     * 搜素条件
     */
    String search;
    /**
     * 当前页
     */
    Long current;
    /**
     * 每页大小
     */
    Long size;
}

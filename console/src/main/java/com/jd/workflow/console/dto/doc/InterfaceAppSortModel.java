package com.jd.workflow.console.dto.doc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/4 17:41
 * @Description: 接口排序
 */
@Data
public class InterfaceAppSortModel extends GroupSortModel {
    /**
     * 子节点
     */
    private List<TreeSortModel> children = new ArrayList<>();


    public String getType() {
        return TYPE_INTERFACE;
    }

    String appName;

    String appCode;
    String modifier;

    String userName;

    Long interfaceId;

}

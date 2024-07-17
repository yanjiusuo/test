package com.jd.workflow.console.dto.doc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/4 17:41
 * @Description: 接口排序
 */
@Data
public class InterfaceSortModel extends GroupSortModel {
    private String appName;
    private int valid = 1;
    /**
     * 子节点
     */


    public String getType() {
        return TYPE_INTERFACE;
    }

}

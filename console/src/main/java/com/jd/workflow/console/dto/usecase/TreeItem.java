package com.jd.workflow.console.dto.usecase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/23
 */
@Data
@NoArgsConstructor
public class TreeItem {
    /**
     * title 名
     */
    private String title;
    /**
     * 唯一id
     */
    private String key;

    /**
     * 数据类型 1-接口 2-方法 3-用例
     */
    private Integer dataType ;

    /**
     * 子对象
     */
    private List<TreeItem> children = new ArrayList<>();

    public TreeItem(String title, String key, Integer dataType) {
        this.title = title;
        this.key = key;
        this.dataType = dataType;
    }
}

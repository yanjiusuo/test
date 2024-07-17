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
public class InterfaceCountModel extends InterfaceSortModel {
    /**
     * 接口数量
     */
    int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

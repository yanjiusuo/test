package com.jd.workflow.console.dto.doc;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/4 17:41
 * @Description: 接口排序
 */
@Data
public class InterfaceTopCountModel extends InterfaceCountModel {
    /**
     * 是否置顶
     */
    boolean topped;


}

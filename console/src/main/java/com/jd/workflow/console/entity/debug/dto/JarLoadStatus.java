package com.jd.workflow.console.entity.debug.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum JarLoadStatus {
    UNLOAD(0,"未加载"),
    LOADING(1,"加载中"),
    LOADED(2,"加载成功"),
    LOAD_FAIL(3,"加载失败")

    ;
    /**
     * @date: 2022/5/12 18:23
     * @author wubaizhao1 扩展点
     */
    @Getter
    @Setter
    private Integer code;

    /**
     * 描述
     * @date: 2022/5/12 18:25
     * @author wubaizhao1
     */
    @Getter
    @Setter
    private String desc;

}

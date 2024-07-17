package com.jd.workflow.console.base.enums;

import lombok.Getter;

/**
 * 项目名称：allen-review
 * 类 名 称：ParseType
 * 类 描 述：
 * 创建时间：2022-05-24 17:11
 * 创 建 人：wangxiaofei8 
 */
@Getter 
public enum ParseType {

    /**
     * request的param中解析
     */
    PARAM(0),
    /**
     * request的body中解析
     */
    BODY(1);

    private final int type;

    ParseType(int type) {
        this.type = type;
    }
}

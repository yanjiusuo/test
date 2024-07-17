package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目名称：parent
 * 类 名 称：LockTypeEnum
 * 类 描 述：TODO
 * 创建时间：2022-12-16 15:30
 * 创 建 人：wangxiaofei8
 */
@AllArgsConstructor
public enum LockTypeEnum {



    CLEAN_DEBUG_LOG(1,"清除流程编排debug日志"),

    SYNC_JAPI_DATA(2,"同步japi数据"),
    SYNC_JDOS_MEMBERS(3,"同步jdos成员"),
    SYNC_INTERFACE_PROP(4,"每天更新接口中字段属性缓存"),
    UPDATE_RANK(5,"更新rank信息"),
    UPDATE_ES_INDEX(6,"更新es索引"),
    CODE_ACTIVITY_STATISTIC(7,"更新统计活动"),
    PROMOTION(8,"更新推荐积分");

    /**
     *
     */
    @Getter
    @Setter
    private Integer code;
    /**
     *
     */
    @Getter
    @Setter
    private String description;
}

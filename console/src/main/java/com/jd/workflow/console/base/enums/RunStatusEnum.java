package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2024-05-22 14:54
 * @author yanzengan
 */
@AllArgsConstructor
public enum RunStatusEnum {
    /**
     *
     */
    INIT(0, "初始化"),

    /**
     *
     */
    WAIT(1, "待执行"),

    /**
     *
     */
    RUNNING(2, "执行中"),

    /**
     *
     */
    SUCCESS(3, "执行成功"),

    /**
     *
     */
    FAIL(4, "执行失败");

    /**
     *
     */
    @Getter
    @Setter
    Integer type;

    /**
     *
     */
    @Getter
    @Setter
    String desc;

    /**
     * 获取状态描述
     * @param type
     * @return
     */
    public static String getDescByType(Integer type) {
        String desc = "";
        if (type == null) {
            return desc;
        }
        for (RunStatusEnum value : RunStatusEnum.values()) {
            if (value.getType().equals(type)) {
                desc = value.getDesc();
                break;
            }
        }
        return desc;
    }
}

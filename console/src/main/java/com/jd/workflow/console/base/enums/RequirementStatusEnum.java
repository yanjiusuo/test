package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2024-01-16 10:58
 * @author yanzengan
 */
@AllArgsConstructor
public enum RequirementStatusEnum {

    /**
     *
     */
    DOING(1, "处理中"),
    FINISH(2, "已完成"),
    NOT_ENABLED(3, "未启用"),
    ;


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

    /**
     *
     * @param code
     * @return
     */
    public static String getStatusDesc(Integer code) {
        String name = "";
        if (DOING.getCode().equals(code)) {
            name = DOING.getDescription();
        } else if (FINISH.getCode().equals(code)) {
            name = FINISH.getDescription();
        } else if (NOT_ENABLED.getCode().equals(code)) {
            name = NOT_ENABLED.getDescription();
        }
        return name;
    }
}

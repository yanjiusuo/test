package com.jd.workflow.console.base.enums;

/**
 * xbp状态枚举
 * @author yangyanping
 * @date 2020-04-26
 */
public enum XbpStatusEnum {
    REJECT(-1, "已驳回", "已被驳回"),
    PROCESSING(0, "进行中", "正在进行中"),
    FINISHED(1, "已完结", "已完结"),
    WITHDRAW(2, "已撤销", "已撤销"),
    ;

    /**
     * 唯一code值
     */
    private final Integer code;

    /**
     * 名字描述
     */
    private final String name;

    /**
     * 描述
     */
    private final String desc;

    XbpStatusEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static XbpStatusEnum getEnumByCode(Integer code) {
        if (code == null || code.intValue() < 1) {
            return null;
        }

        for (XbpStatusEnum item : XbpStatusEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }

        return null;
    }
}

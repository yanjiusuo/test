package com.jd.workflow.console.entity.usecase.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @description: CaseSetExeLog 状态枚举
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
@Getter
public enum CaseSetExeLogStatusEnum {

    WAITING_EXE(1, "用例待执行"),
    RUNNING(2, "用例待执行中"),
    COVERAGE_EXE(3, "覆盖率计算中"),
    SUCCESS(4, "成功"),
    FAIL(5, "失败");

    /**
     * 唯一值
     */
    private Integer code;

    /**
     * 描述
     */
    private String desc;

    CaseSetExeLogStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过code获取枚举值
     *
     * @param code
     * @return
     */
    public static CaseSetExeLogStatusEnum of(Integer code) {
        return Arrays.stream(CaseSetExeLogStatusEnum.values())
                .filter(item -> Objects.equals(item.getCode(), code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 校验code是否在枚举内
     *
     * @param code
     * @return
     */
    public static boolean check(Integer code) {
        return Objects.nonNull(of(code));
    }


}

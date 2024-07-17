package com.jd.workflow.console.entity.logic;

import java.util.Arrays;
import java.util.Objects;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/17
 */
public enum BizLogicTypeEnum  {
    /**
     * 文档类型ID
     */
    Doc_TYPE(1),
    /**
     * 调用示例说明
     */
    ExplanationText(2),
    /**
     * 业务逻辑
     */
    BizLogicText(3)
    ;

    private Integer value;


    /**
     * 通过code获取枚举值
     * @param code
     * @return
     */
    public static BizLogicTypeEnum of(Integer code) {
        return Arrays.stream(BizLogicTypeEnum.values())
                .filter(item -> Objects.equals(item.getValue(), code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 校验code是否在枚举内
     * @param code
     * @return
     */
    public static boolean check(int code) {
        return Objects.nonNull(of(code));
    }

    private BizLogicTypeEnum(int type){
        this.value = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
package com.jd.workflow.console.dto.test;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum FlowNodeTypeCodeEnum {
    //public static final int source = 1;
    DEMAND_REVIEW("demand_review", "需求评审", 1),
    CODING_DESIGN("coding_design", "开发设计", 1),
    INTERFACE_DEFINITION("interface_definition", "接口定义", 1),
    FRONT_DEVELOPMENT("front_development", "PC端开发", 1),
    BACKEND_DEVELOPMENT("backend_development", "后端开发", 1),
    CONFIRM("confirm", "确认节点", 1),
    TEST_CASE_DESIGN("test_case_design", "测试用例设计", 1),
    FUNCTIONAL_TESTING("functional_testing", "功能测试", 1),
    FUNCTIONAL_ACCEPTANCE("functional_acceptance", "功能验收", 1),
    FRONT_BACKEND_DEBUGGING("front_backend_debugging","前后端联调",1),

   
    ;

    /**
     * 唯一值
     */
    private String code;

    /**
     * 描述
     */
    private String desc;

    /**
     * 来源 1-  2-paas2.0
     */
    private int source;



    FlowNodeTypeCodeEnum(String code,String desc,Integer source ) {

        this.code = code;
        this.desc = desc;
        this.source = source;
    }


    /**
     * 通过code获取枚举值
     *
     * @param code
     * @return
     */
    public static FlowNodeTypeCodeEnum of(String code) {
        return Arrays.stream(FlowNodeTypeCodeEnum.values())
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
    public static boolean check(String code) {
        return Objects.nonNull(of(code));
    }
}

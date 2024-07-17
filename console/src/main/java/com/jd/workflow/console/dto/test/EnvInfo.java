package com.jd.workflow.console.dto.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum EnvInfo {
    TEST(1,"测试站"),
    ZH(2,"中国站");

    private Integer code;



    private String desc;


}

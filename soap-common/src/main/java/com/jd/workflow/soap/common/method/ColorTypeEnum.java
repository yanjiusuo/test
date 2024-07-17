package com.jd.workflow.soap.common.method;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public enum ColorTypeEnum {


    requestHeader(1, "requestHeader"),

    requestParam(2, "requestParam"),

    responseHeader(3, "responseHeader");



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

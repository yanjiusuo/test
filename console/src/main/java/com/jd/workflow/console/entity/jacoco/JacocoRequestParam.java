package com.jd.workflow.console.entity.jacoco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JacocoRequestParam {
    /**
     * 方法名
     */
    private String methodName;
    /**
     * IP
     */
    private String ip;
    /**
     * 数组类型，为jsf接口的入参
     */
    private List<Object> args = new ArrayList<>();
}

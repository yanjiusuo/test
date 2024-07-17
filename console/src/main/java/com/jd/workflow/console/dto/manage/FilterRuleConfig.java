package com.jd.workflow.console.dto.manage;

import lombok.Data;

import java.util.List;

@Data
public class FilterRuleConfig {
    /**
     * @hidden
     */
    String appCode;
    /**
     * @hidden
     */
    String key;
    /**
     *  @hidden
     */
    Boolean enabled;
    /**
     * 域名列表，以,分割
     */
    String domains;
    /**
     * http错误状态码
     */
    List<Integer> httpStatusCodes;
    /**
     * 错误断言表达式
     */
    List<FilterRuleConfigExpr>  errorExprs;
    @Data
    public static class FilterRuleConfigExpr{
        /**
         * 属性名
         */
        String name;
        /**
         * 比较符号: >、<、>=、<=、!=、=、in
         */
        String op;
        /**
         * 值
         */
        String value;

  
    }
}

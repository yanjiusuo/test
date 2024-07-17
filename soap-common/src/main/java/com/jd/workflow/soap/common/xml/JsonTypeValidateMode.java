package com.jd.workflow.soap.common.xml;

/**
 * 校验模式:
 * ONLY_TYPE:只校验类型，参数录入的时候校验
 * TYPE_AND_VALUE: 校验类型以及值，并会校验值的有效性，表达式是否正确
 */
public enum JsonTypeValidateMode {
    NONE,ONLY_TYPE,TYPE_AND_VALUE;
}

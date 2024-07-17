package com.jd.workflow.console.dto.manage;

import lombok.Data;

import java.util.List;

/**
 * 示例值
 */
@Data
public class JsfOrHttpDemo {
    /**
     * 入参： 针对http接口格式为：{headers:{},params:{},body:{}}
     *      jsf接口格式为数组
     */
    String input;
    /**
     * 出参:http隔开格式为：{headers:{},body:{}}
     *  针对jsf接口为接口的出参
     */
    String output;

    String httpMethod;
    /***
     * 当前选中的环境id
     */
    Long envConfigItemId;
    List<EnvConfigItemDto> envConfigItems;
}

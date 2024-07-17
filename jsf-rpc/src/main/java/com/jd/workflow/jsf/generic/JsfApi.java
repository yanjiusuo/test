package com.jd.workflow.jsf.generic;

import lombok.Data;

/**
 * 项目名称：parent
 * 类 名 称：JsfApi
 * 类 描 述：接口参数定义
 * 创建时间：2022-06-28 09:56
 * 创 建 人：wangxiaofei8
 */
@Data
public class JsfApi {

    private String jsfInterface;

    private String protocol;

    private String alias;

    private String serialization;

    private Integer timeout;

    private String index;

    private Boolean generic;

    private String configParams;

    private Boolean updateInstance;

    public String getConsumerName(){
        return jsfInterface+"#"+alias;
    }

}

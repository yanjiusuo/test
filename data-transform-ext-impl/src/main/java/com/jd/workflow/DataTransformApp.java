package com.jd.workflow;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */

import com.jd.matrix.sdk.annotation.App;
import com.jd.matrix.sdk.base.BaseApp;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23 
 */
@App(code = DataTransformApp.CODE,
        name = "中国-藏经阁-JAPI",
        priority = 2000,
        version = "1.0.0",
        parserClass = DataTransformAppParser.class)
public class DataTransformApp  extends BaseApp {

    public static final String CODE = "cn_retail_japi";
}

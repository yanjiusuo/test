package com.jd.workflow.console.base.annotation;
import com.jd.workflow.console.base.enums.AuthorizationKeyTypeEnum;
import com.jd.workflow.console.base.enums.ParseType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目名称：allen-review
 * 类 名 称：Authorization
 * 类 描 述：
 * 创建时间：2022-05-24 17:11
 * 创 建 人：wangxiaofei8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Authorization {
    /**
     * 解析key
     * @return
     */
    String key() default "";

    /**
     * 鉴权key的类型
     * @return
     */
    AuthorizationKeyTypeEnum keyType() default AuthorizationKeyTypeEnum.INTERFACE;
    /**
     * 默认 request的参数
     * @return
     */
    ParseType parseType() default ParseType.PARAM;
}

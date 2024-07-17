package com.jd.businessworks.annotation;


import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JtfServiceID {

    public String value();

    //是否限制领域服务走编排
    public String[] assignMajor() default {};
}

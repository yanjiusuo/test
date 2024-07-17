package com.jd.businessworks.annotation;


import java.lang.annotation.*;


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JtfBusinessID {

    public String field() default "";

}

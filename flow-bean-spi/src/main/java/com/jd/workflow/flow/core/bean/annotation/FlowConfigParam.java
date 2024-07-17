package com.jd.workflow.flow.core.bean.annotation;

import com.jd.workflow.flow.core.bean.IValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface FlowConfigParam {

    boolean required() default false;

    String label() default "";

    String desc() default "";

    String[] enumsValue() default "";

    Class<? extends IValidator>[] validator() default {};
}

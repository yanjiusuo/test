package com.jd.workflow.soap.common.xml.schema;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class Constraint {
    /**
     * 最小值
     */
    Number min;
    /**
     * 最大值
     */
    Number max;

    String pattern;
    /*
    枚举值
     */
    List<String> enumValue;

    public Constraint clone(){
        Constraint newConstraint = new Constraint();
        BeanUtils.copyProperties(this,newConstraint);
        return newConstraint;
    }
}

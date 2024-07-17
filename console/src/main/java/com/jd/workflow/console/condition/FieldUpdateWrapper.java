package com.jd.workflow.console.condition;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jd.workflow.soap.common.util.StringHelper;


public class FieldUpdateWrapper<T> extends UpdateWrapper<T> {
    @Override
    protected String columnToString(String column) {
        return StringHelper.camelCaseToUnderscore(column,true);
    }
    @Override
    public UpdateWrapper<T> set(boolean condition, String column, Object val, String mapping) {
        column = StringHelper.camelCaseToUnderscore(column,true);
        return super.set(condition,column,val,mapping);
    }
}

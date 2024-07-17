package com.jd.workflow.console.condition;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.jd.workflow.soap.common.util.StringHelper;


/**
 * 将驼峰字段转换为_分割的
 * @param <T>
 */
public class FieldQueryWrapper<T> extends QueryWrapper<T> {

    @Override
    protected String columnToString(String column) {
        return StringHelper.camelCaseToUnderscore(column,true);
    }
    @Override
    protected QueryWrapper<T> instance() {
        FieldQueryWrapper queryWrapper = new FieldQueryWrapper<>();
        super.setEntity(super.getEntity());
        super.setEntityClass(super.getEntityClass());
        queryWrapper.paramNameSeq = this.paramNameSeq;
        queryWrapper.paramNameValuePairs = this.paramNameValuePairs;
        queryWrapper.expression = new MergeSegments();
        queryWrapper.paramAlias = this.paramAlias;
        queryWrapper.lastSql = SharedString.emptyString();
        queryWrapper.sqlComment = SharedString.emptyString();
        queryWrapper.sqlFirst = SharedString.emptyString();
        return queryWrapper;
    }

}

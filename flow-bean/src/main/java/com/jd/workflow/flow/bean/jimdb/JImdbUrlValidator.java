package com.jd.workflow.flow.bean.jimdb;

import com.jd.workflow.flow.core.bean.IValidator;
import org.apache.commons.lang.StringUtils;

public class JImdbUrlValidator implements IValidator<String> {
    @Override
    public String[] validate(String value) {
        if(StringUtils.isEmpty(value)){
            return new String[]{"jimdb url不可为空"};
        }

        return new String[0];
    }
}

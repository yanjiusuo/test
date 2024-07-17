package com.jd.workflow.flow.bean.ducc;

import com.jd.workflow.flow.core.bean.IValidator;

import java.net.MalformedURLException;
import java.net.URL;

public class DuccUrlValidator implements IValidator<String> {
    @Override
    public String[] validate(String value) {
        if(!value.startsWith("ucc://")){
            return new String[]{"无效的ducc链接:"+value};
        }

        return new String[0];
    }
}

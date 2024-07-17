package com.jd.workflow.console.dto.importer;

import lombok.Data;

import java.util.List;

@Data
public class JddjResult<T> {
    String code;
    String msg;
   T result;

    public boolean isSuccess(){
        return "0".equals(code);
    }
}

package com.jd.workflow.flow.core.exception;

public class TypeCastException extends StepParseException{
    static String MSG = "step.err_cast_json_step";
    public TypeCastException() {
        super(MSG);
    }
    public TypeCastException(Throwable throwable) {
        super(MSG,throwable);
        desc(throwable.getMessage());
    }

    public TypeCastException desc(String desc){
        param("desc",desc);
        return this;
    }
}

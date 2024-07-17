package com.jd.workflow.flow.core.exception;

public class StepScriptParseException extends StepParseException{
    static String MSG = "expr.err_parse_expr";
    /**
     * 哪个环节
     */
    String stage;
    /**
     * 代码行号
     */
    int line;
    /**
     * 描述
     */
    String desc;
    public StepScriptParseException() {
        super(MSG);
    }
    public StepScriptParseException(Throwable throwable) {
        super(MSG,throwable);
    }


    public String getStage() {
        return (String) params.get("stage");
    }

    public void setStage(String stage) {
        param("stage",stage);
    }

    public int getLine() {
          return (int) params.get("line");
    }

    public void setLine(int line) {
       param("line",line);
    }

    public String getDesc() {
        return (String) params.get("desc");
    }

    public void setDesc(String desc) {
        param("desc",desc);
    }
}

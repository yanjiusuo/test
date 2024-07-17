package com.jd.workflow.flow.core.exception;

import com.jd.workflow.soap.common.exception.StdException;
import org.apache.camel.Exchange;

/**
 * 脚本执行异常，需要知道是哪一行，出问题了
 */
public class StepScriptEvalException extends StepExecException {
    static String MSG = "expr.err_eval_script";
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
    public StepScriptEvalException(String stepId) {
        super(stepId, MSG);
    }
    public StepScriptEvalException(String stepId,Throwable e) {
        super(stepId, MSG,e);
        if(e != null){
            setDesc(e.getMessage());
        }
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

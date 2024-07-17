package com.jd.workflow.flow.core.retry;

import com.jd.workflow.soap.common.lang.Variant;
import lombok.Data;

@Data
public class ExecContext {
    /**
     * 失败次数
     */
    int retryCount;
    /**
     * 重试开始时间
     */
    long retryStartTime;
    /**
     * 最后一次的执行异常
     */
    Throwable getLastThrowable;

    public int getInvokedTime(){
        long time = System.currentTimeMillis() - retryStartTime;
        return Variant.valueOf(time).toInt();
    }
}

package com.jd.workflow.console.dto.test;

import lombok.Data;

@Data
public class SmokeExecuteResult {
    /**
     * 是否执行了:true执行了，false未执行
     */
    boolean executed;
    /**
     * 总数
     */
    Integer totalCount;
    /**
     * 成功的数量
     */
    Integer succeedCount;
    /**
     * 失败的数量
     */
    Integer failedCount;
    /**
     * 执行中的数量，大于0表示有正在执行
     */
    Integer executingCount;
}

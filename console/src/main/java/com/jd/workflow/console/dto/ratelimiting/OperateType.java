package com.jd.workflow.console.dto.ratelimiting;

public enum OperateType {
    /**
     * 操作类型
     * 1-创建规则
     * 2-修改规则
     * 3-删除规则
     * 4-上线
     * 5-下线
     */
    CREATE(1, "创建规则"),
    UPDATE(2, "修改规则"),
    DELETE(3, "删除"),
    ONLINE(4, "下线"),
    OFFLINE(5, "上线"),
    ;

    public Integer type;
    public String desc;


    OperateType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}

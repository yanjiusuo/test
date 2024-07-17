package com.jd.workflow.console.entity.manage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

@Data
public class HistorySearchKeyword extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 搜索关键字
     */
    String search;

    String operator;
}

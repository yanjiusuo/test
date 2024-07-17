package com.jd.workflow.console.entity.manage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

@Data
public class RankScore extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer type;// 类型，用来区分是接口分组还是接口：1-接口分组 2-接口
    private Integer rank;// 等级，从1到100级别，区间（i,i+1] 区间内的数据都会落到这个级别里面
    private int count;// 位于该等级的接口分组或者接口数量
}

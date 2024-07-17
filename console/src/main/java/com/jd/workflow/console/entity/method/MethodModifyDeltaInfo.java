package com.jd.workflow.console.entity.method;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

@Data
public class MethodModifyDeltaInfo extends BaseEntityNoDelLogic {
    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关联方法id
     */
    Long methodId;
    /**
     * 接口id,方便过滤
     */
    Long interfaceId;
    /**
     * 用户手动修改的内容
     */
    String deltaContent;
    /**
     * 接口名
     */
    String name;
    /**
     * 接口编码
     */
    String methodCode;
}

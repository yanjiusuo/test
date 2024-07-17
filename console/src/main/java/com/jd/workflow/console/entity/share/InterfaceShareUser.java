package com.jd.workflow.console.entity.share;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/31 14:40
 * @Description: 接口分享组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "interface_share_user", autoResultMap = true)
public class InterfaceShareUser extends BaseEntity implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 分享分组id
     */
    private Long groupId;
    /**
     * 被分享人code
     */
    @TableField("`shared_user_code`")
    private String sharedUserCode;
}

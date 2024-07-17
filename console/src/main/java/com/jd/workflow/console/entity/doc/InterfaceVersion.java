package com.jd.workflow.console.entity.doc;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.MethodSnapshot;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TableName(value = "interface_version",autoResultMap = true)
@Data
public class InterfaceVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    Long interfaceId;
    @TableField("`version`")
    String version;
    @TableField("`desc`")
    String desc;
    @TableField(typeHandler= JacksonTypeHandler.class)
    MethodSnapshot methodSnapshot;

    @TableField(typeHandler= JacksonTypeHandler.class)
    private MethodGroupTreeModel groupTreeSnapshot;

    /**
     * 是否终版 1是 0 否 默认0
     */
    @TableField("final_version")
    private Integer finalVersion;


}

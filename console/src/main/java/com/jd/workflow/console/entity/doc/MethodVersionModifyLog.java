package com.jd.workflow.console.entity.doc;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.doc.MethodContentSnapshot;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

@TableName(autoResultMap = true)
@Data
public class MethodVersionModifyLog extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    Long interfaceId;
    Long methodId;
    String version;
    @TableField(typeHandler= JacksonTypeHandler.class)
    MethodContentSnapshot methodContentSnapshot;
}

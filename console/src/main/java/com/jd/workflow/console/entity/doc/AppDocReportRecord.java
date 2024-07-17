package com.jd.workflow.console.entity.doc;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.doc.ReportDocInfo;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@TableName(value = "app_doc_report_record",autoResultMap = true)
public class AppDocReportRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(typeHandler= JacksonTypeHandler.class)
    Object content;
    /**
     * 是否成功：1-成功 0-不成功
     */
    int success;
    String errorInfo;
    Date reportTime;
    String ip;
    String version;
    String appCode;
    String httpAppCode;
    String digest;
    @TableField(typeHandler = JacksonTypeHandler.class)
    ReportDocInfo reportDocInfo;

    private String codeRepository;
    private String branch;
    private String channel;
}

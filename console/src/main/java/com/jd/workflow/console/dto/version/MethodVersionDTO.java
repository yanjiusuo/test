package com.jd.workflow.console.dto.version;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.jd.workflow.console.dto.doc.MethodContentSnapshot;
import lombok.Data;

import java.util.Date;

/**
 * 方法版本基本信息
 */
@Data
public class MethodVersionDTO {
    private String version;
    private String versionDesc;
    private String desc;
    private String content;
    private Object contentObject;
    private String httpMethod;
    private String inputExample;
    private String outputExample;
    private String modifier;
    private Date modified;

}

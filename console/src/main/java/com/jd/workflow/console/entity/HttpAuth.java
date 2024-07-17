package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.InterfaceDocConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 鉴权标签管理
 * </p>
 *
 * @author wangwenguang
 * @since 2022-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "http_auth",autoResultMap = true)
public class HttpAuth extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 站点
     */
    @TableField("`site`")
    private String site;

    /**
     * 应用编码
     */
    @TableField("`app_code`")
    private String appCode;


    /**
     * 应用名称
     */
    @TableField("`app_name`")
    private String appName;

    /**
     * 鉴权标识
     */
    @TableField("`auth_code`")
    private String authCode;

    /**
     * 成员
     */
    @TableField(exist = false)
    private String members;

}

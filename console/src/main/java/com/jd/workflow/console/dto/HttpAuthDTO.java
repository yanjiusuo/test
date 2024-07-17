package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 鉴权标签管理
 * </p>
 *
 * @author wangwenguang
 * @since 2022-05-11
 */
@Data
public class HttpAuthDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 站点
     */
    private String site;

    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 鉴权标识
     */
    private String authCode;


    /**
     * 创建者
     */
    private String creator;

    /**
     * 修改者
     */
    private String modifier;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    private Integer yn;

    /**
     * 负责人名称
     */
    private String ownerName;

    /**
     * 负责人erp
     */
    private String ownerErp;

    /**
     * 成员
     */
    private String members;

    /**
     * 接口分享时，文件夹类型 1：方法  2：方法分组  3：应用或接口文件夹类型
     */
    private int type;
}

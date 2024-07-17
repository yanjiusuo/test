package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.dto.doc.TreeSortModel;
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
public class HttpAuthDetailDTO implements Serializable {
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
     * 应用名称
     */
    private String appName;

    /**
     * 鉴权标识
     */
    private String authCode;

    /**
     * 老的鉴权标识
     */
    private String oldAuthCode;

    /**
     * 方法ID
     */
    private Long methodId;

    /**
     * 方法编码
     */
    private String methodCode;
    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 方法路径
     */
    private String path;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 接口编码
     */
    private String interfaceCode;
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 拼接name
     */
    private String name;


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
     * 接口分享时，文件夹类型 1：方法  2：方法分组
     */
    private int type;

    /**
     * 接口类型  1-http、3-jsf
     */
    private int interfaceType;

    private List<HttpAuthDetailDTO> methodList;

    private List<TreeSortModel> children;

}

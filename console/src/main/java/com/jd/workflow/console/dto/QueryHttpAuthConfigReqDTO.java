package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 类 名 称：QueryHttpAuthConfigReqDTO
 * 类 描 述：查询鉴权标识
 * @date 2023-01-06 11:30
 * @author wangwenguang
 */
@Data
public class QueryHttpAuthConfigReqDTO {

    /**
     * id
     */
    private Long id;


    /**
     * 站点
     */
    private String site;


    /**s
     * 应用信息
     */
    private String appInfo;


    /**
     * 应用code
     */
    private String appCode;

    /**s
     * 应用名称
     */
    private String appName;

    /**
     * 是否一键降级，1为生效，0为不生效
     */
    private String valid;

    /**
     * 是否强制鉴权，1为强制，0为不强制
     */
    private String forceValid;

    /**
     * 用户查询
     */
    private String pin;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     *当前页数 从1开始
     */
    private Long currentPage = 1L;

    /**
     * 每页条数 最多100条
     */
    private Integer pageSize = 10;

    /**
     * 当前页号
     */
    private Long offset;

    public Long getOffset() {
        return (currentPage-1) * pageSize;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

}

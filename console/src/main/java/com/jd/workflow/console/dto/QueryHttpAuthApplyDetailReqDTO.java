package com.jd.workflow.console.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

/**
 * 类 名 称：QueryHttpAuthDetailReqDTO
 * 类 描 述：查询鉴权标识
 * @date 2023-01-06 11:30
 * @author wangwenguang
 */
@Data
public class QueryHttpAuthApplyDetailReqDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 站点
     */
    private String site;


    /**
     * 应用信息（应用编码+应用名称）
     */
    private String appInfo;

    /**
     * 调用应用信息（应用编码+应用名称）
     */
    private String callAppInfo;

    /**
     * 接口信息（接口编码+接口名称）
     */
    private String interfaceInfo;

    /**
     * 方法信息（方法编码+方法名称）
     */
    private String methodInfo;

    /**
     * 应用提供方用户查询
     */
    private String userInfo;

    /**
     * 应用调用方用户查询
     */
    private String callUserInfo;

    /**
     * 鉴权信息
     */
    private String authInfo;

    /**
     * 应用code
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 调用应用信息
     */
    private String callAppCode;

    /**
     * 调用方应用名称
     */
    private String callAppName;

    /**
     * 鉴权标识
     */
    private String authCode;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 方法ID
     */
    private Long methodId;

    /**
     * 方法ID
     */
    private String methodName;

    /**
     * 方法code
     */
    private String methodCode;

    /**
     * 方法路径
     */
    private String path;

    /**
     * 申请单ID
     */
    private String ticketId;

    /**
     * 申请人
     */
    private String creator;

    /**
     * token有值
     */
    private Boolean hasToken;

    /**
     * 应用提供者用户查询
     */
    private String pin;

    /**
     * 应用提供者用户查询
     */
    private String callPin;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     *当前页数 从1开始
     */
    private Long current = 1L;

    /**
     * 每页条数 最多100条
     */
    private Integer pageSize = 10;

    /**
     * 当前页号
     */
    private Long offset = 0L;

    /**
     * 来源，1：应用调用方； 2：应用提供方
     */
    private Integer source;

    /**
     * 申请明细列表
     */
    private List<HttpAuthApplyDetailDTO> applyDetailList;

    /**
     *  默认null 查询鉴权接口； 1:查询有权限的接口
     */
    private  Integer type;

}

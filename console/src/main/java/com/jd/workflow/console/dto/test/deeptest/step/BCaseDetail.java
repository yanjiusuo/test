package com.jd.workflow.console.dto.test.deeptest.step;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26 
 */
@Data
public class BCaseDetail {
    private Long id;

    private Long detailId;

    private List<String> selectedItems;
    /**
     * 对比组header信息
     */
    private String bHeaderParam;
    /**
     * 对比组接口名
     */
    private String bInterfaceName;
    /**
     * 对比组方法名
     */
    private String bMethodName;
    /**
     * 对比组别名
     */
    private String bAlias;
    /**
     * 对比组ip端口
     */
    private String bIpPort;
    /**
     * 对比组token
     */
    private String bToken;
    /**
     * 对比组接口请求类型(HTTP)
     */
    private String bRequestType;
    /**
     * 对比组入参
     */
    private String bInputParam;
    /**
     * 对比组入参类型(JSF)
     */
    private String bInputParamType;
    /**
     * 调用类型（JSF）
     */
    private Long bCallType;
    /**
     * 入参类型，多个参数用逗号分割
     */
    private String inputParamType;
    /**
     * 类构造函数参数类型，多个参数用逗号分割
     */
    private String initType;
    /**
     * 类构造函数参数，多个参数用逗号分割
     */
    private String initParam;
    /**
     * 匹配类型： 0 键值匹配 1 整段匹配
     */
    private Integer bMatchType;
}

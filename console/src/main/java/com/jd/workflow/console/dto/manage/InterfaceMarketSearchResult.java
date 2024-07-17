package com.jd.workflow.console.dto.manage;

import lombok.Data;

import java.util.List;

/**
 * 接口市场搜索结果
 */
@Data
public class InterfaceMarketSearchResult {
    /**
     * 类型：{@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    private Integer type;
    /**
     * 数量
     */
    private long count;
    List<MethodResult> data;
    @Data
    public static class MethodResult{
        /**
         * 方法名称
         */
        private String methodName;
        /**
         * 方法编码
         */
        private String methodCode;
        /**
         * 接口分组id
         */
        private Long interfaceId;
        /**
         * 接口名称
         */
        private String interfaceName;
        /**
         * 接口编码
         */
        private String interfaceServiceCode;
        /**
         * 方法id
         */
        private Long methodId;
        /**
         * 接口路径
         */
        private String httpPath;
        /**
         * 应用id
         */
        private Long appId;

        private String docInfo;
        /**
         * 应用名称
         */
        private String appName;
        /**
         * 应用编码
         */
        private String appCode;
        /**
         * 用户erp
         */
        private String userCode;
        /**
         * 用户名称
         */
        private String userName;
        /**
         * 部门
         */
        private String dept;
        /**
         * 参数描述
         */
        private String paramDesc;
        /**
         * 接口分组分数
         */
        Double score;

        Boolean hasLicense;
        /**
         * 云文档标签
         */
        private String cloudFileTags;
    }

}

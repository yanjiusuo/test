package com.jd.workflow.console.dto.manage;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

import java.util.List;

@Data
public class ErrorLogFilterParam extends PageParam {
    /**
     * 状态：0-未处理 1-已处理  空的话查询全部
     */
   private Integer status;
    /**
     * 过滤域名，多个以，分割
     */
   private String domains;
    /**
     * 应用id
     */
   private Long appId;
    /**
     * 接口路径
     */
   private String path;
}

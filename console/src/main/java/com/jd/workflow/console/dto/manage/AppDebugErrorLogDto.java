package com.jd.workflow.console.dto.manage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.doc.AppDebugLogContent;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

@Data

public class AppDebugErrorLogDto extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * @hidden
     */
    String appCode;
    Long appId;
    String path;
    /**
     * url前缀
     */
    String urlPrefix;
    /**
     * 域名
     */
    String domain;
    /**
     * ip地址
     */
    String ip;

    /**
     * 上报人
     */
    String reporter;
    /**
     * 解决人
     */
    String op;
    /**
     * 响应体
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    AppDebugLogContent content;
    /**
     * 状态：0-未解决 1-已解决
     */
    Integer status;


    public void init(){
        if(content != null){
            method = content.getMethod();
            url = content.getUrl();
            reason = content.getReason();
        }
    }

    /**
     * http method
     */
    String method;
    /**
     * 全路径
     */
    String url;
    /**
     * 原因
     */
    String reason;
}

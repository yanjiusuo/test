package com.jd.workflow.console.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.service.doc.SwaggerParserService;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.Swagger;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "api_model", autoResultMap = true)
public class ApiModel extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    String name; // 类名
    Long appId;
    @TableField("`desc`")
    String desc;
    /**
     * 签名：只有签名不一致才更新
     */
    String digest;
    Integer autoReport;
    @TableField(typeHandler = JacksonTypeHandler.class)
    JsonType content; // 自动上报的内容
    @TableField(typeHandler = JacksonTypeHandler.class)
    List<String> refNames;// 引用的模型列表，包含递归引用的

    private String packagePath;



    public void setRefNames(List<String> refNames) {
        List<String> list = refNames;
        String strs = JsonUtils.toJSONString(list);
        int actualLength = SwaggerParserService.getActualLength(strs);
        while (actualLength >= 65535){
            list = list.subList(0,list.size()/2);
            strs = JsonUtils.toJSONString(list);
            actualLength = SwaggerParserService.getActualLength(strs);
        }
        this.refNames = list;
    }
}

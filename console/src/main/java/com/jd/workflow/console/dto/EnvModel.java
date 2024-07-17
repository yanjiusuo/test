package com.jd.workflow.console.dto;

import com.jd.workflow.console.base.enums.EnvTypeEnum;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 对应env变量的一个对象
 * {@link com.jd.workflow.console.entity.InterfaceManage.env}
 * @date: 2022/5/30 14:44
 * @author wubaizhao1
 */
@Data
@Builder
@AllArgsConstructor
public class EnvModel {
    public EnvModel(){}
    public EnvModel(String envName,String url,EnvTypeEnum type){
        this.envName = envName;
        this.url = new ArrayList<>();
        this.url.add(url);
        this.type = type;
    }
    /**
     * 环境名称
     */
    String envName;
    /**
     * 基础url列表
     * @date: 2022/5/30 14:51
     * @author wubaizhao1
     */
    List<String> url;
    /**
     * 环境的类型
     */
    EnvTypeEnum type;

    /**
     *
     */
    String hostIp;

    List<SimpleJsonType> headers;
}

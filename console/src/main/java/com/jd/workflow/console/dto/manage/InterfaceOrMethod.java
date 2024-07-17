package com.jd.workflow.console.dto.manage;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dto.doc.InterfaceDocConfig;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 接口或者方法数据
 */
@Data
public class InterfaceOrMethod extends BaseEntity {
    Long id;
    /**
     * 接口状态 {@link com.jd.workflow.console.base.enums.InterfaceTypeStatus}
     */
    Integer status;
    /**
     * 接口分组id
     */
    Long interfaceId;
    /**
     * 应用id
     */
    Long appId;

    Integer type;

    /**
     * http方法
     */
    String httpMethod;
    /**
     * 英文名
     */
    String serviceCode;

    /**
     * 接口路径
     */
    String path;
    /**
     * 接口名称
     */
    String name;
    String groupName;
    /**
     * 配置信息,如：{
     *      "serviceType": 2,"beanName": "demoService"
     *         }
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map config;
    /**
     * 负责人code
     */
    private String userCode;

    /**
     * 负责人名称
     */
    private String userName;

    String groupId;

    String artifactId;

    String version;
    String desc;

    String docInfo;

    InterfaceDocConfig docConfig;
    String key;

    Set<String> tags;
    public String initKey(){
        if(StringUtils.isNotBlank(httpMethod)){ //是http方法
            key = TreeSortModel.getKey(TreeSortModel.TYPE_METHOD,interfaceId,type,id);
        }else{
            key = TreeSortModel.getKey(TreeSortModel.TYPE_INTERFACE,interfaceId,type,id);
        }

        return key;
    }
}

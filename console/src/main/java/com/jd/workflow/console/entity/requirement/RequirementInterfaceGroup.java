package com.jd.workflow.console.entity.requirement;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import com.jd.workflow.console.entity.ITreeEntitySupport;
import lombok.Data;

/**
 * 需求关联接口分组
 */
@Data
@TableName(value = "requirement_interface_group",autoResultMap = true)
public class RequirementInterfaceGroup extends BaseEntityNoDelLogic implements ITreeEntitySupport {
    // 从j-api同步的版本号加一个japi_前缀，手动维护的版本号不加前缀，这样若发现本地维护了版本号，则不再从j-api同步
    public static final String J_API_GROUP_VERION_PREFIX = "japi_";
    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关联的需求id
     */
    private Long requirementId;
    /**
     * 关联的接口id
     */
    private Long interfaceId;
    /**
     * 接口类型 {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    private int interfaceType;
    /**
     * 分组配置树
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    MethodGroupTreeModel sortGroupTree;
    /**
     * 分组树版本
     */
    private String groupLastVersion;

}

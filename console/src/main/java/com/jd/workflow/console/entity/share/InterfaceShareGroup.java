package com.jd.workflow.console.entity.share;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.InterfaceShareTreeModel;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/31 14:40
 * @Description: 接口分享组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "interface_share_group", autoResultMap = true)
public class InterfaceShareGroup extends BaseEntity implements Serializable {

    /**
     * 分享分组id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分享分组名称
     */
    @TableField("share_group_name")
    private String shareGroupName;

    /**
     * 是否跨应用0：不跨应用 1：跨应用
     */
    @TableField("across_app")
    private int acrossApp;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private InterfaceShareTreeModel sortInterfaceShareTree;

    @TableField("`last_version`")
    private String lastVersion;
    @TableField(exist = false)
    private Boolean isCreator = false;
    public void init(){
        UserInfoInSession user = UserSessionLocal.getUser();
        if(user != null && user.getUserId().equals(getCreator())){
            isCreator = true;
        }else{
            isCreator = false;
        }
    }
}

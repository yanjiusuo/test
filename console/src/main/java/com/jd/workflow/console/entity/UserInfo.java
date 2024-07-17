package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.IOException;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.jtfm.configcenter.ducc.utils.JsonUtil;
import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("user_info")
@JsonSerialize(using = UserInfo.UserSerializer.class)
public class UserInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户部门
     */
    @TableField("dept")
    private String dept;

    /**
     * 登录类型：0-erp 1-pin 2-手机号 3-健康体系
     * link{@com.jd.workflow.console.base.enums.LoginTypeEnum}
     */
    @TableField("login_type")
    private Integer loginType;

    /**
     * 用户编码（英文）
     */
    @TableField("user_code")
    private String userCode;

    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;

    private String password;
    /**
     * 用户头像
     */
    @TableField(exist = false)
    private String headImg;
    /**
     * 上次访问时间
     */
    private Date lastAccessTime;
    /**
     * 职位
     */
    private String positionName;

    public Map<String,Object> toMap(){
        PropertyUtilsBean bean = new PropertyUtilsBean();

        Map result = BeanTool.getProps(this, "id","dept","loginType","userCode","userName","creator","modifier","created","modified","headImg");
        result.remove("password");
        return result;
    }
    public static class UserSerializer extends JsonSerializer<UserInfo> {

        @Override
        public void serialize(UserInfo value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(value.toMap());

        }
    }
}

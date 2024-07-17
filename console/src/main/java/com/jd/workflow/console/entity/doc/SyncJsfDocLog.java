package com.jd.workflow.console.entity.doc;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/7
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sync_jsf_doc_log")
public class SyncJsfDocLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口全名
     */
    @TableField("interface_name")
    private String interfaceName;


    /**
     * 应用名称
     */
    private String appCode;


    /**
     * 状态 0-执行中 1-成功 2-失败 3-非java
     */
    @TableField("status")
    private Integer status;

    /**
     * 1-5 原子执行错误具体信息
     * 1、依赖包找不到 2、pom找不到 3、编译版本不匹配 4、文件不存在 5 无权限应用  9 未知异常
     */
    private Integer subStatus;

    /**
     * 备注
     */
    @TableField("remart")
    private String remart;


    /**
     * 上报类型 0= webhook/test 1= 部署记录上报 2 手动上报
     */
    private Integer type;




    /**
     * 流水id
     */
    private String  flowId;

    /**
     * 记录流水类型 // 0 脚本触发 1-流水线 2-原子执行 3 maven插件上报接口
     */
    private Integer flowType;

    /**
     * 结果地址
     */
    private String resultUrl;

    /**
     * 流水线地址
     */
    private String buildUrl;

    /**
     * codePath
     */
    private String codePath;


    /**
     * 部门信息
     */
    private String dept;


    private Integer jsfNum;

    private Integer httpNum;



    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    @TableField("yn")
    private Integer yn;

    /**
     * 创建时间
     */
    @TableField("created")
    private LocalDateTime created;

    /**
     * 修改时间
     */
    @TableField("modified")
    private LocalDateTime modified;

    @TableField(exist = false)
    private static final List<Integer> lastStatus = Arrays.asList(1, 2, 3);

    /**
     * 判断是否为终止状态
     * @return
     */
    public Boolean isLastStatus(){
        return lastStatus.contains(status);
    }

}

package com.jd.workflow.console.dto.test.deeptest;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * 用力挤
 */
@Data
public class CaseGroupCreateDto extends BaseTestEntityInfo{
    /**
     * 目录id
     */
    private Long id;

    /**
     * 目录类型：1-模块、2-用例集、3-单接口目录
     */
    private Integer type;

    /**
     * 目录名称
     */
    private String name;

    /**
     * 目录备注
     */
    private String note;

    /**
     * 父级目录id，根目录-1
     */
    private Long parentId = -1L;

    /**
     * 当前目录层级
     */
    private Integer level = 1;

    /**
     * 当前目录层级全路径：/1/2/3
     */
    private String path = "";

    /**
     * 当前层级目录展示顺序
     */
    @TableField(value = "`order`")
    private Integer order = 0;

    /**
     * 所属目录id，无所属null
     */
    private Long belongId;

    /**
     * 资源来源：default、cjg
     */
    private String source = "default";




    /**
     * 状态0正常1删除
     */
    private Integer status = 0;

    /**
     * 分享状态0模块成员可见1所有人可见
     */
    private Integer share = 0;

}

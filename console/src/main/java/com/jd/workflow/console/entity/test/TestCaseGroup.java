package com.jd.workflow.console.entity.test;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/18
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/18 
 */
@Data
@TableName(value = "test_case_group", autoResultMap = true)
public class TestCaseGroup extends BaseEntityNoDelLogic {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 环境
     */
    private String env;

    /**
     *  根节点id
     */
    private Long moduleId;

    /**
     * 父节点id
     */
    private Long parentId;
    /**
     * 类型：1-应用，2-分组，3-接口,0-模块
     */
    private Integer relatedType;
    /**
     * 关联的:应用id||分组id||接口id
     */
    private Long relatedId;

    /**
     * 关联deeptest用例组id
     */
    private Long relatedTestCaseGroupId;

    /**
     * 关联deeptest模块id
     */
    private Long relatedTestModuleId;

}

package com.jd.workflow.console.entity.test;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

/**
 * 关联deeptest表工程信息
 */
@Data
public class TestProjectInfo extends BaseEntityNoDelLogic {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关联工程id
     */
    String relatedProjectId;
    /**
     * 关联工程编码
     */
    String relatedProjectCode;
    /**
     * 关联工程名称
     */
    String relatedProjectName;
    /**
     * 站点信息 test、zh
     */
    String env;
    /**
     * 关联的deeptest模块id
     */
    private Long relatedTestModuleId;

}

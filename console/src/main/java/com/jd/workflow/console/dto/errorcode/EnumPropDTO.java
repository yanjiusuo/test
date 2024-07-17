package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */
@Data
public class EnumPropDTO {
    /**
     * 主键
     */
    private Long id;


    /**
     * 枚举表主键
     */
    private Long enumId;

    /**
     * 编码
     */
    private String propCode;

    /**
     * 中文名
     */
    private String propName;
    /**
     * 描述
     */
    private String propDesc;

    /**
     * 解决方案描述
     */
    private String propSolution;

    /**
     * 应用id，删除时必传
     */
    private Long appId;


}

package com.jd.workflow.console.dto.requirement;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @author yza
 * @description
 * @date 2024/5/21
 */
@Data
public class ParamBuilderRecordParam extends PageParam {

    /**
     * 用例id
     */
    private String paramBuilderId;

    /**
     * 1.个人 2.全部
     */
    private Integer type;
}

package com.jd.workflow.console.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum RequirementTypeEnum {

        FLOW(1,"工作流"),
        JAPI(2,"迁移");
        /**
         * @date: 2022/5/12 18:23
         * @author wubaizhao1
         */
        @Getter
        @Setter
        private Integer code;

        /**
         * 描述
         * @date: 2022/5/12 18:25
         * @author wubaizhao1
         */
        @Getter
        @Setter
        private String desc;
}

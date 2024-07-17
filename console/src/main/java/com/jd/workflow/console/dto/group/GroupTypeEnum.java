package com.jd.workflow.console.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum GroupTypeEnum {

        APP(1,"应用"),
        PRD(2,"需求"),
        STEP(3,"工作流步骤");
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

package com.jd.workflow.console.dto.doc;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ListModifyLogDto extends PageParam {
    /**
     * 接口id
     */
    @NotNull(message = "接口id不可为空")
    Long interfaceId;
    @NotNull(message = "方法id不可为空")
    Long methodId;
}

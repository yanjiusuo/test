package com.jd.workflow.console.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页参数
 * @description:
 * @date: 2022/5/11 18:54
 * @author wubaizhao1
 */
@Data
@NoArgsConstructor
public class PageParam {
    /**
     * 当前页号
     */
    @ApiModelProperty("当前页号")
    private Long current=1L;
    /**
     * 页大小 最大500条
     */
    @ApiModelProperty("页大小 最大100条")
    private Long size=10L;

    private Long total=0L;
}

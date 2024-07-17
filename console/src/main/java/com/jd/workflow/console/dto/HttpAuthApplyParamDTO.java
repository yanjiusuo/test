package com.jd.workflow.console.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 鉴权标签管理
 * </p>
 *
 * @author wangwenguang
 * @since 2022-05-11
 */
@Data
public class HttpAuthApplyParamDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 申请明细
     */
    private List<HttpAuthApplyDTO> authApplyList;

    /**
     * 申请原因
     */
    private String applyDesc;
}

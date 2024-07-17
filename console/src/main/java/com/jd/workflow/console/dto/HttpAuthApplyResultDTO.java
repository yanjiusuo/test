package com.jd.workflow.console.dto;

import lombok.Data;

import java.io.Serializable;
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
public class HttpAuthApplyResultDTO  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导入失败的
     */
    List<HttpAuthApplyDTO> applyFailList;

    /**
     * 导入成功的
     */
    List<HttpAuthApplyDTO> applySuccessList;

    /**
     * 导入失败的
     */
    List<HttpAuthApplyDetailDTO> applyDetailFailList;

    /**
     * 导入成功的
     */
    List<HttpAuthApplyDetailDTO> applyDetailSuccessList;

}

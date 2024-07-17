package com.jd.workflow.console.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * cjg应用导入结果
 * @author xiaobei
 * @date 2023-02-26 16:59
 */
@Getter
@Setter
@ToString
public class AppImportResultDTO implements Serializable {

    private static final long serialVersionUID = 6571382870743403755L;

    /**
     * 导入成功的所有应用信息
     */
    private List<AppImportDTO> successList;

    /**
     * 导入失败的所有应用信息
     */
    private List<AppImportDTO> failList;

}

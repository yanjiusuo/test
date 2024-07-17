package com.jd.workflow.console.service;

import com.jd.workflow.console.dto.AppImportDTO;
import com.jd.workflow.console.dto.AppImportResultDTO;

import java.util.List;

/**
 * 导入藏经阁应用
 * @author xiaobei
 * @date 2023-02-26 16:53
 */
public interface AppImportService {

    /**
     * 批量导入cjg应用
     * @param authApplyList
     * @return
     */
    AppImportResultDTO batchImportCjgApp(List<AppImportDTO> authApplyList, String currentUser);
}

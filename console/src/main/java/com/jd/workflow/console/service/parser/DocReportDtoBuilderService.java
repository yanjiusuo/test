package com.jd.workflow.console.service.parser;

import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.model.sync.BuildReportContext;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
public interface DocReportDtoBuilderService<T> {
    T build(T targetObj, BuildReportContext context);
}

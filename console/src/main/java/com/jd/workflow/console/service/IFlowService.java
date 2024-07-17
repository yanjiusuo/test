package com.jd.workflow.console.service;

import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.FlowImportDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.metadata.impl.WebServiceStorageMetadata;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.soap.common.lang.Guard;

import java.util.List;
import java.util.Map;

public interface IFlowService {
    /**
     * 合并webservice定义
     */
    public void mergeFlowDefinition(WorkflowDefinition definition);

    public List<WebServiceStorageMetadata> loadWebserviceStorage(List<Long> ids);

    public WorkflowDefinition loadDef(Long id);

    /**
     *
     * @param flowImportDTO
     *
     * @return
     */
    Long saveFlow(FlowImportDTO flowImportDTO) ;

    WorkflowDefinition parseAndInitFlow(String methodId, Map<String,Object> def);
    /**
     *
     * @param
     * @return
     */
    WorkflowDefinition loadFlowDef(Long methodId);

    /**
     *
     * @return
     */
    Object invokeFlow();
}

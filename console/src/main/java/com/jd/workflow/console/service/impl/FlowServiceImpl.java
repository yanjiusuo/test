package com.jd.workflow.console.service.impl;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.FlowImportDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamQuoteReqDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamQuoteResultDTO;
import com.jd.workflow.console.entity.FlowParamQuote;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.service.IFlowParamQuoteService;
import com.jd.workflow.console.service.IFlowService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.flow.bean.BeanStepMetadata;
import com.jd.workflow.flow.core.definition.BeanStepDefinition;
import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.definition.WorkflowParam;
import com.jd.workflow.flow.core.metadata.impl.HttpStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.WebServiceStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.WebServiceStorageMetadata;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.flow.parser.context.IFlowResolver;
import com.jd.workflow.flow.parser.context.impl.DefaultFlowParserContext;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
public class FlowServiceImpl implements IFlowService {
    /**
     *
     */
    @Resource
    IMethodManageService methodManageService;
    @Resource
    IInterfaceManageService interfaceManageService;
    @Autowired
    UserPrivilegeHelper privilegeHelper;
    @Autowired
    IFlowParamQuoteService flowParamQuoteService;

    @Override
    public void mergeFlowDefinition(WorkflowDefinition definition) {
        List<StepDefinition> tasks = definition.getTasks();
        List<WebServiceStepMetadata> webserviceTasks = new ArrayList<>();
        for (StepDefinition task : tasks) {
            if (task instanceof BeanStepDefinition
                    &&
                    (task.getMetadata() instanceof WebServiceStorageMetadata)
            ) {
                webserviceTasks.add((WebServiceStepMetadata) task.getMetadata());
            }
        }
        List<Long> ids = new ArrayList<>();
        for (WebServiceStepMetadata webserviceTask : webserviceTasks) {
            ids.add(webserviceTask.getEntityId());
        }
        List<WebServiceStorageMetadata> metadatas = loadWebserviceStorage(ids);
        for (int i = 0; i < webserviceTasks.size(); i++) {
            WebServiceStepMetadata webServiceStepMetadata = webserviceTasks.get(i);
            webServiceStepMetadata.mergeFrom(metadatas.get(i));
        }
    }

    @Override
    public List<WebServiceStorageMetadata> loadWebserviceStorage(List<Long> ids) {
        return Collections.emptyList();
    }

    @Override
    public WorkflowDefinition loadDef(Long id) {
        return loadFlowDef(id);
    }

    private void checkAuth(MethodManage manage) {
        Long interfaceId = manage.getInterfaceId();
        Boolean success = privilegeHelper.hasInterfaceRole(interfaceId, UserSessionLocal.getUser().getUserId());
        if (!success) {
            throw new BizException("没有操作权限");
        }
    }


    @Override
    public Long saveFlow(FlowImportDTO dto) {
        Guard.notEmpty(dto.getId(), "方法id不能为空", ServiceErrorEnum.INVALID_PARAMETER.getCode());
        Guard.notEmpty(dto.getDefinition(), "编排内容不能为空", ServiceErrorEnum.INVALID_PARAMETER.getCode());


        WorkflowDefinition definition = parseAndInitFlow(dto.getId() + "", dto.getDefinition());

        MethodManage manageDto = methodManageService.getById(dto.getId());

        Guard.notEmpty(manageDto, "无效的方法id", ServiceErrorEnum.INVALID_PARAMETER.getCode());
        checkAuth(manageDto);
        manageDto.setId(dto.getId());
        manageDto.setContent(JsonUtils.toJSONString(definition));
        methodManageService.updateById(manageDto);

        return dto.getId();
    }

    private void initHasBean(WorkflowDefinition workflowDefinition) {
        initHasBean(workflowDefinition, workflowDefinition.getTasks());
    }

    private void initHasBean(WorkflowDefinition workflowDefinition, List<StepDefinition> list) {
        for (StepDefinition definition : list) {
            if (definition.getMetadata() instanceof BeanStepMetadata) {
                BeanStepMetadata metadata = (BeanStepMetadata) definition.getMetadata();
                if (metadata.getServiceType() != null) {
                    workflowDefinition.setHasJavaBean(true);
                }
            }
            if (definition.getChildren() == null) return;
            for (List<StepDefinition> children : definition.getChildren().values()) {
                initHasBean(workflowDefinition, children);
            }
        }


    }

    private void initParam(String methodId, WorkflowDefinition definition) {
        MethodManage methodManage = methodManageService.getById(methodId);

        QueryParamQuoteReqDTO dto = new QueryParamQuoteReqDTO();
        dto.setInterfaceId(methodManage.getInterfaceId());
        QueryParamQuoteResultDTO result = flowParamQuoteService.queryQuoteParam(dto);
        for (FlowParamQuote flowParamQuote : result.getList()) {
            WorkflowParam workflowParam = new WorkflowParam();
            workflowParam.setName(flowParamQuote.getName());
            workflowParam.setValue(flowParamQuote.getValue());
            workflowParam.setEntityId(flowParamQuote.getId());
            definition.getParams().add(workflowParam);
        }
    }

    @Override
    public WorkflowDefinition parseAndInitFlow(String methodId, Map<String, Object> def) {
        if (def == null) {
            String content = loadFlowContent(Long.valueOf(methodId));
            def = JsonUtils.parse(content, Map.class);
        }
        DefaultFlowParserContext parserContext = new DefaultFlowParserContext();
        parserContext.setValidate(true);
        parserContext.pushFlowId(methodId);
        parserContext.setFlowResolver(new IFlowResolver() {
            @Override
            public WorkflowDefinition resolveSubflow(String entityId, SubflowStepMetadata metadata) {
                Long id = Long.valueOf(entityId);
                return loadDef(id);
            }
        });
        //Map<String,Object> def = JsonUtils.parse(defStr, Map.class);
        WorkflowDefinition definition = WorkflowParser.parse(def, parserContext);
        initHasBean(definition);
        initParam(methodId, definition);
        parserContext.removeFlowId(methodId);

        return definition;
    }

    public String loadFlowContent(Long id) {

        Guard.notEmpty(id, "方法id不能为空", ServiceErrorEnum.INVALID_PARAMETER.getCode());

        MethodManage manage = methodManageService.getById(id);
        Guard.notEmpty(manage, "无效的方法", ServiceErrorEnum.INVALID_PARAMETER.getCode());
        InterfaceManage interfaceManage = interfaceManageService.getById(manage.getInterfaceId());
        boolean isPublic = interfaceManage.getIsPublic() != null && interfaceManage.getIsPublic().equals(1);
        if (!isPublic) {
            checkAuth(manage);
        }

        if (StringUtils.isBlank(manage.getContent())) {
            return null;
        }
        return manage.getContent();
    }

    @Override
    public WorkflowDefinition loadFlowDef(Long id) {


        String content = loadFlowContent(id);
        if (StringUtils.isEmpty(content)) return null;
        WorkflowDefinition def = WorkflowParser.parse(content);
        if (CollectionUtils.isNotEmpty(def.getTasks())) {
            for (StepDefinition task : def.getTasks()) {
                if (task.getMetadata() instanceof HttpStepMetadata) {
                    HttpStepMetadata httpStepMetadata = (HttpStepMetadata) task.getMetadata();
                    if (Objects.nonNull(httpStepMetadata.getInterfaceID()) && httpStepMetadata.getInterfaceID() > 0) {
                        InterfaceManage interfaceManage = interfaceManageService.getById(httpStepMetadata.getInterfaceID());
                        if (Objects.nonNull(interfaceManage)) {
                            httpStepMetadata.setAppId(interfaceManage.getAppId());
                        }
                    }
                }
            }

        }

        return def;
    }

    @Override
    public Object invokeFlow() {
        return null;
    }
}

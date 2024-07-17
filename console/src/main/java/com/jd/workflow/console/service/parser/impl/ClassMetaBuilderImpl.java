package com.jd.workflow.console.service.parser.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jd.common.util.StringUtils;
import com.jd.fastjson.JSON;
import com.jd.fastjson.JSONObject;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.model.sync.BuildReportContext;
import com.jd.workflow.console.service.depend.InterfaceServiceWrap;
import com.jd.workflow.console.service.parser.DocReportDtoBuilderService;
import com.jd.workflow.console.service.parser.ParserUtils;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Service(value = "classMetaBuilder")
public class ClassMetaBuilderImpl implements DocReportDtoBuilderService<DocReportDto> {

    @Autowired
    private InterfaceServiceWrap interfaceServiceWrap;
    @Autowired
    private DocReportDtoBuilderService<MethodMetadata> methodInputBuilder;
    @Autowired
    private DocReportDtoBuilderService<MethodMetadata> methodOutputBuilder;

    @Override
    public DocReportDto build(DocReportDto docReportDto, BuildReportContext context) {
        ClassMetadata classMetadata = new ClassMetadata();
        String interfaceName = context.getInterfaceInfo().getInterfaceName();
        classMetadata.setClassName(context.getInterfaceInfo().getInterfaceName());
        classMetadata.setDesc(context.getInterfaceInfo().getRemark());
        List<String> methodList = interfaceServiceWrap.getMethodList(context.getInterfaceInfo().getInterfaceName());
        if (CollectionUtils.isNotEmpty(methodList)) {
            for (String method : methodList) {
                String methodName = ParserUtils.getMethodName(method);
                if(StringUtils.isEmpty(methodName)) continue;
                String methodInfo = interfaceServiceWrap.getMethodInfo(interfaceName,methodName );
                JSONObject jsonObject = JSON.parseObject(methodInfo);
                context.setMethodJsonObject(jsonObject);
                MethodMetadata methodMetadata = methodInputBuilder.build(new MethodMetadata(), context);
                methodMetadata = methodOutputBuilder.build(methodMetadata, context);
                classMetadata.getMethods().add(methodMetadata);
            }
        }
        List<ClassMetadata> classMetadataList = Arrays.asList(classMetadata);
        docReportDto.setJsfDocs(JsonUtils.toJSONString(classMetadataList));
        return docReportDto;
    }
}

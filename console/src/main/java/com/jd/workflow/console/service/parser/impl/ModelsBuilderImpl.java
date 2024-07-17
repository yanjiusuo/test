package com.jd.workflow.console.service.parser.impl;

import com.jd.matrix.core.utils.CollectionUtils;
import com.jd.workflow.console.dto.doc.ApiClassModel;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.model.sync.BuildReportContext;
import com.jd.workflow.console.service.parser.DocReportDtoBuilderService;
import com.jd.workflow.console.service.parser.ParserUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Service(value = "modelsBuilder")
public class ModelsBuilderImpl implements DocReportDtoBuilderService<DocReportDto> {
    @Override
    public DocReportDto build(DocReportDto docReportDto, BuildReportContext context) {
        List<String> classNameList = new ArrayList<>();
        List<BuilderJsonType> allBuilderJsonTypes = context.getAllBuilderJsonTypes();
        List<ApiClassModel> apiClassModels = new ArrayList<>();
        if (!CollectionUtils.isEmpty(allBuilderJsonTypes)) {
            for (BuilderJsonType allBuilderJsonType : allBuilderJsonTypes) {
                String className = allBuilderJsonType.getClassName();
                if(classNameList.contains(className)){
                     continue;
                }
                if (!ParserUtils.isJdkClass(className)) {
                    ApiClassModel apiClassModel = new ApiClassModel();
                    apiClassModel.setClassName(allBuilderJsonType.getClassName());
                    apiClassModel.setModel((ObjectJsonType) allBuilderJsonType.toJsonType());
                    apiClassModels.add(apiClassModel);
                    classNameList.add(className);
                }
            }
        }
        docReportDto.setModels(apiClassModels);
        return docReportDto;
    }
}

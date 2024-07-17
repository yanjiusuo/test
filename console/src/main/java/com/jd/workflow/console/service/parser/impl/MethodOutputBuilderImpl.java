package com.jd.workflow.console.service.parser.impl;

import com.jd.common.util.StringUtils;
import com.jd.fastjson.JSONObject;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.model.sync.BuildReportContext;
import com.jd.workflow.console.service.parser.DocReportDtoBuilderService;
import com.jd.workflow.console.service.parser.ParserService;
import com.jd.workflow.console.service.parser.ParserUtils;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Service(value = "methodOutputBuilder")
public class MethodOutputBuilderImpl  implements DocReportDtoBuilderService<MethodMetadata> {

    @Autowired
    private ParserService parserService;
    /**
     * 构建方法入参信息
     *
     * @param context
     * @return
     */
    @Override
    public MethodMetadata build(MethodMetadata methodMetadata, BuildReportContext context) {
        JSONObject methodJsonObject = context.getMethodJsonObject();
        String returnType = methodJsonObject.getString("returnType");
        BuilderJsonType builderJsonType = null;
        if(returnType.indexOf("<") != -1){
            ParserUtils.inferPropertyType(methodJsonObject, returnType);
            builderJsonType = parserService.parseJsonType(new ArrayList(),null, methodJsonObject,"returnType",methodJsonObject);
        }else{
            builderJsonType = parserService.parseJsonType(new ArrayList(),null, methodJsonObject,"returnType",null);
        }

        context.getAllBuilderJsonTypes().add(builderJsonType);
        methodMetadata.setOutput(builderJsonType.toJsonType());
        return methodMetadata;
    }


}

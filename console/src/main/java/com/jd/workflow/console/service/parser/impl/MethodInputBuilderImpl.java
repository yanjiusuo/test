package com.jd.workflow.console.service.parser.impl;

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

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Service(value = "methodInputBuilder")
public class MethodInputBuilderImpl  implements DocReportDtoBuilderService<MethodMetadata> {

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
        methodMetadata.setInterfaceName(context.getInterfaceInfo().getInterfaceName());
        methodMetadata.setMethodName(methodJsonObject.getString("methodName"));
        List<JSONObject> parameters = methodJsonObject.getJSONArray("parameters").toJavaList(JSONObject.class);
        List<JsonType> jsonTypeList = new ArrayList<>();
        for (JSONObject parameter : parameters) {
            BuilderJsonType builderJsonType = parserService.parseJsonType(new ArrayList(),null, parameter,null,null);
            context.getAllBuilderJsonTypes().add(builderJsonType);
            jsonTypeList.add(builderJsonType.toJsonType());
        }
        methodMetadata.setInput(jsonTypeList);
        return methodMetadata;
    }

}

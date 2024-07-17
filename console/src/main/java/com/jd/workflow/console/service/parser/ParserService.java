package com.jd.workflow.console.service.parser;

import com.jd.fastjson.JSONObject;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;

import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/24
 */
public interface ParserService {

    BuilderJsonType parseJsonType(List<String> parentClassList,List<BuilderJsonType> jsonTypes, JSONObject jsonObject,String dealKey,JSONObject parentJsonObject);
}

package com.jd.workflow.codegen.language;

import com.jd.workflow.codegen.FileCode;
import com.jd.workflow.codegen.GenerateConfig;
import com.jd.workflow.codegen.GroupModels;
import com.jd.workflow.codegen.ILanguageCodeRender;
import com.jd.workflow.codegen.model.ApiModel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class TypescriptLanguageRender extends ILanguageCodeRender {
    @Override
    public List<FileCode> render(List<ApiModel> apiGroups, GroupModels genericModels, Map<ApiModel, GroupModels> groupModels,
                                 GenerateConfig config) {
        List<FileCode> result = new ArrayList<>();
        for (ApiModel apiModel : apiGroups) { // 生成api代码
            Map<String, Object> apiData = new HashMap<>();
            apiData.put("api", apiModel);
            apiData.put("config", config);
            log.info("api.generate_api_Model:path={},desc={}",apiModel.getSavedPath(),apiModel.getDesc());
            String interfaceCode = getCodeGenerator().generateCode(apiData, "api.ftl");
            log.info("api.gene_api_code:path={},code={}",apiModel.getSavedPath(),interfaceCode);

            FileCode code = new FileCode();
            code.setCode(interfaceCode);
            code.setFileName(getFileName(apiModel.getSavedPath()));
            code.setFilePath(apiModel.getSavedPath());
            result.add(code);
        }

        { // 生成js代码
            if(!genericModels.getModels().isEmpty()){
                Map<String, Object> map = new HashMap<>();
                map.put("group", genericModels);
                map.put("config", config);
                String code = getCodeGenerator().generateCode(map, "model.ftl");
                FileCode fileCode = new FileCode();
                fileCode.setCode(code);
                fileCode.setFileName(getFileName(genericModels.getSavedPath()));
                fileCode.setFilePath(genericModels.getSavedPath());
                result.add(fileCode);


            }

        }

        for (Map.Entry<ApiModel,GroupModels> entry : groupModels.entrySet()) { // 生成分组代码
            Map<String,Object> map = new HashMap<>();
            map.put("group",entry.getValue());
            map.put("config", config);
            String code = getCodeGenerator().generateCode(map, "model.ftl");
            if(entry.getValue().getSavedPath() == null) continue;


            FileCode fileCode = new FileCode();
            fileCode.setCode(code);
            fileCode.setFileName(getFileName(entry.getValue().getSavedPath()));
            fileCode.setFilePath(entry.getValue().getSavedPath());
            result.add(fileCode);
        }
        return result;
    }

    @Override
    public String getType() {
        return "ts";
    }


}

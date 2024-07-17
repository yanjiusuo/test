package com.jd.workflow.codegen;

import com.jd.workflow.codegen.model.ApiModel;

import java.util.List;
import java.util.Map;

public abstract class ILanguageCodeRender {
    CodeGenerator codeGenerator;
    String type;

    public abstract String getType() ;

    public String getFileName(String filePath){
        String[] split = filePath.split("[\\\\/]");
        return split[split.length-1];
    }

    public void setCodeGenerator(CodeGenerator codeGenerator){
        this.codeGenerator = codeGenerator;
    }
    public CodeGenerator getCodeGenerator(){
        return codeGenerator;
    }
    public abstract List<FileCode> render(List<ApiModel> apiGroups, GroupModels genericModels, Map<ApiModel, GroupModels> groupModels,
                                          GenerateConfig config);
}

package com.jd.workflow.console.service.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/25
 */

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.service.model.IModelManageService;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.RefObjectJsonType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/25 
 */
@Slf4j
@Service
public class ModelManageServiceImpl implements IModelManageService {

    /**
     * 保存模型的差量
     *
     * @param beforeApiModel
     * @param afterApiModel
     */
    @Override
    public void saveDeltaModel(ApiModel beforeApiModel, ApiModel afterApiModel) {

    }

    /**
     * 合并模型的差量，保存的时候使用
     *
     * @param apiModels 要合并的模型
     */
    @Override
    public void mergeDeltaModel(List<ApiModel> apiModels) {

    }

    /**
     * 将模型的引用全部实例化，加载模型的时候使用
     *
     * @param apiModels
     */
    @Override
    public void instanceModelRefs(List<ApiModel> apiModels) {

    }

    /**
     * 简化模型的引用信息，保存模型的时候使用，只保留引用的模型名
     *
     * @param apiModels
     */
    @Override
    public void simplifyModelRefs(List<ApiModel> apiModels) {

    }
    private boolean hasRef(JsonType jsonType){
        if(jsonType instanceof RefObjectJsonType
                && !StringUtils.isBlank(((RefObjectJsonType) jsonType).getRefName())
        ){
            return true;
        }
        if(jsonType instanceof ComplexJsonType){
            for(JsonType childJsonType:((ComplexJsonType) jsonType).getChildren()){
                if(hasRef(childJsonType)){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void saveModelDelta(ApiModel apiModel, boolean onlyRef) {
        if(onlyRef && !hasRef(apiModel.getContent())){
            return;
        }

    }

    /**
     * 简化方法的引用信息，保存的时候使用，只保留引用的模型名
     *
     * @param methodInfos
     */
    @Override
    public void simplifyMethodRefs(List<IMethodInfo> methodInfos) {

    }

    /**
     * 实例化方法的引用信息，加载方法的时候使用
     *
     * @param methodInfos
     */
    @Override
    public void instanceMethodRefs(List<IMethodInfo> methodInfos) {

    }

    @Override
    public void removeDeltaModel(ApiModel beforeApiModel) {

    }
}

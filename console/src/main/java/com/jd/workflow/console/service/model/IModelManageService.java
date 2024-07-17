package com.jd.workflow.console.service.model;

import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.model.ApiModel;

import java.util.List;

/**
 * 模型管理服务类，加载或者保存模型以及方法的时候使用
 */
public interface IModelManageService {
    /**
     * 保存模型的差量
     *
     * @param beforeApiModel
     * @param afterApiModel
     */
    public void saveDeltaModel(ApiModel beforeApiModel, ApiModel afterApiModel);

    /**
     * 合并模型的差量，保存的时候使用
     *
     * @param apiModels 要合并的模型
     */
    public void mergeDeltaModel(List<ApiModel> apiModels);

    /**
     * 将模型的引用全部实例化，加载模型的时候使用
     *
     * @param apiModels
     */
    public void instanceModelRefs(List<ApiModel> apiModels);

    /**
     * 简化模型的引用信息，保存模型的时候使用，只保留引用的模型名
     *
     * @param apiModels
     */
    public void simplifyModelRefs(List<ApiModel> apiModels);

    /**
     * 计算模型的差量，保存的时候使用
     * @param apiModel
     * @param onlyRef 是否只有引用字段才计算差量。非引用字段不计算差量
     */
    public void saveModelDelta(ApiModel apiModel,boolean onlyRef);

    /**
     * 简化方法的引用信息，保存的时候使用，只保留引用的模型名
     *
     * @param methodInfos
     */
    public void simplifyMethodRefs(List<IMethodInfo> methodInfos);

    /**
     * 实例化方法的引用信息，加载方法的时候使用
     *
     * @param methodInfos
     */
    public void instanceMethodRefs(List<IMethodInfo> methodInfos);

    /**
     * 删除对象时，删除对应的差量
     *
     * @param beforeApiModel
     */
    void removeDeltaModel(ApiModel beforeApiModel);
}

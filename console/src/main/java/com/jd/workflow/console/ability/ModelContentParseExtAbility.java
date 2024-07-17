package com.jd.workflow.console.ability;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */

import com.alibaba.fastjson.JSON;
import com.jd.matrix.core.SimpleReducer;
import com.jd.matrix.core.annotation.DomainAbility;
import com.jd.matrix.core.base.BaseDomainAbility;
import com.jd.workflow.common.ObjectJsonType;
import com.jd.workflow.domain.ModelContentParseModel;
import com.jd.workflow.matrix.ext.matrix1.ModelContentParseExt;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */
@DomainAbility(parent = "test.data_transform", name = "japi相关")
@Slf4j
public class ModelContentParseExtAbility extends BaseDomainAbility<ModelContentParseModel, ModelContentParseExt> {

    public ObjectJsonType parseContent(ModelContentParseModel modelContentParseModel) {
        log.info("ModelContentParseExt 扩展点入口参数{}", JSON.toJSONString(modelContentParseModel));
        ModelContentParseExt ext = this.getExtension(ModelContentParseExt.class, modelContentParseModel, SimpleReducer.firstOf(Objects::nonNull));
        if (ext == null) {
            throw new RuntimeException("未获取到扩展点对象！");
        }
        return ext.parseContent(modelContentParseModel);
    }

    @Override
    public ModelContentParseExt getDefaultExtension() {
        return null;
    }
}

package com.jd.workflow.console.ability;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */

import com.alibaba.fastjson.JSON;
import com.jd.matrix.core.SimpleReducer;
import com.jd.matrix.core.annotation.DomainAbility;
import com.jd.matrix.core.base.BaseDomainAbility;
import com.jd.workflow.domain.MethodModifyModel;
import com.jd.workflow.domain.ModelContentParseModel;
import com.jd.workflow.matrix.ext.matrix1.MethodModifyExt;
import com.jd.workflow.matrix.ext.matrix1.ModelContentParseExt;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */
@DomainAbility(parent = "test.data_transform", name = "japi相关")
@Slf4j
public class MethodModifyExtAbility extends BaseDomainAbility<MethodModifyModel, MethodModifyExt> {

    public void onMethodBeforeUpdate(MethodModifyModel methodModifyModel) {
        log.info("MethodModifyExtAbility onMethodBeforeUpdate 扩展点入口参数{}", JSON.toJSONString(methodModifyModel));
        MethodModifyExt ext = this.getExtension(MethodModifyExt.class, methodModifyModel, SimpleReducer.firstOf(Objects::nonNull));
        if (ext == null) {
            log.info("MethodModifyExtAbility 未找到扩展点");
        }
        ext.onMethodBeforeUpdate(methodModifyModel);
    }

    public void onMethodAfterUpdate(MethodModifyModel methodModifyModel) {
        log.info("MethodModifyExtAbility onMethodAfterUpdate 扩展点入口参数{}", JSON.toJSONString(methodModifyModel));
        MethodModifyExt ext = this.getExtension(MethodModifyExt.class, methodModifyModel, SimpleReducer.firstOf(Objects::nonNull));
        if (ext == null) {
            log.info("MethodModifyExtAbility 未找到扩展点");
        }
        ext.onMethodAfterUpdate(methodModifyModel);
    }

    @Override
    public MethodModifyExt getDefaultExtension() {
        return null;
    }
}

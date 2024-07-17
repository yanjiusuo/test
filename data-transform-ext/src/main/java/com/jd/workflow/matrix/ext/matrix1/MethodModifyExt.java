package com.jd.workflow.matrix.ext.matrix1;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */

import com.jd.matrix.sdk.annotation.DomainAbilityExtension;
import com.jd.matrix.sdk.base.IDomainAbilityExtension;
import com.jd.workflow.domain.MethodModifyModel;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */
public interface MethodModifyExt extends IDomainAbilityExtension {

    @DomainAbilityExtension(code = "METHOD_UPDATE_BEFORE", name = "解析字符串")
    void onMethodBeforeUpdate(MethodModifyModel methodModifyModel);

    @DomainAbilityExtension(code = "METHOD_UPDATE_AFTER", name = "解析字符串")
    void onMethodAfterUpdate(MethodModifyModel methodModifyModel);
}

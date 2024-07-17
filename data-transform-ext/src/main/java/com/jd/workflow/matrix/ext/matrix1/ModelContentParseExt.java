package com.jd.workflow.matrix.ext.matrix1;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */

import com.jd.matrix.sdk.annotation.DomainAbilityExtension;
import com.jd.matrix.sdk.base.IDomainAbilityExtension;
import com.jd.workflow.common.ObjectJsonType;
import com.jd.workflow.domain.ModelContentParseModel;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23 
 */
public interface ModelContentParseExt  extends IDomainAbilityExtension {

    /**
     * 解析字符串
     * @param modelContentParseModel
     * @return
     */
    @DomainAbilityExtension(code ="PARSE_CONTENT_EXT" , name = "解析字符串")
    ObjectJsonType parseContent(ModelContentParseModel modelContentParseModel);
}

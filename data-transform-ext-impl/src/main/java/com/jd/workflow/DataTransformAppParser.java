package com.jd.workflow;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */

import com.alibaba.druid.support.json.JSONUtils;
import com.jd.matrix.sdk.base.BizCodeParser;
import com.jd.matrix.sdk.base.DomainModel;
import com.jd.workflow.domain.MethodModifyModel;
import com.jd.workflow.domain.ModelContentParseModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */
@Slf4j
public class DataTransformAppParser implements BizCodeParser {
    @Override
    public boolean filter(DomainModel domainModel) {
        log.info("DataTransformAppParser入参{}", JSONUtils.toJSONString(domainModel));
        if (domainModel instanceof ModelContentParseModel) {
            return true;
        }
        if (domainModel instanceof MethodModifyModel) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> parseScenario(DomainModel domainModel) {
        return null;
    }
}

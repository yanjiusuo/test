package com.jd.workflow.console.matrix.impl;

import com.jd.common.util.StringUtils;
import com.jd.matrix.generic.annotation.GenericExtensionImpl;
import com.jd.workflow.matrix.ext.spi.HealthCheckSPI;
import com.jd.workflow.method.MethodInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GenericExtensionImpl(bizCode = "healthCheck", bizName = "healthCheck", group = "jd")
public class DocHealthCheckImpl implements HealthCheckSPI {
    @Override
    public Boolean isTypeValid(MethodInfo content) {
        return StringUtils.isNotEmpty(content.getDocInfo());
    }

    @Override
    public String getType() {
        return "docInfo";
    }
}

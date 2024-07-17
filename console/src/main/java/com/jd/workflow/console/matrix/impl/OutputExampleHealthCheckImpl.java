package com.jd.workflow.console.matrix.impl;

import com.jd.common.util.StringUtils;
import com.jd.matrix.generic.annotation.GenericExtensionImpl;
import com.jd.workflow.console.service.impl.ScoreManageService;
import com.jd.workflow.matrix.ext.spi.HealthCheckSPI;
import com.jd.workflow.method.MethodInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GenericExtensionImpl(bizCode = "healthCheck", bizName = "healthCheck", group = "jd")
public class OutputExampleHealthCheckImpl implements HealthCheckSPI {

    @Autowired
    ScoreManageService scoreManageService;
    @Override
    public Boolean isTypeValid(MethodInfo content) {
        return StringUtils.isNotEmpty(content.getOutputExample());
    }

    @Override
    public String getType() {
        return "outputExample";
    }
}

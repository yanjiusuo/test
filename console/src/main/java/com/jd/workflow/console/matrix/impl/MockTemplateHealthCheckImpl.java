package com.jd.workflow.console.matrix.impl;

import com.jd.common.util.StringUtils;
import com.jd.matrix.generic.annotation.GenericExtensionImpl;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.matrix.ext.spi.HealthCheckSPI;
import com.jd.workflow.method.MethodInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GenericExtensionImpl(bizCode = "healthCheck", bizName = "healthCheck", group = "jd")
public class MockTemplateHealthCheckImpl implements HealthCheckSPI {

    @Autowired
    EasyMockRemoteService testEasyMockRemoteService;

    @Autowired
    IInterfaceManageService interfaceManageService;
    @Override
    public Boolean isTypeValid(MethodInfo content) {
        MethodManage methodManage = new MethodManage();
        InterfaceManage interfaceManage = interfaceManageService.getById(content.getInterfaceId());
        BeanUtils.copyProperties(content,methodManage);
        testEasyMockRemoteService.existTemplate(interfaceManage,methodManage);
        return StringUtils.isNotEmpty(content.getDocInfo());
    }

    @Override
    public String getType() {
        return "mockTemplate";
    }
}

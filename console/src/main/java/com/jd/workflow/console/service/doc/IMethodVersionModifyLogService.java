package com.jd.workflow.console.service.doc;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;

import java.util.List;

public interface IMethodVersionModifyLogService extends IService<MethodVersionModifyLog> {
    public void removeByInterfaceId(Long interfaceId);
    public void removeByMethodIds(List<Long> methodIds);

    public List<MethodVersionModifyLog> listMethodVersions(Long methodId);
}

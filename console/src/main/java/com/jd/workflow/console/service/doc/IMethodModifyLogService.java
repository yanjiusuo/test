package com.jd.workflow.console.service.doc;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.doc.ListModifyLogDto;
import com.jd.workflow.console.dto.version.CompareMethodVersionDTO;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodModifyLog;

import java.util.List;

public interface IMethodModifyLogService extends IService<MethodModifyLog> {


    public IPage<MethodModifyLog> listModifyLogs(ListModifyLogDto dto);


    public MethodModifyLog getDetailById(Long id);

    public CompareMethodVersionDTO compareMethod(Long id);

    public void removeByInterfaceId(Long interfaceId);
    public void removeByMethodIds(List<Long> methodIds);

    public Boolean updateLogRemark(Long id, String remark);
}

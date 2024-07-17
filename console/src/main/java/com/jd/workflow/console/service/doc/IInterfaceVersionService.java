package com.jd.workflow.console.service.doc;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.version.CompareMethodVersionDTO;
import com.jd.workflow.console.dto.version.CompareVersionDTO;
import com.jd.workflow.console.dto.version.InterfaceInfo;
import com.jd.workflow.console.dto.version.InterfaceInfoReq;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.doc.InterfaceVersion;

import java.util.List;

public interface IInterfaceVersionService extends IService<InterfaceVersion> {
    public InterfaceVersion getInterfaceVersion(Long interfaceId,String version);

    public InterfaceVersion initInterfaceVersion(InterfaceManage manage);
    /**
     * 获取接口基本信息
     * @param req
     * @return
     */
    public InterfaceInfo findInterfaceBaseInfo(InterfaceInfoReq req);

    /**
     * 查询版本的分组信息
     * @param req
     * @return
     */
    public MethodGroupTreeDTO findMethodGroupTree(InterfaceInfoReq req);

    /**
     * 创建版本迭代
     * @param req
     * @return
     */
    public String createInterfaceVersion(InterfaceInfoReq req);

    /**
     * 展示下个迭代版本
     * @param req
     * @return
     */
    public String viewNextInterfaceVersion(InterfaceInfoReq req);

    /**
     * 版本列表
     * @param req
     * @return
     */
    public List<InterfaceVersion> findInterfaceVersion(InterfaceInfoReq req);

    /**
     * 版本比对
     * @param req
     * @return
     */
    public CompareVersionDTO compareInterfaceVersion(InterfaceInfoReq req);
    public MethodManageDTO getVersionMethod(String version, Long methodId);
    /**
     * 版本方法比对展示
     * @param req
     * @return
     */
    public CompareMethodVersionDTO compareMethodVersion(InterfaceInfoReq req);

    void updateInterfaceVersion(InterfaceVersion interfaceVersion);

    public void removeInterfaceVersion(Long interfaceId);
}

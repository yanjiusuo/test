package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.version.CompareVersionDTO;
import com.jd.workflow.console.dto.version.InterfaceInfoReq;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceMethodGroup;

import java.util.List;
import java.util.Map;

/**
 * 项目名称：parent
 * 类 名 称：IInterfaceMethodGroupService
 * 类 描 述：接口下方法分组管理
 * 创建时间：2022-11-08 16:36
 * 创 建 人：wangxiaofei8
 */
public interface IInterfaceMethodGroupService extends IService<InterfaceMethodGroup> {

     public Long addGroup(String name,String enName,Long interfaceId,Long parentId);

     public Boolean modifyGroupName(Long id,String name,String enName);

     public Boolean removeGroup(Long id);

     public Map<Long,InterfaceMethodGroup> findInterfaceGroups(Long interfaceId);

     public MethodGroupTreeDTO findMethodGroupTree(Long interfaceId);


     public MethodGroupTreeDTO findAppHttpTree(Long appId);

     public Boolean modifyMethodGroupTree(MethodGroupTreeDTO dto);

     public MethodGroupTreeDTO findMethodGroupTreeSnapshot(InterfaceManage interfaceMangae,MethodGroupTreeModel groupTreeSnapshot);

     public MethodGroupTreeModel createGroupTreeSnapshot(InterfaceManage interfaceMangae);

     public void findGroupTreeVersionDiff(InterfaceInfoReq req, InterfaceManage interfaceObj,CompareVersionDTO dto);

     public List<InterfaceMethodGroup> searchGroup(int type,String search,List<Long> interfaceIds);

     public List<InterfaceMethodGroup> list(Long interfaceId,int type);

     MethodGroupTreeDTO findAppJsfTree(Long appId);

}

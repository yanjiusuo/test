package com.jd.workflow.server.service;

import com.jd.workflow.server.dto.*;
import com.jd.workflow.server.dto.tree.InterfaceTreeItem;

import java.util.List;

/**
 * 获取需求对应的jsf或者http接口
 * @Api
 */
public interface InterfaceGetRpcService {
    /**
     * 查询http或者jsf接口
     * @param type 1:http 3:jsf
     * @param flowId 流程id
     * @return
     */
    public QueryResult<List<JsfOrHttpInfo>> queryJsfOrHttp(int type, Long flowId);

    /**
     * 查询jsf接口的访问链接
     * @param jsfName jsf接口名
     * @return 为空是，JsfDocInfo的id为空
     */
    public QueryResult<JsfDocInfo> queryJsfUrl(String jsfName);



    /**
     * 查询jsf或者http接口列表
     * @param appType 1-jdos 2-藏经阁
     * @param appCode appType为1时，appCode为jdos的appCode，appType为2时，appCode为藏经阁的appCode
     * @return 应用不存在或者接口为空的时候返回空列表，否则返回接口列表
     */
    public QueryResult<List<JsfOrHttpInterfaceInfo>> queryJsfOrHttpInterface(int appType, String appCode);


    /**
     * 查询jsf或者http方法信息
     * @param interfaceId 接口id
     * @param current 当前页，从1开始
     * @param size 每页大小
     * @return 方法信息列表
     */
    public QueryResult<Pageable<JsfOrHttpMethodInfo>> queryJsfOrHttpMethodInfo(Long interfaceId, int current, int size);

    /**
     * 获取http或者jsf接口的列表(包含已删除的)
     * @param interfaceId 接口分组id
     * @param current 当前页
     * @param size 每页大小
     * @return 方法信息列表
     */
    public QueryResult<Pageable<JsfOrHttpMethodInfo>> queryJsfOrHttpMethodInfoIncludeDeleted(Long interfaceId, int current, int size);

    /**
     * 根据id查询方法信息
     * @param id 方法id
     * @return jsf或者http方法信息
     */
    public QueryResult<JsfOrHttpMethodInfo> queryMethodById(Long id);

    /**
     * 获取版本下拉列表
     * @param interfaceId 接口id
     * @return
     */
    public QueryResult<List<InterfaceVersionDto>> listInterfaceVersion(Long interfaceId);

    /**
     * 查询指定版本的方法出入参信息
     * @param id 方法id
     * @param version
     * @return
     */
    public QueryResult<JsfOrHttpMethodInfo> queryVersionMethodInfo(Long id,String version);

    /**
     * 查询最新版本接口对应的树节点信息
     * @param interfaceId 接口id
     * @return 树节点列表
     */
    public QueryResult<List<InterfaceTreeItem>> queryLatestInterfaceTree(Long interfaceId);

    /**
     * 查询应用节点树
     * @param appCode 应用编码
     * @return
     */
    public QueryResult<List<InterfaceTreeItem>> queryLatestInterfaceAndMethodTree(String appCode);

    /**
     * 查询指定版本的接口树节点信息
     * @param interfaceId 接口id
     * @param version 版本号
     * @return 树节点列表
     */
    public QueryResult<List<InterfaceTreeItem>> queryVersionInterfaceTree(Long interfaceId,String version);

    /**
     * 导出文档
     * @param docType 文档类型：md、html、pdf
     * @param erp 操作人
     * @param treeItems 要导出的文档节点列表
     * @return
     */
    public QueryResult<DocExportDto> exportDoc(String docType,String erp, List<InterfaceTreeItem> treeItems);

    /**
     * 查询版本方法列表
     * @param methodId
     * @return
     */
    public QueryResult<List<InterfaceVersionDto>> queryMethodVersions(Long methodId);

    /**
     * 撤销删除方法id
     * @param methodId 方法id
     * @return 方法id不存在或者不是删除状态返回false，否则返回true。如果异常则返回码非0
     */
    public QueryResult<Boolean> cancelDelete(Long methodId);

    public QueryResult<List<AppHasInterfaceResult>> queryAppHasInterfaceResult(List<String> jdosAppCodes);

    /**
     * 查询jsf接口的权限信息
     * @param appCode 应用编码
     * @param interfaceName 接口名
     * @return
     */
    public QueryResult<Boolean> removeJsfAuth(String appCode,String interfaceName);
}

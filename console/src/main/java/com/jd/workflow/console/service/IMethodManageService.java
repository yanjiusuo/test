package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.auth.InterfaceAuthFilter;
import com.jd.workflow.console.dto.datasource.DataSourceInvokeDto;
import com.jd.workflow.console.dto.doc.AppInterfaceCount;
import com.jd.workflow.console.dto.doc.InterfaceTypeCount;
import com.jd.workflow.console.dto.doc.UpdateMethodConfigDto;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.dto.errorcode.MethodPropParam;
import com.jd.workflow.console.dto.jsf.JsfImportDto;
import com.jd.workflow.console.dto.requirement.DemandDetailDTO;
import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.MethodDocDto;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.flow.core.output.HttpOutput;
import io.swagger.annotations.Api;

import java.util.List;
import java.util.Map;

/**
 * @author wubaizhao1
 * @date: 2022/5/16 18:20
 */
@Api(value = "方法管理接口")
public interface IMethodManageService extends IService<MethodManage> {

    /**
     * 分页查询
     * 入参 ： 租户id 接口id 可选：type name【模糊】
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:38
     * @author wubaizhao1
     */
    Page<MethodManageDTO> pageMethod(MethodManageDTO methodManageDTO);

    public MethodManage getMethodExcludeBigTextField(Long id);

    public void updateMethodInterfaceId(List<Long> methodIds, Long interfaceId);

    public List<MethodManage> getInterfaceMethods(Long interfaceId);

    public void updateMethodDigest();

    /**
     * 没有字段的方法需要重新查询数据库，并且去掉大字段。上面查询数据库的时候，跳过了大字段。
     *
     * @param noContentMethods
     * @return
     */
    public List<MethodManage> fixNoContentMethodsDigest(List<MethodManage> noContentMethods);

    void addListener(InterfaceChangeListener listener);

	public List<MethodManage> listScoreFields(List<Long> ids,Long appId);


    /**
     * 添加
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:40
     * @author wubaizhao1
     */
    Long add(MethodManageDTO methodManageDTO);

    void addColorInfo(String ss);

    /**
     * 修改
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:39
     * @author wubaizhao1
     */
    Long edit(MethodManageDTO methodManageDTO);



    Boolean updateCloudFile(Long id, String path, String tags);

    Long createDoc(MethodDocDto dto);

    /**
     * 更新接口上报状态
     *
     * @return
     */
    Long updateReportStatus(Long methodId, Integer reportStatus);

    boolean updateStatus(Long methodId, Integer status);



    boolean updateFunctionId(Long methodId, String zone, String functionId, String type);

    String getMethodIdByDoc(String docUrl, String key);

    /**
     * 删除
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:39
     * @author wubaizhao1
     */
    Boolean remove(MethodManageDTO methodManageDTO);

    /**
     * @param interfaceId 接口id
     * @return
     */
    Boolean removeByInterfaceId(Long interfaceId);

    /**
     * 根据id获取详情
     *
     * @return
     * @date: 2022/5/24 14:24
     * @author wubaizhao1
     */
    MethodManageDTO getEntity(String methodIdOrBeanId);

    MethodManageDTO getEntityById(Long methodIdOrBeanId);


    Page<DemandDetailDTO> getDemandByInterfaceId(Long interfaceId, long current, long size);

    boolean updateRelatedId(Long methodId, Long relatedId);

    /**
     * 更新webservice方法列表
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/24 16:08
     * @author wubaizhao1
     */
    Boolean updateWebService(MethodManageDTO methodManageDTO);

    /**
     * 更新jsf方法
     *
     * @param dto
     * @param manage
     */
    public void updateJsfMethods(JsfImportDto dto, InterfaceManage manage);

    /**
     * 合并接口下的方法
     *
     * @param newMethods
     * @param interfaceId
     * @param skipListener
     */
    public void mergeMethods(List<MethodManage> newMethods, Long interfaceId, boolean skipListener);

    Long copyJsfMethod(Long groupId, MethodManageDTO newMethod);

    /**
     * @param wsdlPath
     * @throws Exception
     * @date: 2022/5/25 16:26
     * @author wubaizhao1
     */
    Boolean checkWsdlPath(String wsdlPath);

    /**
     * @param wsdlPath
     * @throws Exception
     * @date: 2022/5/25 16:26
     * @author wubaizhao1
     */
    List<MethodManage> wsdlToMethod(Long interfaceId, String wsdlPath, List<EnvModel> models) throws Exception;

    /**
     * 调试方法
     *
     * @param invokeMethodDTO
     * @return
     * @date: 2022/5/19 17:25
     * @author wubaizhao1
     */
    Object invokeMethod(InvokeMethodDTO invokeMethodDTO);

    /**
     * 调试方法
     *
     * @param dto
     * @return
     * @date: 2022/5/19 17:25
     * @author wubaizhao1
     */
    Object invokeDataSourceMethod(DataSourceInvokeDto dto);

    /**
     * 执行webservice方法
     *
     * @param basePath
     * @param dto
     * @return
     */
    public HttpOutput invokeWebService(String basePath, String cookie, CallHttpToWebServiceReqDTO dto);


    public MethodDocConfig updateDocConfig(UpdateMethodConfigDto dto);

    public void fillMethodDigest(IMethodInfo methodManage);

    public boolean removeMethodByIds(List<Long> ids);

    public List<MethodManage> listMethods(List<Long> ids);

    public void initMethodDeltaInfos(List<MethodManage> methods);

    /**
     * 获取方法请求体示例值
     *
     * @param methodId
     * @return
     */
    public Object getMethodReqBodyDemoValue(Long methodId);

    public Object getMethodExampleValue(Long methodId);



    MethodManageDTO getEntity(String methodIdOrBeanId, FilterParam filter);

    public Object getFlowReqBodyDemoValue(Long methodId);

	boolean  exportInterface(List<MethodManageDTO> list);



	public  Page<MethodManage> getInterfaceMethodsIncludeContent(Long interfaceId,Long pageNo,Long size,boolean containsDeleted);
    List<MethodManage> searchMethod(List<Integer> types, String search, List<Long> interfaceIds);

    public Page<MethodManage> getInterfaceMethods(Long interfaceId, Long pageNo, Long size, String search);

    public Page<MethodManage> listMethodsByIds(String search, Integer status, List<Long> ids, long pageNo, long size);

    void updateObject(Long id, String content);

    public List<InterfaceTypeCount> queryInterfaceTypeCount(Long appId);

	public List<AppInterfaceCount> queryInterfaceMethodCount(Long appId);

    /**
     * 初始化方法属性列表
     */
    void initAllMethodProps();


    /**
     * 获取出现次数最多的属性
     * @param methodPropParam
     * @return
     */
    List<MethodPropDTO> getTopProp(MethodPropParam methodPropParam);

    void clearAllProps();

	public List<Long> getExistIds(List<Long> ids);

    List<MethodManage> searchMethod(List<Integer> types, String search, List<Long> interfaceIds,List<Long> methodIds);


    public List<MethodManage> queryMethodByPath(Long interfaceId, String path,Integer type);


    public List<Long> getMethodByCode(String methodCode,Long interFaceId);

    /**
     * 获取文档元数据
     * @param id
     * @return
     */
    MethodManageDTO getMetaMethodInfo(Long id);

    Page<MethodManageDTO> marketMethod(InterfaceAuthFilter filter);


    MethodManageDTO getMethodManageDTOById(String id,FilterParam filter);

    String obtainInterfaceMarkDown(String interFaceName);
}

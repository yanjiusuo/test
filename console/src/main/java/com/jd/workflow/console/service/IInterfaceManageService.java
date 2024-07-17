package com.jd.workflow.console.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.businessworks.domain.FlowBeanInfo;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.datasource.DataSourceDto;
import com.jd.workflow.console.dto.auth.InterfaceAuthFilter;
import com.jd.workflow.console.dto.dept.QueryDeptReqDTO;
import com.jd.workflow.console.dto.dept.QueryDeptResultDTO;
import com.jd.workflow.console.dto.doc.InterfaceDocConfig;
import com.jd.workflow.console.dto.doc.JsfAndHttpInterfaceCountDto;
import com.jd.workflow.console.dto.doc.UpdateInterfaceConfigDto;
import com.jd.workflow.console.dto.doc.UserInterfaceCountDto;
import com.jd.workflow.console.dto.jsf.JsfImportDto;
import com.jd.workflow.console.dto.manage.InterfaceAppSearchDto;
import com.jd.workflow.console.dto.manage.InterfaceOrMethodResult;
import com.jd.workflow.console.dto.manage.MethodSearchResult;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.doc.MethodDocDto;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 接口管理 服务类
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
public interface IInterfaceManageService extends IService<InterfaceManage> {

    /**
     * 新增
     * 入参:
     * 出参: id
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/12 17:55
     * @author wubaizhao1
     */
    Long add(InterfaceManageDTO interfaceManageDTO);

	public InterfaceManage getOneExcludeBigTextField(Long id);


    void addListener(InterfaceChangeListener listener);

    Long importJsfInterface(JsfImportDto jsfImportDto);

    Integer getInterfaceNoticeStatus(Long interfaceId);

    Integer updateInterfaceNoticeStatus(Long interfaceId, int status);


    public Boolean saveJavaBean(List<FlowBeanInfo> beanInfos,Long appId);

    /**
     * 修改
     * 入参:
     * 出参: id
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/12 17:54
     * @author wubaizhao1
     */
    Long edit(InterfaceManageDTO interfaceManageDTO);



    /**
     * 删除
     * 入参: id
     * 出参: boolean
     *
     * @param
     * @return
     * @date: 2022/5/12 17:50
     * @author wubaizhao1
     */
    Boolean remove(Long interfaceId);

	public boolean setInterfaceAppId(Long interfaceId,Long appId);

	/**
	 * 接口分页查询
	 * 入参: 必传租户id ,搜索条件有 接口名称(模糊),类型,负责人
	 * 出参: Page<InterfaceManage>
	 * @date: 2022/5/12 17:48
	 * @author wubaizhao1
	 * @param interfaceManageDTO
	 * @return
	 */
	Page<InterfaceManage> pageList(InterfacePageQuery interfaceManageDTO);

	public boolean hasNoAppInterface();

	public List<InterfaceManage> getNoAppInterfaces(Integer interfaceType);


	/**
	 * 获取接口市场的所有接口
	 * @param filter
	 * @return
	 */
	Page<InterfaceManage> pageMarketInterface(InterfaceAuthFilter filter);

	/**
	 * 获取app 接口类型
	 * @param appInfos
	 * @return
	 */
	Map<Long, List<String>> getAppInterfaceTypes( List<AppInfoDTO> appInfos );

	/**
	 * 接口市场大搜索
	 * @param search 搜索字符串
	 * @param current 当前页数
	 * @param size 分页大小
	 * @return
	 */
	InterfaceOrMethodResult searchInterfaceOrMethod(String search, int current, int size );
	Page<MethodSearchResult> searchMethod(int type, String search, int current, int size );
	Page<InterfaceManage> searchInterface(int type,String search, int current, int size );

	/**
	 * 更新藏经阁业务域
	 * @param dto 业务域标识
	 * @return
	 */
	public boolean updateInterfaceDomain(UpdateBusinessDomainDto dto);

	/**
	 * 分页查询我的接口列表
	 * 入参: 搜索条件有 接口名称(模糊),类型,负责人
	 * 出参: Page<InterfaceManage>
	 * @date: 2022/5/30 18:09
	 * @author wubaizhao1
	 * @param interfaceManageDTO
	 * @return
	 */
	//Page<InterfaceManage> pageListByUser(InterfaceManageDTO interfaceManageDTO);
	/**
	 * @date: 2022/5/24 17:34
	 * @author wubaizhao1
	 * @param id
	 * @return
	 */
	InterfaceManage getOneById(Long id);

	/**
	 * 添加成员
	 * @date: 2022/5/16 14:56
	 * @author wubaizhao1
	 * @param interfaceManageDTO
	 * @return
	 */
	Boolean addMember(InterfaceManageDTO interfaceManageDTO);

    /**
     * 成员列表
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
	Page<MemberRelationWithUser> listMember(InterfaceManageDTO interfaceManageDTO);

    /**
     * 列出所有成员，包括应用的requ
     *
     * @param interfaceId
     * @return
     */
    public List<MemberRelationWithUser> listAllAppMember(Long interfaceId);

    /**
     * 成员列表 会区分是否是该接口的用户
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    List<UserForAddDTO> listMemberForAdd(InterfaceManageDTO interfaceManageDTO);

    /**
     * 复制接口及其包含的所有方法
     *
     * @return
     */
    Long copy(InterfaceCopyDto dto);

    List<InterfaceManage> getCjgRelatedList(String cjgAppId);


    Long addDataSource(DataSourceDto datasourceDto);

    public Long updateDatasource(DataSourceDto datasourceData);

    public InterfaceManage getAppInterface(String appId, String interfaceName, boolean authReport);

	/**
	 * 通过接口名查询接口信息 （authReport 为 0 或 1 ）
	 * @param interfaceName
	 * @return
	 */
	List<InterfaceManage> geInterfaceByName(String interfaceName);
    public List<InterfaceManage> getAppInterface(Long appId);
    public List<InterfaceManage> getAppInterfaces(Long appId, String interfaceName,int type);

	public Page<InterfaceManage> listInterface(InterfaceAppSearchDto dto);
	public List<InterfaceManage> getAppInterfaces(Long appId);
	public List<InterfaceManage> getAppInterfaceIncludeInvalids(Long appId);

    /**
     * 模糊检索某个应用下项目数据
     * @author wufagang
     * @date 2023/4/17 11:12
     * @param appCode 应用id
     * @param search 项目code 支持模糊检索
     */
	Page<InterfaceManage> findInterfaceList(String appCode, String search, Long current, Long size,Integer autoReport ) ;
    public InterfaceDocConfig updateDocConfig(UpdateInterfaceConfigDto dto);

    public UserInterfaceCountDto statisticUserInterfaceCount();

    /**
     * 查询部门信息列表
     *
     * @param query
     * @return
     */
    QueryDeptResultDTO queryDeptList(QueryDeptReqDTO query);

    /**
     * 批量更新接口信息
     *
     * @param interfaceManages
     */
    void batchUpdateInterfaceInfo(List<InterfaceManage> interfaceManages);

    /**
     * 根据AppId查询所有的接口列表
     *
     * @param appId
     * @return
     */
    List<InterfaceManage> queryListByAppId(Long appId);

    /**
     * 查询JSF接口和HTTP接口的接口数量
     *
     * @return
     */
    JsfAndHttpInterfaceCountDto queryNumsByType();

    /**
     * 查询接口所属部门信息
     * @param appId
     * @param adminCode
     * @return
     */
    String getDeptName(Long appId, String adminCode);

   int batchUpdateInterfaceDeptName(List<InterfaceManage> interfaceManages);

    void sendMessage(InterfaceManage manage);

    public List<InterfaceManage> listInterfaceByIds(List<Long> ids);
    public List<InterfaceManage> listInterfaceByIds(List<Long> ids,String search);

    public List<AppInfoDTO> getInterfaceApps(List<Long> interfaceIds);
    public void initInterfaceAppAndAdminInfo(List<InterfaceManage> interfaceManages);

	List<InterfaceManage> listHttpInterfaceByAppId(Long appId);

	List<InterfaceManage> listInterfaceByIdsOnlyOne(List<Long> ids);

	boolean updateCloudFile(Long id, String path, String tags);

	/**
	 * 通过应用id 获取 自动上报 接口信息
	 * @param appIds
	 * @return
	 */
	List<InterfaceManage> getAppInterfaceByAppIdList(List<Long> appIds);
}

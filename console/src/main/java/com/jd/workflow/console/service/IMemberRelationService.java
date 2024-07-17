package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.MemberRelationWithUser;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.UserInfo;

import java.util.List;

/**
 * <p>
 * 接口成员关联表 服务类
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
public interface IMemberRelationService extends IService<MemberRelation> {

	/**
	 * 绑定资源
	 * @date: 2022/5/12 18:38
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 * @return
	 */
	Boolean binding (MemberRelationDTO memberRelationDTO);

	/**
	 * binding 已存在不报错
	 * @param memberRelationDTO
	 * @return
	 */
	Boolean checkAndBinding (MemberRelationDTO memberRelationDTO);
	/**
	 * 修改接口负责人
	 * @date: 2022/6/1 10:36
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 * @return
	 */
	Boolean changeAdminCode(MemberRelationDTO memberRelationDTO);
	/**
	 * 解绑资源
	 * @date: 2022/5/13 14:15
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 */
	Boolean unBinding (MemberRelationDTO memberRelationDTO);

	/**
	 * 查询某用户\某租户 下的某类型资源的id
	 * @date: 2022/5/13 14:18
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 * @return
	 */
	List<Long> listResourceIds(MemberRelationDTO memberRelationDTO);


	/**
	 * 某资源下的各个成员Code
	 * @date: 2022/5/13 14:20
	 * @author wubaizhao1
	 */
	List<String> listUserCodeByResource(MemberRelationDTO memberRelationDTO);

	/**
	 * 查看关系表，带上成员信息
	 * @date: 2022/5/16 14:52
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 * @return
	 */
	List<MemberRelationWithUser> listRelationWithUserInfoByResource(MemberRelationDTO memberRelationDTO);
	/**
	 * 查看关系表，带上成员信息
	 * @date: 2022/5/16 14:52
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 * @return
	 */
	Page<MemberRelationWithUser> pageListRelationWithUserInfoByResource(MemberRelationDTO memberRelationDTO);
	/**
	 * 检查是否为租户管理员
	 * @date: 2022/5/30 17:34
	 * @author wubaizhao1
	 * @param userCode
	 * @return
	 */
	Boolean checkTenantAdmin(String userCode);

	/**
	 * 获取某资源的负责人
	 * @date: 2022/5/31 16:39
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 * @return
	 */
	UserInfo getAdminWithUser(MemberRelationDTO memberRelationDTO);
	/**
	 * 检查某资源的权限
	 * @date: 2022/5/16 12:01
	 * @author wubaizhao1
	 * @param memberRelationDTO
	 * @return
	 */
	ResourceRoleEnum getRole(MemberRelationDTO memberRelationDTO);
	public void fixInterfaceAdminInfo(List<InterfaceManage> interfaceManages, Integer interfaceType);


	/**
	 * 资源权限校验
	 * @param memberRelationDTO
	 * @return
	 */
	public boolean checkResourceAuth(MemberRelationDTO memberRelationDTO);


	public List<MemberRelation> listByInterfaceId(Long interfaceId);


}

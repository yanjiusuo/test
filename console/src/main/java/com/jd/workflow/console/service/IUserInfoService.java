package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.LoginDto;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.dto.UserPinDTO;
import com.jd.workflow.console.dto.dept.QueryDeptResultDTO;
import com.jd.workflow.console.entity.UserInfo;

import java.util.List;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
public interface IUserInfoService extends IService<UserInfo> {

	/**
	 * 新增
	 * 出参: id
	 * @date: 2022/5/11 19:54
	 * @author wubaizhao1
	 * @param userInfoDTO
	 * @return
	 */
	Long add(UserInfoDTO userInfoDTO);
	Long register(UserInfoDTO userInfoDTO);

	/**
	 * 入参： id 或者 [唯一索引]租户，登录类型，用户编码
	 * 出参： id
	 * @date: 2022/5/11 19:54
	 * @author wubaizhao1
	 * @param userInfoDTO
	 * @return
	 */
	Long edit(UserInfoDTO userInfoDTO);

	/**
	 * 删除
	 * @date: 2022/5/13 15:59
	 * @author wubaizhao1
	 * @param userInfoDTO
	 * @return
	 */
	Boolean remove(UserInfoDTO userInfoDTO);

	/**
	 * @date: 2022/5/24 10:27
	 * @author wubaizhao1
	 * @param userCode
	 * @return
	 */
	UserInfo getOne(String userCode);

	/**
	 * @date: 2022/5/24 10:27
	 * @author wubaizhao1
	 * @return
	 */
	UserInfo getLoginOne();

	/**
	 * 根据erp或者其他字段进行模糊搜索
	 * 入参: 登录类型,租户id,用户编码(erp手机号等)
	 * 出参: List<UserInfo>
	 * @date: 2022/5/12 18:20
	 * @author wubaizhao1
	 * @param userInfoDTO
	 * @return
	 */
	List<UserInfo> listByCode(UserInfoDTO userInfoDTO);

	public UserInfo getUser(String erp);

	/**
	 * 批量搜索
	 * @date: 2022/5/16 14:41
	 * @author wubaizhao1
	 * @param ids
	 * @return
	 */
	List<UserInfo> getByIds(List<Long> ids);

	/**
	 * 校验并尝试添加用户
	 * @date: 2022/6/10 11:04
	 * @author wubaizhao1
	 * @param userInfoDTO
	 */
	Long checkAndAdd(UserInfoDTO userInfoDTO);

	public void updateUserDeptInfo();

	/**
	 * @date: 2022/6/14 14:39
	 * @author wubaizhao1
	 * @return
	 */
	Boolean addPin(UserPinDTO userPinDTO);

	/**
	 * 编辑
	 * @date: 2022/6/15 16:27
	 * @author wubaizhao1
	 * @param userPinDTO
	 * @return
	 */
	Boolean editPin(UserPinDTO userPinDTO);

	/**
	 * 检查是否是pin用户的成员
	 * @param userCode
	 * @return
	 */
	Boolean getPin(String userCode);
	/**
	 * 管理列表
	 * @date: 2022/6/15 16:28
	 * @author wubaizhao1
	 * @param userPinDTO
	 * @return
	 */
	Page<UserPinDTO> pageListUserPinDTO(UserPinDTO userPinDTO);

	UserInfo login(LoginDto dto);

	public List<UserInfo> getUsers(List<String> userCodes);

	/**
	 * 根据用户erp查询用户部门信息
	 * @param erp
	 * @return
	 */
	public String getUserDeptNameByErp(String erp);

	List<QueryDeptResultDTO> getAllDept(String parentOrganizationCode);

}

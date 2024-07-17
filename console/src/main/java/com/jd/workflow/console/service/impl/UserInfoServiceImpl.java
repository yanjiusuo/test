package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jd.official.omdm.is.hr.vo.OrganizationVo;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.*;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.dao.mapper.UserInfoMapper;
import com.jd.workflow.console.dto.LoginDto;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.dto.UserPinDTO;
import com.jd.workflow.console.dto.dept.QueryDeptResultDTO;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.ConfigInfoService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.cache.impl.MemoryCache;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Slf4j
@Service
@Data
@ConfigurationProperties(prefix = "user.origin.tenant.admin")
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {
    public static String USER_KEY = "user:";
    static final String ENCODE_SALT = "_034L}eten&^#Htfropy*&%#";
    private final static String JD_COMPANY = "00000000";
    @Resource
    UserInfoMapper userInfoMapper;

    ICache cache;

    @Resource
    IMemberRelationService memberRelationService;

    @Resource
    ConfigInfoService configInfoService;

    @Resource
    private UserHelper userHelper;

    /**
     * 默认的租户管理员
     */
    List<String> userCodeList;
    /**
     * 默认的租户管理员
     */
    List<String> userNameList;

    @Autowired
    ScheduledThreadPoolExecutor defaultScheduledExecutor;

    /**
     * @date: 2022/6/16 10:00
     * @author wubaizhao1
     */
    @Value("${interceptor.loginType}")
    private Integer loginType;

    @PostConstruct
    public void init() {
        cache = new MemoryCache();
        log.info("test UserInfoServiceImpl originTenantAdmin={}", JsonUtils.toJSONString(userCodeList));
        if (EmptyUtil.isEmpty(userCodeList)) {
            return;
        }
        for (int i = 0; i < userCodeList.size(); i++) {
            UserPinDTO userPinDTO = new UserPinDTO();
            userPinDTO.setUserCode(userCodeList.get(i));
            userPinDTO.setUserName(userNameList.get(i));
            userPinDTO.setResourceRole(ResourceTypeEnum.TENANT_ADMIN.getCode());
            //addPin(userPinDTO);
        }
    }

    /**
     * @param userInfoDTO
     * @return
     * @date: 2022/5/11 19:54
     * @author wubaizhao1
     */
    @Override
    public Long add(UserInfoDTO userInfoDTO) {
        log.info("UserInfoServiceImpl.add userInfoDTO={}", JsonUtils.toJSONString(userInfoDTO));
        Guard.notEmpty(userInfoDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(userInfoDTO.getUserName(), "用户名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(userInfoDTO.getUserCode(), "用户编码不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
//		Guard.notEmpty(userInfoDTO.getDept(),"用户部门不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(userInfoDTO.getLoginType(), "登录类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        // 查询是否已存在
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInfo::getYn, DataYnEnum.VALID.getCode())
                //下面 条件构成唯一索引
                .eq(UserInfo::getLoginType, userInfoDTO.getLoginType())
                .eq(UserInfo::getUserCode, userInfoDTO.getUserCode());
        int count = userInfoMapper.selectCount(lqw);
        if (count > 0) {
            throw ServiceException.with(ServiceErrorEnum.DATA_DUPLICATION_ERROR);
        }
        //添加
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoDTO, userInfo);
        userInfo.setYn(DataYnEnum.VALID.getCode());
        int add = userInfoMapper.insert(userInfo);
        return userInfo.getId();
    }

    @Override
    public Long register(UserInfoDTO userInfoDTO) {
        log.info("UserInfoServiceImpl.add userInfoDTO={}", JsonUtils.toJSONString(userInfoDTO));
        Guard.notEmpty(userInfoDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(userInfoDTO.getUserName(), "用户名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(userInfoDTO.getUserCode(), "用户编码不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
//		Guard.notEmpty(userInfoDTO.getDept(),"用户部门不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        userInfoDTO.setLoginType(LoginTypeEnum.SELF.getCode());
        userInfoDTO.setPassword(encodeUserPass(userInfoDTO.getUserCode(), userInfoDTO.getUserName()));
        // 查询是否已存在
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInfo::getYn, DataYnEnum.VALID.getCode())
                //下面 条件构成唯一索引
                .eq(UserInfo::getLoginType, userInfoDTO.getLoginType())
                .eq(UserInfo::getUserCode, userInfoDTO.getUserCode());
        int count = userInfoMapper.selectCount(lqw);
        if (count > 0) {
            throw ServiceException.with(ServiceErrorEnum.DATA_DUPLICATION_ERROR);
        }
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userInfoDTO, userInfo);
        save(userInfo);
        return userInfo.getId();
    }

    /**
     * 入参： id 或者 [唯一索引]用户编码
     * 出参： 用户id
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/5/11 19:54
     * @author wubaizhao1
     */
    @Override
    public Long edit(UserInfoDTO userInfoDTO) {
        if (EmptyUtil.isEmpty(userInfoDTO)) {
            throw ServiceException.with(ServiceErrorEnum.SERVICE_INVALID_PARAMETER);
        }
        boolean idEmpty = EmptyUtil.isAnyEmpty(userInfoDTO.getId());
        boolean uniEmpty = EmptyUtil.isAnyEmpty(userInfoDTO.getUserCode());
        if (idEmpty && uniEmpty) {
            throw ServiceException.with(ServiceErrorEnum.SERVICE_INVALID_PARAMETER);
        }
        if (EmptyUtil.isEmpty(userInfoDTO.getId())) {
            UserInfo userInfo = getOne(userInfoDTO.getUserCode());
            if (userInfo == null) {
                throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
            }
            userInfoDTO.setId(userInfo.getId());
        } else {
            LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper();
            lqw.eq(UserInfo::getId, userInfoDTO.getId())
                    .eq(UserInfo::getYn, DataYnEnum.VALID.getCode());
            UserInfo exist = userInfoMapper.selectOne(lqw);
            if (exist == null) {
                //数据为空无法修改
                throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
            }
        }
        userInfoDTO.setPassword(null);
        UserInfo userInfoForUpdate = new UserInfo();
        BeanUtils.copyProperties(userInfoDTO, userInfoForUpdate);
        userInfoForUpdate.setId(userInfoDTO.getId());
        userInfoMapper.updateById(userInfoForUpdate);
        return userInfoDTO.getId();
    }

    @Override
    public Boolean remove(UserInfoDTO userInfoDTO) {
        if (EmptyUtil.isEmpty(userInfoDTO)) {
            throw ServiceException.with(ServiceErrorEnum.SERVICE_INVALID_PARAMETER);
        }
        boolean idEmpty = EmptyUtil.isAnyEmpty(userInfoDTO.getId());
        boolean uniEmpty = EmptyUtil.isAnyEmpty(userInfoDTO.getUserCode());
        if (idEmpty && uniEmpty) {
            throw ServiceException.with(ServiceErrorEnum.SERVICE_INVALID_PARAMETER);
        }
        if (EmptyUtil.isEmpty(userInfoDTO.getId())) {
            UserInfo userInfo = getOne(userInfoDTO.getUserCode());
            if (userInfo == null) {
                throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
            }
            userInfoDTO.setId(userInfo.getId());
        }
        //逻辑删除
        UserInfo removeEntity = new UserInfo();
        removeEntity.setId(userInfoDTO.getId());
        removeEntity.setYn(DataYnEnum.INVALID.getCode());
        int update = userInfoMapper.updateById(removeEntity);
        return update > 0;
    }

    @Override
    public List<UserInfo> getByIds(List<Long> ids) {
        List<UserInfo> userInfos = userInfoMapper.selectBatchIds(ids);
        return userInfos;
    }

    @Override
    public UserInfo getOne(String userCode) {
        Guard.notEmpty(userCode, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        // 查询是否已存在
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInfo::getYn, DataYnEnum.VALID.getCode())
                .eq(UserInfo::getUserCode, userCode);
        lqw.eq(UserInfo::getLoginType, loginType);
        UserInfo userInfo = userInfoMapper.selectOne(lqw);
        return userInfo;
    }

    /**
     * getLoginOne
     *
     * @return
     * @date: 2022/6/10 14:23
     * @author wubaizhao1
     */
    @Override
    public UserInfo getLoginOne() {
        String userCode = UserSessionLocal.getUser().getUserId();
        return getOne(userCode);
    }

    /**
     * 根据erp或者其他字段进行模糊搜索
     * 入参: 登录类型,用户编码(erp手机号等)
     * 出参: List<UserInfo>
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/5/12 18:20
     * @author wubaizhao1
     */
    @Override
    public List<UserInfo> listByCode(UserInfoDTO userInfoDTO) {
        if (configInfoService.isErpLogin()) {
            return userHelper.findUsers(userInfoDTO.getUserCode());
        }
        Guard.notEmpty(userInfoDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(userInfoDTO.getUserCode(), "用户编码不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //查询条件
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper();
        lqw.eq(UserInfo::getYn, DataYnEnum.VALID.getCode());
        //若不指定登录类型，只输出本次部署相关的列表
        if (EmptyUtil.isNotEmpty(userInfoDTO.getLoginType())) {
            lqw.eq(UserInfo::getLoginType, userInfoDTO.getLoginType());
        } else {
            lqw.eq(EmptyUtil.isNotEmpty(loginType), UserInfo::getLoginType, loginType);
        }
        if (EmptyUtil.isNotEmpty(userInfoDTO.getKey())) {
            lqw.and(x -> x.like(UserInfo::getUserCode, userInfoDTO.getKey()).or().like(UserInfo::getUserName, userInfoDTO.getKey()));
        } else {
            lqw.like(EmptyUtil.isNotEmpty(userInfoDTO.getUserCode()), UserInfo::getUserCode, userInfoDTO.getUserCode());
            lqw.like(EmptyUtil.isNotEmpty(userInfoDTO.getUserName()), UserInfo::getUserName, userInfoDTO.getUserName());
        }
        List<UserInfo> list = userInfoMapper.selectList(lqw);
        return list;
    }

    @Override
    public UserInfo getUser(String erp) {
         UserInfo user = cache.hGet(USER_KEY, erp);
         if(user == null){
             UserInfo remoteUser = getRemoteUser(erp);
             cache.hSet(USER_KEY, erp, remoteUser,5*60);
             user = remoteUser;
         }
         return user;
    }
    public UserInfo getRemoteUser(String erp) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserCode(erp);
        final List<UserInfo> result = listByCode(dto);
        if(result.isEmpty()){
            return null;
        }
        return result.get(0);
    }
    public void updateLastAccessTime(UserInfo userInfo){
        Long currentTime = System.currentTimeMillis();
        Date lastAccessTime = userInfo.getLastAccessTime();
        if(lastAccessTime == null){
            internalUpdateLastAccessTime(userInfo);
        }else if(currentTime - lastAccessTime.getTime() > 60*1000){ // 一分钟最多更新一次
            internalUpdateLastAccessTime(userInfo);
        }
    }
    private void internalUpdateLastAccessTime(UserInfo userInfo){
        if(userInfo.getId() == null) return;
        defaultScheduledExecutor.execute(()->{
            try{
                LambdaUpdateWrapper<UserInfo> luw = new LambdaUpdateWrapper<>();
                luw.set(UserInfo::getLastAccessTime,new Date());
                luw.eq(UserInfo::getId,userInfo.getId());
                update(luw);
            }catch (Exception e){
                log.error("user.err_update_last_access_time",e);
            }
        });
    }
    /**
     * 检查是否存在并且尝试插入
     *
     * @param userInfoDTO
     * @return
     * @date: 2022/6/10 11:01
     * @author wubaizhao1
     */
    @Override
    public Long checkAndAdd(UserInfoDTO userInfoDTO) {
        log.info("UserInfoServiceImpl.add userInfoDTO={}", JsonUtils.toJSONString(userInfoDTO));
        Guard.notEmpty(userInfoDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(userInfoDTO.getUserCode(), "用户编码不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(userInfoDTO.getLoginType(), "登录类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        // 查询是否已存在
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInfo::getYn, DataYnEnum.VALID.getCode())
                //下面 条件构成唯一索引
                .eq(UserInfo::getLoginType, userInfoDTO.getLoginType())
                .eq(UserInfo::getUserCode, userInfoDTO.getUserCode());
        UserInfo info = userInfoMapper.selectOne(lqw);
        //存在
        if (info != null) {
            updateLastAccessTime(info);
            return info.getId();
        }
        //尝试添加
        UserInfo userInfo = new UserInfo();
        userInfo.setCreated(new Date());
        userInfo.setCreator(userInfoDTO.getUserName());
        BeanUtils.copyProperties(userInfoDTO, userInfo);
        userInfo.setYn(DataYnEnum.VALID.getCode());
        try {
            try {
                UserVo userVo = userHelper.getUserBaseInfoByUserName(userInfo.getUserCode());
                if (userVo != null) {
                    userInfo.setDept(userVo.getOrganizationFullName());
                }
            } catch (Exception e) {
                log.error("user.err_set_user_info:erp={}", userInfo.getUserCode(), e);
            }
            if (userInfo.getDept() == null) {
                userInfo.setDept("");
            }
            if (userInfo.getPassword() == null) {
                userInfo.setPassword("");
            }
            if (userInfo.getUserName() == null) {
                userInfo.setUserName(userInfo.getUserCode());
            }
            int add = userInfoMapper.insertUniqueUser(userInfo);
            if (add == 0) {
                userInfo = userInfoMapper.selectOne(lqw);
            } else {
                userInfo = userInfoMapper.selectOne(lqw);
            }
        } catch (Exception e) {
            log.error("user.err_inser_user:userInfo={}", JsonUtils.toJSONString(userInfo), e);
            userInfo = userInfoMapper.selectOne(lqw);
        }
        return userInfo.getId();
    }

    @Override
    public void updateUserDeptInfo() {
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.or().eq(UserInfo::getDept, "暂无").or().isNull(UserInfo::getDept).or().eq(UserInfo::getDept,"");
        List<UserInfo> users = list(lqw);
        for (UserInfo user : users) {
            UserVo vo = userHelper.getUserBaseInfoByUserName(user.getUserCode());
            if (vo != null) {
                log.info("user.update_user_dept:erp={},orgName={}", user.getUserCode(), vo.getOrganizationFullName());
                user.setDept(vo.getOrganizationFullName());
                updateById(user);
            }
        }
    }
    public void updateAllUserDept(){
        int pageNo = 1;
        while (true){
            LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
            Page<UserInfo> page = page(new Page<>(pageNo, 1000));
            pageNo++;
            if (page.getRecords().isEmpty()) {
                break;
            }
            for (UserInfo user : page.getRecords()) {
                UserVo vo = userHelper.getUserBaseInfoByUserName(user.getUserCode());
                if (vo != null && vo.getUserCode() != null) {
                    log.info("user.update_user_dept:erp={},orgName={}", user.getUserCode(), vo.getOrganizationFullName());
                    if(vo.getOrganizationFullName() != null && !vo.getOrganizationFullName().equals(user.getDept())
                     || !Objects.equals(vo.getPositionName(),user.getPositionName())
                    ){
                        user.setDept(vo.getOrganizationFullName());
                        LambdaUpdateWrapper<UserInfo> luw = new LambdaUpdateWrapper<>();
                        luw.set(UserInfo::getDept, vo.getOrganizationFullName());
                        luw.set(UserInfo::getPositionName,vo.getPositionName());
                        luw.eq(UserInfo::getId, user.getId());
                        update(luw);
                    }

                }else{
                    LambdaUpdateWrapper<UserInfo> luw = new LambdaUpdateWrapper<>();
                    luw.set(UserInfo::getDept, null);
                    luw.eq(UserInfo::getId, user.getId());
                    update(luw);
                }
            }
        }
    }

    /**
     * 入参：
     * 用户code 角色（租户管理员 或者成员） 可选：username
     *
     * @return
     * @date: 2022/6/14 14:39
     * @author wubaizhao1
     */
    @Override
    public Boolean addPin(UserPinDTO userPinDTO) {
        Guard.notEmpty(userPinDTO.getUserCode(), "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(userPinDTO.getResourceRole(), "资源角色不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        String userName = EmptyUtil.isEmpty(userPinDTO.getUserName()) ? userPinDTO.getUserCode() : userPinDTO.getUserName();
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserCode(userPinDTO.getUserCode());
        userInfoDTO.setUserName(userName);
        userInfoDTO.setLoginType(LoginTypeEnum.PIN.getCode());
        userInfoDTO.setDept("默认部门");
        Long id = checkAndAdd(userInfoDTO);
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setUserCode(userPinDTO.getUserCode());
        memberRelationDTO.setResourceRole(userPinDTO.getResourceRole());
        memberRelationDTO.setResourceType(ResourceTypeEnum.PIN_MANAGE.getCode());
        memberRelationDTO.setResourceId(id);
        Boolean binding = memberRelationService.checkAndBinding(memberRelationDTO);
        return binding;
    }

    /**
     * 入参：
     * 关系id 用户code 角色（租户管理员 或者成员） 可选：username
     *
     * @return
     * @date: 2022/6/14 14:39
     * @author wubaizhao1
     */
    @Override
    @Transactional
    public Boolean editPin(UserPinDTO userPinDTO) {
        Guard.notEmpty(userPinDTO.getUserCode(), "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(userPinDTO.getResourceRole(), "资源角色不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(userPinDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        MemberRelation memberRelation = memberRelationService.getById(userPinDTO.getId());
        if (memberRelation == null) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
//		UserInfoDTO userInfoDTO=new UserInfoDTO();
        if (!userPinDTO.getUserCode().equals(memberRelation.getUserCode())) {
            UserInfo existUser = getOne(memberRelation.getUserCode());
            UserInfoDTO userInfoDTOForUpdate = new UserInfoDTO();
            userInfoDTOForUpdate.setId(existUser.getId());
            userInfoDTOForUpdate.setUserCode(userPinDTO.getUserCode());
            String userName = EmptyUtil.isEmpty(userPinDTO.getUserName()) ? userPinDTO.getUserCode() : userPinDTO.getUserName();
            userInfoDTOForUpdate.setUserName(userName);
            edit(userInfoDTOForUpdate);
//			userInfoDTO.setUserCode(userPinDTO.getUserCode());
        }
        MemberRelation memberRelationForUpdate = new MemberRelation();
        BeanUtils.copyProperties(userPinDTO, memberRelationForUpdate);
        memberRelationForUpdate.setResourceType(ResourceTypeEnum.PIN_MANAGE.getCode());
        Boolean binding = memberRelationService.updateById(memberRelationForUpdate);
        return binding;
    }

    /**
     * 判断是否存在pin
     *
     * @param userCode
     * @return
     * @date: 2022/6/16 11:01
     * @author wubaizhao1
     */
    @Override
    public Boolean getPin(String userCode) {
        Guard.notEmpty(userCode, "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        UserInfo userInfo = getOne(userCode);
        if (userInfo == null) {
            return false;
        }
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setUserCode(userCode);
        memberRelationDTO.setResourceType(ResourceTypeEnum.PIN_MANAGE.getCode());
        memberRelationDTO.setResourceId(userInfo.getId());
        ResourceRoleEnum role = memberRelationService.getRole(memberRelationDTO);
        if (role == ResourceRoleEnum.TENANT_ADMIN || role == ResourceRoleEnum.MEMBER) {
            return true;
        }
        return false;
    }

    @Override
    public Page<UserPinDTO> pageListUserPinDTO(UserPinDTO userPinDTO) {
        //查询条件
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper();
        lqw.eq(EmptyUtil.isNotEmpty(userPinDTO.getUserCode()), MemberRelation::getUserCode, userPinDTO.getUserCode());
        lqw.eq(EmptyUtil.isNotEmpty(userPinDTO.getResourceRole()), MemberRelation::getResourceRole, userPinDTO.getResourceRole());
        lqw.eq(MemberRelation::getResourceType, ResourceTypeEnum.PIN_MANAGE.getCode());
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        //分页
        Page<MemberRelation> pageParam = null;
        if (userPinDTO.getCurrent() == null || userPinDTO.getSize() == null) {
            pageParam = new Page<>(1, 100000);
        } else {
            pageParam = new Page<>(userPinDTO.getCurrent(), userPinDTO.getSize());
        }
        Page<MemberRelation> page = memberRelationService.page(pageParam, lqw);
        Page<UserPinDTO> result = new Page<>();
        BeanUtils.copyProperties(page, result);
        List<UserPinDTO> userPinDTOList = new ArrayList<>();
        for (MemberRelation record : page.getRecords()) {
            UserPinDTO temp = new UserPinDTO();
            BeanUtils.copyProperties(record, temp);
            UserInfo one = getOne(record.getUserCode());
            if (one != null) {
                temp.setUserName(one.getUserName());
            }
            userPinDTOList.add(temp);
        }
        result.setRecords(userPinDTOList);
        return result;
    }

    public UserInfo login(LoginDto dto) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper();
        String password = encodeUserPass(dto.getUserName(), dto.getPassword());
        queryWrapper.eq(UserInfo::getLoginType, LoginTypeEnum.SELF.getCode());
        queryWrapper.eq(UserInfo::getUserCode, dto.getUserName());
        queryWrapper.eq(UserInfo::getPassword, password);
        UserInfo userInfo = getOne(queryWrapper);
        return userInfo;
    }

    public List<UserInfo> getUsers(List<String> userCodes) {
        if(userCodes.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper();
        wrapper.in(UserInfo::getUserCode, userCodes);
        wrapper.eq(UserInfo::getLoginType, loginType);
        return list(wrapper);
    }

    public String encodeUserPass(String userName, String userPass) {
        return StringHelper.sha256(userName + ENCODE_SALT + userPass);
    }

    /**
     * 根据用户erp查询用户部门信息
     * @param erp
     * @return
     */
    public String getUserDeptNameByErp(String erp) {
        UserVo userVo = userHelper.getUserBaseInfoByUserName(erp);
        if (Objects.nonNull(userVo)) {
            return userVo.getOrganizationFullName();
        }
        return null;
    }


    public static void main(String[] args) {
        UserInfoServiceImpl impl = new UserInfoServiceImpl();
        System.out.println(impl.encodeUserPass("wangjingfang3", "abc321"));
    }


    @Override
    public List<QueryDeptResultDTO> getAllDept(String parentOrganizationCode) {
        List<QueryDeptResultDTO> deptInfos = Lists.newArrayList();
        if(StringUtils.isEmpty(parentOrganizationCode)){
            parentOrganizationCode = JD_COMPANY;
        }
        List<OrganizationVo> depts = userHelper.getDeptByParentCode(parentOrganizationCode);
        transDeptInfoDTo(deptInfos,depts);
        return deptInfos;
    }


    private void transDeptInfoDTo(List<QueryDeptResultDTO> deptInfos, List<OrganizationVo> depts) {
        if(CollectionUtils.isEmpty(depts)){
            return;
        }
        depts.stream().forEach(organization ->{
            QueryDeptResultDTO deptInfo = new QueryDeptResultDTO();
            deptInfo.setOrganizationFullPath(organization.getOrganizationFullPath());
            deptInfo.setOrganizationCode(organization.getOrganizationCode());
            deptInfo.setOrganizationFullname(organization.getOrganizationFullname());
            deptInfo.setOrganizationName(organization.getOrganizationName());
            deptInfo.setOrganizationLevel(organization.getOrganizationLevel());
            deptInfo.setHasChildDepartment(organization.isHasChildDepartment());
            deptInfos.add(deptInfo);
        } );
    }
}

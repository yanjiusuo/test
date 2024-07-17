package com.jd.workflow.console.service.statistics;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeStatus;
import com.jd.workflow.console.base.enums.MeasureDataEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dto.dashboard.HealthItem;
import com.jd.workflow.console.dto.dashboard.InterfaceHealthDTO;
import com.jd.workflow.console.dto.dashboard.UserDashboardDTO;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.impl.MemberRelationServiceImpl;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.service.requirement.InterfaceSpaceService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */
@Slf4j
@Service
public class StatisticsService {

    @Autowired
    private RequirementInfoService requirementInfoService;

    @Autowired
    private IInterfaceManageService interfaceManageService;

    @Autowired
    private IMethodManageService methodManageService;

    @Autowired
    private IAppInfoMembersService appInfoMembersService;

    @Autowired
    private IHttpAuthDetailService httpAuthDetailService;

    @Autowired
    private MemberRelationServiceImpl relationService;

    @Autowired
    private InterfaceSpaceService interfaceSpaceService;

    @Autowired
    private TagService tagService;
    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;

    @Autowired
    private IAppInfoService appInfoService;

    @Autowired
    private IMeasureDataService measureDataService;


    public UserDashboardDTO getPersonInterfaceStatistics(String erp) {
        UserDashboardDTO userDashboardDTO = new UserDashboardDTO();

        LambdaQueryWrapper<AppInfo> appLwq = new LambdaQueryWrapper<>();
        appLwq.inSql(AppInfo::getId, "select mr.app_id from app_info_members mr  JOIN user_info ui ON mr.erp = ui.user_code   WHERE mr.erp =  '" + erp + "'");
        appLwq.eq(BaseEntity::getYn, 1);
        appLwq.ne(AppInfo::getAppCode, "J-dos-japi-demo");
        appLwq.select(AppInfo::getId);
        appLwq.eq(org.apache.commons.lang.StringUtils.isNotBlank(UserSessionLocal.getUser().getTenantId()), AppInfo::getTenantId, UserSessionLocal.getUser().getTenantId());

        appLwq.select(AppInfo::getId);
        List<AppInfo> appInfoList = appInfoService.list(appLwq);
        if (CollectionUtils.isNotEmpty(appInfoList)) {

            List<Long> appIds = appInfoList.stream().map(AppInfo::getId).collect(Collectors.toList());

            //应用数
            userDashboardDTO.setTotalAppCount(appIds.size());

            //初始化
            if (initInterfaceCount(userDashboardDTO, appIds)) {
                return userDashboardDTO;
            }

            //jsf接口鉴权
//            LambdaQueryWrapper<InterfaceManage> jsfAuthManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            jsfAuthManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode()).in(InterfaceManage::getAppId, appIds);
//            jsfAuthManageLambdaQueryWrapper.isNotNull(InterfaceManage::getCjgAppId);
//            Integer jsfAuthCount = interfaceManageService.count(jsfAuthManageLambdaQueryWrapper);
//            userDashboardDTO.setJsfAuthCount(jsfAuthCount);

            //http接口鉴权
//            List<String> appCodes = appInfoMembersList.stream().map(AppInfoMembers::getAppCode).distinct().collect(Collectors.toList());
//            LambdaQueryWrapper<HttpAuthDetail> httpAuthLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            httpAuthLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(HttpAuthDetail::getAppCode, appCodes);
//
//            Integer httpAuthCount = httpAuthDetailService.count(httpAuthLambdaQueryWrapper);
//            userDashboardDTO.setHttpAuthCount(httpAuthCount);


            //接口空间数

            LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
            lqw.eq(BaseEntity::getYn, 1);
//            lqw.eq(RequirementInfo::getType, RequirementTypeEnum.JAPI.getCode());


            LambdaQueryWrapper<MemberRelation> lqwMember = new LambdaQueryWrapper<>();
            lqwMember.eq(MemberRelation::getUserCode, erp);
            lqwMember.eq(MemberRelation::getResourceType, ResourceTypeEnum.PRD_GROUP.getCode());
            lqwMember.eq(MemberRelation::getYn, 1);
            lqwMember.select(MemberRelation::getResourceId);
            List<MemberRelation> memberRelationList = relationService.list(lqwMember);
            if (CollectionUtils.isEmpty(memberRelationList)) {
                userDashboardDTO.setSpaceCount(0);
                userDashboardDTO.setRequireCount(0);
            } else {
                List<Long> spaceIdList = memberRelationList.stream().map(MemberRelation::getResourceId).collect(Collectors.toList());
                lqw.in(RequirementInfo::getId, spaceIdList);
                Integer spaceCount = interfaceSpaceService.count(lqw);
                //空间数
                userDashboardDTO.setSpaceCount(spaceCount);

                lqw = new LambdaQueryWrapper<>();
                lqw.eq(BaseEntity::getYn, 1);
//                lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
                lqw.isNotNull(RequirementInfo::getRelatedRequirementCode);
                lqw.in(RequirementInfo::getId, spaceIdList);

                Integer requireCount = interfaceSpaceService.count(lqw);
                //需求交付总数
                userDashboardDTO.setRequireCount(requireCount);

            }
            LambdaQueryWrapper<TagInfo> tagLqw = new LambdaQueryWrapper<>();
            tagLqw.eq(TagInfo::getYn, 1);
            tagLqw.in(TagInfo::getAppId, appIds);
            //自定义标签数
            Integer tagCount = tagService.count(tagLqw);
            userDashboardDTO.setUserTagCount(tagCount);

        }

        return userDashboardDTO;
    }

    public UserDashboardDTO getDeptInterfaceStatistics(String erp) {
        UserDashboardDTO userDashboardDTO = new UserDashboardDTO();
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(erp);
        if (!userRoleDTO.getDeptLeader() && !userRoleDTO.getJapiDepartment() && !userRoleDTO.getJapiAdmin()) {
            throw new BizException("无权查看");
        }

        StringBuilder deptBuilder = new StringBuilder();
        String[] depts = userRoleDTO.getDept().split("-");
        if (depts.length > 5) {
            deptBuilder.append(depts[0]);
            deptBuilder.append("-");
            deptBuilder.append(depts[1]);
            deptBuilder.append("-");
            deptBuilder.append(depts[2]);
            deptBuilder.append("-");
            deptBuilder.append(depts[3]);
            deptBuilder.append("-");
            deptBuilder.append(depts[4]);
        } else {
            deptBuilder.append(userRoleDTO.getDept());
        }
        LambdaQueryWrapper<AppInfo> appLwq = new LambdaQueryWrapper<>();
        appLwq.inSql(AppInfo::getId, "select mr.app_id from app_info_members mr  JOIN user_info ui ON mr.erp = ui.user_code   WHERE ui.dept LIKE '" + deptBuilder.toString() + "%'");
        appLwq.eq(BaseEntity::getYn, 1);
        appLwq.ne(AppInfo::getAppCode, "J-dos-japi-demo");
        appLwq.select(AppInfo::getId);
        appLwq.eq(org.apache.commons.lang.StringUtils.isNotBlank(UserSessionLocal.getUser().getTenantId()), AppInfo::getTenantId, UserSessionLocal.getUser().getTenantId());

        List<AppInfo> appInfoList = appInfoService.list(appLwq);
        if (CollectionUtils.isNotEmpty(appInfoList)) {
            List<Long> appIds = appInfoList.stream().map(AppInfo::getId).distinct().collect(Collectors.toList());
            //应用数
            userDashboardDTO.setTotalAppCount(appIds.size());

            //初始化
            if (initInterfaceCount(userDashboardDTO, appIds)) {
                return userDashboardDTO;
            }


            //接口空间数

            LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
            lqw.eq(BaseEntity::getYn, 1);
//            lqw.eq(RequirementInfo::getType, RequirementTypeEnum.JAPI.getCode());


            lqw.inSql(RequirementInfo::getId, "select mr.resource_id from member_relation mr  JOIN user_info ui ON mr.user_code = ui.user_code  WHERE mr.yn=1 and mr.resource_type = 20 and ui.dept LIKE '" + deptBuilder.toString() + "%'");
            Integer spaceCount = interfaceSpaceService.count(lqw);
            //空间数
            userDashboardDTO.setSpaceCount(spaceCount);

            lqw = new LambdaQueryWrapper<>();
            lqw.eq(BaseEntity::getYn, 1);
//                lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
            lqw.isNotNull(RequirementInfo::getRelatedRequirementCode);
            lqw.inSql(RequirementInfo::getId, "select mr.resource_id from member_relation mr  JOIN user_info ui ON mr.user_code = ui.user_code  WHERE mr.yn=1 and mr.resource_type = 20 and ui.dept LIKE '" + deptBuilder.toString() + "%'");

            Integer requireCount = interfaceSpaceService.count(lqw);
            //需求交付总数
            userDashboardDTO.setRequireCount(requireCount);


        }

        return userDashboardDTO;
    }

    public UserDashboardDTO getAllInterfaceStatistics(String erp) {
        UserDashboardDTO userDashboardDTO = new UserDashboardDTO();
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(erp);

        if (!userRoleDTO.getJapiAdmin()) {
            throw new BizException("不是管理员，无权查看");
        }

        LambdaQueryWrapper<AppInfo> appLwq = new LambdaQueryWrapper<>();
        appLwq.eq(BaseEntity::getYn, 1);
        appLwq.ne(AppInfo::getAppCode, "J-dos-japi-demo");
        appLwq.eq(org.apache.commons.lang.StringUtils.isNotBlank(UserSessionLocal.getUser().getTenantId()), AppInfo::getTenantId, UserSessionLocal.getUser().getTenantId());

        int appCount = appInfoService.count(appLwq);
        //应用数
        userDashboardDTO.setTotalAppCount(appCount);


        //http接口数

        Date now = new Date();
        Date lastWeekNow = DateUtils.addDays(now, -7);
        LambdaQueryWrapper<MethodManage> methodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode());
        Integer totalHttpMethodCount = methodManageService.count(methodManageLambdaQueryWrapper);
        methodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 0).eq(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode());
        methodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeHttpMethodCount = methodManageService.count(methodManageLambdaQueryWrapper);
        methodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageLambdaQueryWrapper.eq(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode());
        methodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addHttpMethodCount = methodManageService.count(methodManageLambdaQueryWrapper);

        userDashboardDTO.setTotalHttpCount(totalHttpMethodCount);
        userDashboardDTO.setChangeHttpCount(addHttpMethodCount - removeHttpMethodCount);

        //jsf方法数
        LambdaQueryWrapper<MethodManage> jsfmethodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        jsfmethodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(MethodManage::getType, InterfaceTypeEnum.JSF.getCode());
        Integer totalJsfMethodCount = methodManageService.count(jsfmethodManageLambdaQueryWrapper);
        jsfmethodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        jsfmethodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 0).eq(MethodManage::getType, InterfaceTypeEnum.JSF.getCode());
        jsfmethodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeJsfMethodCount = methodManageService.count(jsfmethodManageLambdaQueryWrapper);
        jsfmethodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        jsfmethodManageLambdaQueryWrapper.eq(MethodManage::getType, InterfaceTypeEnum.JSF.getCode());
        jsfmethodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addJsfMethodCount = methodManageService.count(jsfmethodManageLambdaQueryWrapper);

        userDashboardDTO.setTotalJsfMethodCount(totalJsfMethodCount);
        userDashboardDTO.setChangeJsfMethodCount(addJsfMethodCount - removeJsfMethodCount);

        //总接口数
        LambdaQueryWrapper<MethodManage> methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1);
        Integer totalAllCount = methodManageService.count(methodManageAllLambdaQueryWrapper);
        methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 0).ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeAllCount = methodManageService.count(methodManageAllLambdaQueryWrapper);

        methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addAllCount = methodManageService.count(methodManageAllLambdaQueryWrapper);

        methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).gt(MethodManage::getScore, 60);
        Integer score60Count = methodManageService.count(methodManageAllLambdaQueryWrapper);

        userDashboardDTO.setTotalInterfaceCount(totalAllCount);
        userDashboardDTO.setChangeInterfaceCount(addAllCount - removeAllCount);
        userDashboardDTO.setScore60Count(score60Count);

        //jsf接口数
        LambdaQueryWrapper<InterfaceManage> interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        interfaceManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode());
        Integer jsfAllCount = interfaceManageService.count(interfaceManageLambdaQueryWrapper);

        interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        interfaceManageLambdaQueryWrapper.eq(BaseEntity::getYn, 0).eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode()).ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeJsfAllCount = interfaceManageService.count(interfaceManageLambdaQueryWrapper);

        interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        interfaceManageLambdaQueryWrapper.eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode()).ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addJsfAllCount = interfaceManageService.count(interfaceManageLambdaQueryWrapper);

        userDashboardDTO.setTotalJsfCount(jsfAllCount);
        userDashboardDTO.setChangeJsfCount(addJsfAllCount - removeJsfAllCount);


        //接口空间数

        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BaseEntity::getYn, 1);
        Integer spaceCount = interfaceSpaceService.count(lqw);
        //空间数
        userDashboardDTO.setSpaceCount(spaceCount);

        lqw = new LambdaQueryWrapper<>();
        lqw.eq(BaseEntity::getYn, 1);

        lqw.isNotNull(RequirementInfo::getRelatedRequirementCode);

        Integer requireCount = interfaceSpaceService.count(lqw);
        //需求交付总数
        userDashboardDTO.setRequireCount(requireCount);


        LambdaQueryWrapper<MeasureData> lqwMeasureData = new LambdaQueryWrapper<>();
        lqwMeasureData.eq(MeasureData::getType, MeasureDataEnum.INTERFACE_DOC_DETAIL.getCode());
        Integer viewCount = measureDataService.count(lqwMeasureData);
        userDashboardDTO.setInterfaceViewCount(viewCount);

        return userDashboardDTO;
    }

    private boolean initInterfaceCount(UserDashboardDTO userDashboardDTO, List<Long> appIds) {
        LambdaQueryWrapper<AppInfo> appLwq = new LambdaQueryWrapper<>();
        appLwq.eq(BaseEntity::getYn, 1).in(AppInfo::getId, appIds);
        appLwq.select(AppInfo::getId);
        List<AppInfo> appInfoList = appInfoService.list(appLwq);
        appIds = appInfoList.stream().map(AppInfo::getId).collect(Collectors.toList());
        LambdaQueryWrapper<InterfaceManage> interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        interfaceManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(InterfaceManage::getAppId, appIds).select(InterfaceManage::getId);
        List<InterfaceManage> interfaceManageList = interfaceManageService.list(interfaceManageLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(interfaceManageList)) {
            return true;
        }
        List<Long> interfaceIds = interfaceManageList.stream().map(InterfaceManage::getId).distinct().collect(Collectors.toList());

        Date now = new Date();
        Date lastWeekNow = DateUtils.addDays(now, -7);

        //http接口数
        LambdaQueryWrapper<MethodManage> methodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode()).in(MethodManage::getInterfaceId, interfaceIds);
        Integer totalHttpMethodCount = methodManageService.count(methodManageLambdaQueryWrapper);
        methodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 0).eq(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode()).in(MethodManage::getInterfaceId, interfaceIds);
        methodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeHttpMethodCount = methodManageService.count(methodManageLambdaQueryWrapper);
        methodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageLambdaQueryWrapper.eq(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode()).in(MethodManage::getInterfaceId, interfaceIds);
        methodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addHttpMethodCount = methodManageService.count(methodManageLambdaQueryWrapper);

        userDashboardDTO.setTotalHttpCount(totalHttpMethodCount);
        userDashboardDTO.setChangeHttpCount(addHttpMethodCount - removeHttpMethodCount);

        //jsf方法数
        LambdaQueryWrapper<MethodManage> jsfmethodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        jsfmethodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(MethodManage::getType, InterfaceTypeEnum.JSF.getCode()).in(MethodManage::getInterfaceId, interfaceIds);
        Integer totalJsfMethodCount = methodManageService.count(jsfmethodManageLambdaQueryWrapper);
        jsfmethodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        jsfmethodManageLambdaQueryWrapper.eq(BaseEntity::getYn, 0).eq(MethodManage::getType, InterfaceTypeEnum.JSF.getCode()).in(MethodManage::getInterfaceId, interfaceIds);
        jsfmethodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeJsfMethodCount = methodManageService.count(jsfmethodManageLambdaQueryWrapper);
        jsfmethodManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        jsfmethodManageLambdaQueryWrapper.eq(MethodManage::getType, InterfaceTypeEnum.JSF.getCode()).in(MethodManage::getInterfaceId, interfaceIds);
        jsfmethodManageLambdaQueryWrapper.ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addJsfMethodCount = methodManageService.count(jsfmethodManageLambdaQueryWrapper);

        userDashboardDTO.setTotalJsfMethodCount(totalJsfMethodCount);
        userDashboardDTO.setChangeJsfMethodCount(addJsfMethodCount - removeJsfMethodCount);

        //总接口数
        LambdaQueryWrapper<MethodManage> methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds);
        Integer totalAllCount = methodManageService.count(methodManageAllLambdaQueryWrapper);
        methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 0).in(MethodManage::getInterfaceId, interfaceIds).ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeAllCount = methodManageService.count(methodManageAllLambdaQueryWrapper);

        methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.in(MethodManage::getInterfaceId, interfaceIds).ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addAllCount = methodManageService.count(methodManageAllLambdaQueryWrapper);

        methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).gt(MethodManage::getScore, 60).in(MethodManage::getInterfaceId, interfaceIds);
        Integer score60Count = methodManageService.count(methodManageAllLambdaQueryWrapper);

        userDashboardDTO.setTotalInterfaceCount(totalAllCount);
        userDashboardDTO.setChangeInterfaceCount(addAllCount - removeAllCount);
        userDashboardDTO.setScore60Count(score60Count);

        //jsf接口数
        interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        interfaceManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode()).in(InterfaceManage::getAppId, appIds);
        Integer jsfAllCount = interfaceManageService.count(interfaceManageLambdaQueryWrapper);

        interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        interfaceManageLambdaQueryWrapper.eq(BaseEntity::getYn, 0).eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode()).in(InterfaceManage::getAppId, appIds).ge(BaseEntityNoDelLogic::getModified, lastWeekNow);
        Integer removeJsfAllCount = interfaceManageService.count(interfaceManageLambdaQueryWrapper);

        interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        interfaceManageLambdaQueryWrapper.eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode()).in(InterfaceManage::getAppId, appIds).ge(BaseEntityNoDelLogic::getCreated, lastWeekNow);
        Integer addJsfAllCount = interfaceManageService.count(interfaceManageLambdaQueryWrapper);

        userDashboardDTO.setTotalJsfCount(jsfAllCount);
        userDashboardDTO.setChangeJsfCount(addJsfAllCount - removeJsfAllCount);


        LambdaQueryWrapper<MeasureData> lqwMeasureData = new LambdaQueryWrapper<>();
        lqwMeasureData.eq(MeasureData::getType, MeasureDataEnum.INTERFACE_DOC_DETAIL.getCode());
        lqwMeasureData.inSql(MeasureData::getNote, "select id from method_manage where interface_id in (" + StringUtils.join(interfaceIds, ",") + ")");
        Integer viewCount = measureDataService.count(lqwMeasureData);
        userDashboardDTO.setInterfaceViewCount(viewCount);

        return false;
    }

    public InterfaceHealthDTO getHeathStatistics(String erp) {
        InterfaceHealthDTO interfaceHealthDTO = new InterfaceHealthDTO();
        LambdaQueryWrapper<AppInfoMembers> memberLwq = new LambdaQueryWrapper<>();
        memberLwq.eq(AppInfoMembers::getErp, erp).eq(BaseEntity::getYn, 1);
        memberLwq.select(AppInfoMembers::getAppId, AppInfoMembers::getAppCode);
        List<AppInfoMembers> appInfoMembersList = appInfoMembersService.list(memberLwq);
        interfaceHealthDTO.setHealthItemList(Lists.newArrayList());
        if (CollectionUtils.isNotEmpty(appInfoMembersList)) {
            List<Long> appIds = appInfoMembersList.stream().map(AppInfoMembers::getAppId).distinct().collect(Collectors.toList());
            LambdaQueryWrapper<InterfaceManage> interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            interfaceManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(InterfaceManage::getAppId, appIds).select(InterfaceManage::getId);
            List<InterfaceManage> interfaceManageList = interfaceManageService.list(interfaceManageLambdaQueryWrapper);
            if (CollectionUtils.isEmpty(interfaceManageList)) {
                return interfaceHealthDTO;
            }
            List<Long> interfaceIds = interfaceManageList.stream().map(InterfaceManage::getId).distinct().collect(Collectors.toList());

            //avg score
            QueryWrapper<MethodManage> methodManageAllQueryWrapper = new QueryWrapper<>();
            methodManageAllQueryWrapper.select("avg(score) as avgScore");
            methodManageAllQueryWrapper.lambda();
            LambdaQueryWrapper<MethodManage> methodManageAllLambdaQueryWrapper = methodManageAllQueryWrapper.lambda();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds);

            List<Map<String, Object>> scoreList = methodManageService.getBaseMapper().selectMaps(methodManageAllLambdaQueryWrapper);
            if (CollectionUtils.isNotEmpty(scoreList)) {
                BigDecimal avgScore = new BigDecimal(scoreList.get(0).get("avgScore").toString());
                interfaceHealthDTO.setAvgCount(avgScore.setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            //count
            methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds);
            methodManageAllLambdaQueryWrapper.select(MethodManage::getScore);
            Integer count = methodManageService.count(methodManageAllLambdaQueryWrapper);


            //count20
            methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds).between(MethodManage::getScore, 0, 20);
            methodManageAllLambdaQueryWrapper.select(MethodManage::getScore);
            Integer count20 = methodManageService.count(methodManageAllLambdaQueryWrapper);
            BigDecimal count20Decimal = new BigDecimal(count20);
            //count40
            methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds).between(MethodManage::getScore, 20, 40);
            methodManageAllLambdaQueryWrapper.select(MethodManage::getScore);
            Integer count40 = methodManageService.count(methodManageAllLambdaQueryWrapper);
            BigDecimal count40Decimal = new BigDecimal(count40);
            //count60
            methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds).between(MethodManage::getScore, 40, 60);
            methodManageAllLambdaQueryWrapper.select(MethodManage::getScore);
            Integer count60 = methodManageService.count(methodManageAllLambdaQueryWrapper);
            BigDecimal count60Decimal = new BigDecimal(count60);

            //count80
            methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds).between(MethodManage::getScore, 60, 80);
            methodManageAllLambdaQueryWrapper.select(MethodManage::getScore);
            Integer count80 = methodManageService.count(methodManageAllLambdaQueryWrapper);
            BigDecimal count80Decimal = new BigDecimal(count80);

            //count100
            methodManageAllLambdaQueryWrapper = new LambdaQueryWrapper();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds).between(MethodManage::getScore, 80, 100);
            methodManageAllLambdaQueryWrapper.select(MethodManage::getScore);
            Integer count100 = methodManageService.count(methodManageAllLambdaQueryWrapper);
            BigDecimal count100Decimal = new BigDecimal(count100);

            BigDecimal countDecimal = new BigDecimal(count20 + count40 + count60 + count80 + count100);

            HealthItem healthItem20 = new HealthItem();
            healthItem20.setName("0-20分");
            healthItem20.setValue(count20Decimal.divide(countDecimal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));

            HealthItem healthItem40 = new HealthItem();
            healthItem40.setName("21-40分");
            healthItem40.setValue(count40Decimal.divide(countDecimal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));

            HealthItem healthItem60 = new HealthItem();
            healthItem60.setName("41-60分");
            healthItem60.setValue(count60Decimal.divide(countDecimal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));

            HealthItem healthItem80 = new HealthItem();
            healthItem80.setName("61-80分");
            healthItem80.setValue(count80Decimal.divide(countDecimal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));

            HealthItem healthItem100 = new HealthItem();
            healthItem100.setName("81-100分");
            healthItem100.setValue(count100Decimal.divide(countDecimal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
            interfaceHealthDTO.getHealthItemList().add(healthItem20);
            interfaceHealthDTO.getHealthItemList().add(healthItem40);
            interfaceHealthDTO.getHealthItemList().add(healthItem60);
            interfaceHealthDTO.getHealthItemList().add(healthItem80);
            interfaceHealthDTO.getHealthItemList().add(healthItem100);


        }

        return interfaceHealthDTO;
    }


    public InterfaceHealthDTO getMethodStatusStatistics(String erp) {
        InterfaceHealthDTO interfaceHealthDTO = new InterfaceHealthDTO();
        LambdaQueryWrapper<AppInfoMembers> memberLwq = new LambdaQueryWrapper<>();
        memberLwq.eq(AppInfoMembers::getErp, erp).eq(BaseEntity::getYn, 1);
        memberLwq.select(AppInfoMembers::getAppId, AppInfoMembers::getAppCode);
        List<AppInfoMembers> appInfoMembersList = appInfoMembersService.list(memberLwq);
        interfaceHealthDTO.setHealthItemList(Lists.newArrayList());
        if (CollectionUtils.isNotEmpty(appInfoMembersList)) {
            List<Long> appIds = appInfoMembersList.stream().map(AppInfoMembers::getAppId).distinct().collect(Collectors.toList());
            LambdaQueryWrapper<InterfaceManage> interfaceManageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            interfaceManageLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(InterfaceManage::getAppId, appIds).select(InterfaceManage::getId);
            List<InterfaceManage> interfaceManageList = interfaceManageService.list(interfaceManageLambdaQueryWrapper);
            if (CollectionUtils.isEmpty(interfaceManageList)) {
                return interfaceHealthDTO;
            }
            List<Long> interfaceIds = interfaceManageList.stream().map(InterfaceManage::getId).distinct().collect(Collectors.toList());

            //avg score
            QueryWrapper<MethodManage> methodManageAllQueryWrapper = new QueryWrapper<>();
            methodManageAllQueryWrapper.select("count(status) as statusCount,status");
            methodManageAllQueryWrapper.lambda();
            LambdaQueryWrapper<MethodManage> methodManageAllLambdaQueryWrapper = methodManageAllQueryWrapper.lambda();
            methodManageAllLambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(MethodManage::getInterfaceId, interfaceIds);
            methodManageAllLambdaQueryWrapper.groupBy(MethodManage::getStatus);
            List<Map<String, Object>> list = methodManageService.listMaps(methodManageAllLambdaQueryWrapper);
            if (CollectionUtils.isEmpty(list)) {
                return interfaceHealthDTO;
            }
            BigDecimal total = new BigDecimal(0);
            for (Map<String, Object> stringObjectMap : list) {
                if (stringObjectMap.containsKey("status")) {
                    InterfaceTypeStatus interfaceTypeStatus = InterfaceTypeStatus.getByCode((Integer) stringObjectMap.get("status"));
                    if (Objects.nonNull(interfaceTypeStatus)) {
                        total = total.add(new BigDecimal(stringObjectMap.get("statusCount").toString()));
                    }
                }
            }
            if (total.equals(BigDecimal.ZERO)) {
                return interfaceHealthDTO;
            }
            for (Map<String, Object> stringObjectMap : list) {
                if (stringObjectMap.containsKey("status")) {
                    InterfaceTypeStatus interfaceTypeStatus = InterfaceTypeStatus.getByCode((Integer) stringObjectMap.get("status"));
                    if (Objects.nonNull(interfaceTypeStatus)) {
                        HealthItem healthItem = new HealthItem();

                        healthItem.setName(interfaceTypeStatus.getDesc());
                        BigDecimal count = new BigDecimal(stringObjectMap.get("statusCount").toString());
                        healthItem.setValue(count.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
                        interfaceHealthDTO.getHealthItemList().add(healthItem);
                    }
                }
            }

        }

        return interfaceHealthDTO;
    }
}

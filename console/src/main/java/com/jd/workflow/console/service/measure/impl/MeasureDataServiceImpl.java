package com.jd.workflow.console.service.measure.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.MeasureDataEnum;
import com.jd.workflow.console.base.enums.RequirementStatusEnum;
import com.jd.workflow.console.dao.mapper.MeasureDataMapper;
import com.jd.workflow.console.dto.doc.GroupHttpData;
import com.jd.workflow.console.dto.measure.RequirementMeasureDataDTO;
import com.jd.workflow.console.dto.measure.UserMeasureDataDTO;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.entity.plugin.HotswapDeployInfo;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.ProxyDebuggerRegistryService;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.service.plugin.HotswapDeployInfoService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yza
 * @description
 * @date 2024/1/12
 */
@Slf4j
@Service
public class MeasureDataServiceImpl extends ServiceImpl<MeasureDataMapper, MeasureData> implements IMeasureDataService {

    @Autowired
    private UserHelper userHelper;

    /**
     *
     */
    private static final String SEPARATOR = "-";

    @Autowired
    private RequirementInfoService requirementInfoService;

    @Autowired
    private HotswapDeployInfoService hotswapDeployInfoService;

    @Autowired
    private ProxyDebuggerRegistryService proxyDebuggerRegistryService;

    @Autowired
    private IMemberRelationService memberRelationService;

    /**
     *
     */
    private static final int BATCH_SIZE = 500;

    /**
     * 【指标度量】接口上报
     *
     * @param interfaceType
     * @param group2MethodManage
     */
    @Override
    public void saveReportDataLog(Integer interfaceType, List<GroupHttpData<MethodManage>> group2MethodManage, String erp) {
        try {
            if (CollectionUtils.isEmpty(group2MethodManage)) {
                return;
            }
            int num = group2MethodManage.stream().mapToInt(e -> e.getHttpData().size()).sum();
            if (num == 0) {
                return;
            }
            MeasureData measureData = new MeasureData();
            this.buildMeasureData(measureData);
            measureData.setNum(num);
            String department = "";
            UserVo userVo = userHelper.getUserBaseInfoByUserName(erp);
            if (Objects.nonNull(userVo)) {
                department = userVo.getOrganizationFullName();
            }
            this.parseDepartment(department, measureData);
            measureData.setDepartment(department);
            measureData.setErp(erp);
            measureData.setCreator(erp);
            measureData.setModifier(erp);
            if (InterfaceTypeEnum.HTTP.getCode().equals(interfaceType)) {
                measureData.setType(MeasureDataEnum.REPORT_HTTP.getCode());
            } else if (InterfaceTypeEnum.JSF.getCode().equals(interfaceType)) {
                measureData.setType(MeasureDataEnum.REPORT_JSF.getCode());
            }
            this.save(measureData);
        } catch (Exception e) {
            log.error("saveReportDataLog error", e);
        }
    }

    /**
     * 【指标度量】快捷调用
     *
     * @param type
     * @param debugLog
     */
    @Override
    public void saveQuickCallLog(Integer type, FlowDebugLog debugLog) {
        try {
            if (Objects.isNull(debugLog)) {
                return;
            }
            MeasureData measureData = new MeasureData();
            this.buildMeasureData(measureData);
            measureData.setType(type);
            measureData.setStatus(debugLog.getSuccess());
            this.save(measureData);
        } catch (Exception e) {
            log.error("saveQuickCallLog error", e);
        }
    }

    /**
     * 【指标度量】接口详情
     *
     * @param methodId
     */
    @Override
    public void saveInterfaceDetailLog(String methodId) {
        try {
            MeasureData measureData = new MeasureData();
            this.buildMeasureData(measureData);
            measureData.setType(MeasureDataEnum.INTERFACE_DOC_DETAIL.getCode());
            measureData.setNote(methodId);
            this.save(measureData);
        } catch (Exception e) {
            log.error("saveInterfaceDetailLog error", e);
        }
    }

    /**
     * 【指标度量】mock模版
     *
     * @param type
     * @param methodId
     */
    @Override
    public void saveMockTemplateLog(Integer type, String methodId) {
        try {
            MeasureData measureData = new MeasureData();
            this.buildMeasureData(measureData);
            measureData.setType(type);
            measureData.setNote(methodId);
            this.save(measureData);
        } catch (Exception e) {
            log.error("saveMockTemplateLog error", e);
        }
    }

    /**
     * 【指标度量】快捷调用一键mock
     *
     * @param type
     * @param methodId
     */
    @Override
    public void saveQuickCallMockTempLog(Integer type, String methodId) {
        try {
            MeasureData measureData = new MeasureData();
            this.buildMeasureData(measureData);
            measureData.setType(type);
            measureData.setNote(methodId);
            this.save(measureData);
        } catch (Exception e) {
            log.error("saveQuickCallMockTempLog error", e);
        }
    }

    /**
     * 【指标度量】用户明细
     *
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    @Override
    public List<UserMeasureDataDTO> queryUserMeasureData(String department, String timeStart, String timeEnd, String erp) {
        if (StringUtils.isEmpty(timeStart)) {
            // 本周第一天
            timeStart = StringHelper.formatDate(DateUtil.beginOfWeek(new Date()), "yyyy-MM-dd HH:mm:ss");
        }
        if (StringUtils.isEmpty(timeEnd)) {
            // 当前
            timeEnd = StringHelper.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
        }
        List<UserMeasureDataDTO> list = new ArrayList<>();
        // 度量数据表获取用户
        LambdaQueryWrapper<MeasureData> lqwMeasure = new LambdaQueryWrapper<>();
        lqwMeasure.like(StringUtils.isNotEmpty(department), MeasureData::getDepartment, department)
                .gt(MeasureData::getCreated, timeStart)
                .lt(MeasureData::getCreated, timeEnd)
                .like(StringUtils.isNotEmpty(erp), MeasureData::getErp, erp)
                .select(MeasureData::getErp, MeasureData::getStatus, MeasureData::getType);
        List<MeasureData> measureDataList = this.list(lqwMeasure);
        Set<String> measureUserSet = measureDataList.stream().map(MeasureData::getErp)
                .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());

        // 需求空间表获取用户
        LambdaQueryWrapper<RequirementInfo> lqwRequirement = new LambdaQueryWrapper<>();
        lqwRequirement.gt(RequirementInfo::getCreated, timeStart)
                .lt(RequirementInfo::getCreated, timeEnd)
                .eq(RequirementInfo::getYn, 1)
                .select(RequirementInfo::getCreator, RequirementInfo::getRelatedRequirementCode, RequirementInfo::getId);
        List<RequirementInfo> requirementInfoList = requirementInfoService.list(lqwRequirement);
        // 空间创建人为空时,成员表获取负责人
        List<Long> requirementInfoIds = requirementInfoList.stream().filter(e -> StringUtils.isEmpty(e.getCreator()))
                .map(RequirementInfo::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(requirementInfoIds)) {
            LambdaQueryWrapper<MemberRelation> lqwMember = new LambdaQueryWrapper<>();
            lqwMember.in(MemberRelation::getResourceId, requirementInfoIds)
                    .eq(MemberRelation::getResourceRole, 2)
                    .eq(MemberRelation::getYn, 1)
                    .select(MemberRelation::getResourceId, MemberRelation::getUserCode);
            List<MemberRelation> memberRelationList = memberRelationService.list(lqwMember);
            Map<Long, String> memberIdAndOwnerMap = memberRelationList.stream().filter(e->StringUtils.isNotEmpty(e.getUserCode())).collect(Collectors.toMap(MemberRelation::getResourceId, MemberRelation::getUserCode));
            for (RequirementInfo info : requirementInfoList) {
                if (StringUtils.isEmpty(info.getCreator()) && StringUtils.isNotEmpty(memberIdAndOwnerMap.get(info.getId()))) {
                    info.setCreator(memberIdAndOwnerMap.get(info.getId()));
                }
            }
        }

        Set<String> requirementUserSet;
        if (StringUtils.isNotEmpty(erp)) {
            requirementUserSet = requirementInfoList.stream().map(RequirementInfo::getCreator)
                    .filter(e -> e.contains(erp)).collect(Collectors.toSet());
        } else {
            requirementUserSet = requirementInfoList.stream().map(RequirementInfo::getCreator)
                    .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        }

        // 热更新表获取用户
        LambdaQueryWrapper<HotswapDeployInfo> lqwHotswap = new LambdaQueryWrapper<>();
        lqwHotswap.gt(HotswapDeployInfo::getCreated, timeStart)
                .lt(HotswapDeployInfo::getCreated, timeEnd)
                .like(StringUtils.isNotEmpty(erp), HotswapDeployInfo::getDeployErp, erp)
                .select(HotswapDeployInfo::getDeployErp, HotswapDeployInfo::getSucceed, HotswapDeployInfo::getRemoteDeployCostTime);
        List<HotswapDeployInfo> hotswapDeployInfoList = hotswapDeployInfoService.list(lqwHotswap);
        Set<String> hotswapUserSet = hotswapDeployInfoList.stream().filter(Objects::nonNull).map(HotswapDeployInfo::getDeployErp)
                .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());

        // 远程调试表获取用户
        LambdaQueryWrapper<ProxyDebuggerRegistry> lqwProxy = new LambdaQueryWrapper<>();
        lqwProxy.gt(ProxyDebuggerRegistry::getCreated, timeStart)
                .lt(ProxyDebuggerRegistry::getCreated, timeEnd)
                .like(StringUtils.isNotEmpty(erp), ProxyDebuggerRegistry::getErp, erp)
                .select(ProxyDebuggerRegistry::getErp);
        List<ProxyDebuggerRegistry> proxyDebuggerList = proxyDebuggerRegistryService.list(lqwProxy);
        Set<String> proxyDebuggerUserSet = proxyDebuggerList.stream().map(ProxyDebuggerRegistry::getErp)
                .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());

        Set<String> userSet = new HashSet<>();
        userSet.addAll(measureUserSet);
        userSet.addAll(requirementUserSet);
        userSet.addAll(hotswapUserSet);
        userSet.addAll(proxyDebuggerUserSet);

        for (String name : userSet) {
            String departmentName = "";
            UserVo userVo = userHelper.getUserBaseInfoByUserName(name);
            if (Objects.nonNull(userVo) && StringUtils.isNotEmpty(userVo.getOrganizationFullName())) {
                departmentName = userVo.getOrganizationFullName();
                // 根据部门过滤
                if (StringUtils.isNotEmpty(department) && !departmentName.contains(department)) {
                    continue;
                }
                UserMeasureDataDTO userMeasureDataDTO = new UserMeasureDataDTO();
                userMeasureDataDTO.setErp(name);
                userMeasureDataDTO.setDepartment(departmentName);
                this.buildMeasureDataInfo(measureDataList, userMeasureDataDTO);
                this.buildRequirementInfo(requirementInfoList, userMeasureDataDTO);
                this.buildHotswapInfo(hotswapDeployInfoList, userMeasureDataDTO);
                this.buildProxyDebuggerInfo(proxyDebuggerList, userMeasureDataDTO);
                list.add(userMeasureDataDTO);
            }
        }
        return list;
    }

    /**
     * 【指标度量】空间明细
     *
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    @Override
    public List<RequirementMeasureDataDTO> queryRequirementMeasureData(String department, String timeStart, String timeEnd,
                                                                       String requirementName, String requirementCode, String creator) {
        List<RequirementMeasureDataDTO> list = new ArrayList<>();
        if (StringUtils.isEmpty(timeStart)) {
            // 本月第一天
            timeStart = StringHelper.formatDate(DateUtil.beginOfMonth(new Date()), "yyyy-MM-dd HH:mm:ss");
        }
        if (StringUtils.isEmpty(timeEnd)) {
            // 本月最后一条
            timeEnd = StringHelper.formatDate(DateUtil.endOfMonth(new Date()), "yyyy-MM-dd HH:mm:ss");
        }
        // 需求空间表
        LambdaQueryWrapper<RequirementInfo> lqwRequirement = new LambdaQueryWrapper<>();
        lqwRequirement.gt(RequirementInfo::getCreated, timeStart)
                .lt(RequirementInfo::getCreated, timeEnd)
                .like(StringUtils.isNotEmpty(requirementName), RequirementInfo::getName, requirementName)
                .like(StringUtils.isNotEmpty(requirementCode), RequirementInfo::getRelatedRequirementCode, requirementCode)
                .eq(RequirementInfo::getYn, DataYnEnum.VALID.getCode());
        List<RequirementInfo> requirementInfoList = requirementInfoService.list(lqwRequirement);
        // 从成员表获取负责人信息填充创建人为空的数据
        List<Long> requirementInfoIds = requirementInfoList.stream().filter(e -> StringUtils.isEmpty(e.getCreator()))
                .map(RequirementInfo::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(requirementInfoIds)) {
            LambdaQueryWrapper<MemberRelation> lqwMember = new LambdaQueryWrapper<>();
            lqwMember.in(MemberRelation::getResourceId, requirementInfoIds)
                    .eq(MemberRelation::getResourceRole, 2)
                    .eq(MemberRelation::getYn, 1)
                    .select(MemberRelation::getResourceId, MemberRelation::getUserCode);
            List<MemberRelation> memberRelationList = memberRelationService.list(lqwMember);
            Map<Long, String> memberIdAndOwnerMap = memberRelationList.stream().filter(e->StringUtils.isNotEmpty(e.getUserCode())).collect(Collectors.toMap(MemberRelation::getResourceId, MemberRelation::getUserCode));
            for (RequirementInfo info : requirementInfoList) {
                if (StringUtils.isEmpty(info.getCreator()) && StringUtils.isNotEmpty(memberIdAndOwnerMap.get(info.getId()))) {
                    info.setCreator(memberIdAndOwnerMap.get(info.getId()));
                }
            }
        }
        if (StringUtils.isNotEmpty(creator)) {
            requirementInfoList = requirementInfoList.stream().filter(e -> StringUtils.isNotEmpty(e.getCreator()))
                    .filter(e -> e.getCreator().contains(creator))
                    .collect(Collectors.toList());
        }

        for (RequirementInfo requirementInfo : requirementInfoList) {
            RequirementMeasureDataDTO requirementMeasureDataDTO = new RequirementMeasureDataDTO();
            BeanUtils.copyProperties(requirementInfo, requirementMeasureDataDTO);
            requirementMeasureDataDTO.setStatusName(RequirementStatusEnum.getStatusDesc(requirementInfo.getStatus()));
            // 查询部门信息
            UserVo userVo = userHelper.getUserBaseInfoByUserName(requirementInfo.getCreator());
            if (Objects.nonNull(userVo)) {
                String departmentName = userVo.getOrganizationFullName();
                requirementMeasureDataDTO.setDepartment(departmentName);
                // 根据部门过滤
                if (StringUtils.isNotEmpty(department)) {
                    if (StringUtils.isEmpty(departmentName) || !departmentName.contains(department)) {
                        continue;
                    }
                }
            }
            list.add(requirementMeasureDataDTO);
        }
        return list;
    }

    @Override
    public List<MeasureData> queryDeptInfo(String dept, Integer count) {

        LambdaQueryWrapper<MeasureData> lqwMeasureData = new QueryWrapper<MeasureData>().select("distinct type,num,status,note,erp,department,dep0,dep1,dep2,dep3,yn,creator,modifier,created,modified").lambda();
        lqwMeasureData.like(MeasureData::getDepartment, dept);
        lqwMeasureData.orderByDesc(BaseEntityNoDelLogic::getCreated);
        lqwMeasureData.last("limit " + count);
        return list(lqwMeasureData);


    }

    @Override
    public List<MeasureData> queryUserView(String erp, Integer count) {
        LambdaQueryWrapper<MeasureData> lqwMeasureData = new QueryWrapper<MeasureData>().select("distinct note").lambda();
        lqwMeasureData.eq(MeasureData::getErp, erp);
        lqwMeasureData.eq(MeasureData::getType, MeasureDataEnum.INTERFACE_DOC_DETAIL.getCode());
        lqwMeasureData.orderByDesc(BaseEntityNoDelLogic::getCreated);
        lqwMeasureData.last("limit " + count);
        return list(lqwMeasureData);
    }

    /**
     * 组装度量默认数据
     *
     * @param measureData
     */
    private void buildMeasureData(MeasureData measureData) {
        String erp = UserSessionLocal.getUser().getUserId();
        measureData.setErp(erp);
        measureData.setCreator(erp);
        measureData.setModifier(erp);
        String department = "";
        UserVo userVo = userHelper.getUserBaseInfoByUserName(erp);
        if (Objects.nonNull(userVo)) {
            department = userVo.getOrganizationFullName();
        }
        this.parseDepartment(department, measureData);
        measureData.setDepartment(department);
        measureData.setCreated(new Date());
        measureData.setModified(new Date());
        measureData.setNum(1);
        measureData.setStatus(1);
    }

    /**
     * 解析部门
     *
     * @param department
     * @param measureData
     */
    public void parseDepartment(String department, MeasureData measureData) {
        if (StringUtils.isNotEmpty(department)) {
            String[] depArr = department.split(SEPARATOR);
            if (depArr.length > 4) {
                measureData.setDep0(depArr[1]);
                measureData.setDep1(depArr[2]);
                measureData.setDep2(depArr[3]);
                measureData.setDep3(depArr[4]);
            } else if (depArr.length > 3) {
                measureData.setDep0(depArr[1]);
                measureData.setDep1(depArr[2]);
                measureData.setDep2(depArr[3]);
            } else if (depArr.length > 2) {
                measureData.setDep0(depArr[1]);
                measureData.setDep1(depArr[2]);
            } else if (depArr.length > 1) {
                measureData.setDep0(depArr[1]);
            }
        }
    }

    /**
     * 组装度量相关信息
     *
     * @param measureDataList
     * @param userMeasureDataDTO
     */
    private void buildMeasureDataInfo(List<MeasureData> measureDataList, UserMeasureDataDTO userMeasureDataDTO) {
        try {
            String erp = userMeasureDataDTO.getErp();
            // 接口上报次数
            long interfaceReportNum = measureDataList.stream().filter(e -> erp.equals(e.getErp()))
                    .filter(e -> (e.getType() == 1 || e.getType() == 2))
                    .count();
            userMeasureDataDTO.setInterfaceReportNum((int) interfaceReportNum);
            // 快捷调用成功次数
            long quickCallSuccessNum = measureDataList.stream().filter(e -> erp.equals(e.getErp()))
                    .filter(e -> (e.getType() == 3 || e.getType() == 10))
                    .filter(e -> e.getStatus() == 1)
                    .count();
            userMeasureDataDTO.setQuickCallSuccessNum((int) quickCallSuccessNum);
            // 快捷调用失败次数
            long quickCallFailNum = measureDataList.stream().filter(e -> erp.equals(e.getErp()))
                    .filter(e -> (e.getType() == 3 || e.getType() == 10))
                    .filter(e -> e.getStatus() == 0)
                    .count();
            userMeasureDataDTO.setQuickCallFailNum((int) quickCallFailNum);
            // mock模版数
            long mockTemplateNum = measureDataList.stream().filter(e -> erp.equals(e.getErp()))
                    .filter(e -> (e.getType() == 6 || e.getType() == 7))
                    .count();
            userMeasureDataDTO.setMockTemplateNum((int) mockTemplateNum);
            // 快捷调用生成mock模版数
            long quickCallMockTemplateNum = measureDataList.stream().filter(e -> erp.equals(e.getErp()))
                    .filter(e -> (e.getType() == 8 || e.getType() == 9))
                    .count();
            userMeasureDataDTO.setQuickCallMockTemplateNum((int) quickCallMockTemplateNum);
        } catch (Exception e) {
            log.error("buildMeasureDataInfo error", e);
        }
    }

    /**
     * 组装需求空间相关信息
     *
     * @param requirementInfoList
     * @param userMeasureDataDTO
     */
    private void buildRequirementInfo(List<RequirementInfo> requirementInfoList, UserMeasureDataDTO userMeasureDataDTO) {
        try {
            // 需求空间数
            long requirementInfoNum = requirementInfoList.stream().filter(e -> userMeasureDataDTO.getErp().equals(e.getCreator()))
                    .count();
            userMeasureDataDTO.setRequirementInfoNum((int) requirementInfoNum);
            // 需求空间绑定需求数
            long requirementInfoBindNum = requirementInfoList.stream().filter(e -> userMeasureDataDTO.getErp().equals(e.getCreator()))
                    .filter(e -> StringUtils.isNotEmpty(e.getRelatedRequirementCode()))
                    .count();
            userMeasureDataDTO.setRequirementInfoBindNum((int) requirementInfoBindNum);
        } catch (Exception e) {
            log.error("buildRequirementInfo error", e);
        }
    }

    /**
     * 组装热部署相关信息
     *
     * @param hotswapDeployInfoList
     * @param userMeasureDataDTO
     */
    private void buildHotswapInfo(List<HotswapDeployInfo> hotswapDeployInfoList, UserMeasureDataDTO userMeasureDataDTO) {
        try {
            String erp = userMeasureDataDTO.getErp();
            // 热更新次数
            long hotswapNum = hotswapDeployInfoList.stream().filter(e -> erp.equals(e.getDeployErp()))
                    .count();
            // 热更新成功次数
            long hotswapSuccessNum = hotswapDeployInfoList.stream()
                    .filter(e -> erp.equals(e.getDeployErp()) && e.getSucceed() != null && e.getSucceed() == 1)
                    .count();
            BigDecimal hotswapSuccessBigNum = new BigDecimal(hotswapSuccessNum);
            userMeasureDataDTO.setHotswapSuccessBigNum(hotswapSuccessBigNum);

            if (hotswapNum != 0) {
                // 热更新成功率
                BigDecimal hotswapBigNum = new BigDecimal(hotswapNum);
                String hotswapSuccessRatio = hotswapSuccessBigNum.divide(hotswapBigNum, 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).toString();
                userMeasureDataDTO.setHotswapSuccessRatio(hotswapSuccessRatio + "%");
                // 热更新成功总耗时
                Integer hotswapSuccessTime = hotswapDeployInfoList.stream().filter(e -> erp.equals(e.getDeployErp()))
                        .filter(e -> e.getSucceed() == 1)
                        .map(HotswapDeployInfo::getRemoteDeployCostTime)
                        .reduce(Integer::sum).orElse(0);
                // 热更新平均时长
                BigDecimal hotswapSuccessBigTime = new BigDecimal(hotswapSuccessTime);
                String hotswapSuccessAvgTime = hotswapSuccessBigTime.divide(hotswapSuccessBigNum, 2, BigDecimal.ROUND_HALF_UP)
                        .divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP).toString();
                userMeasureDataDTO.setHotswapSuccessAvgTime(hotswapSuccessAvgTime + "s");
            }
        } catch (Exception e) {
            log.error("buildHotswapInfo error", e);
        }
    }

    /**
     * 组装远程调试相关信息
     *
     * @param proxyDebuggerList
     * @param userMeasureDataDTO
     */
    private void buildProxyDebuggerInfo(List<ProxyDebuggerRegistry> proxyDebuggerList, UserMeasureDataDTO userMeasureDataDTO) {
        try {
            long remoteDebugNum = proxyDebuggerList.stream().filter(e -> userMeasureDataDTO.getErp().equals(e.getErp()))
                    .count();
            userMeasureDataDTO.setRemoteDebugNum((int) remoteDebugNum);
        } catch (Exception e) {
            log.error("buildProxyDebuggerInfo error", e);
        }
    }

    @Override
    public Boolean refreshMeasureDataDept() {
        log.info("refreshMeasureDataDept--开始执行");
        List<String> erpList = this.baseMapper.selectErps();
        log.info("refreshMeasureDataDept erpList size:{}", erpList.size());
        for (String erp : erpList) {
            try {
                UserVo userVo = userHelper.getUserBaseInfoByUserName(erp);
                if (Objects.nonNull(userVo)) {
                    String department = userVo.getOrganizationFullName();
                    MeasureData measureData = new MeasureData();
                    this.parseDepartment(department, measureData);
                    List<MeasureData> measureDataList = this.list(new LambdaQueryWrapper<MeasureData>()
                            .eq(MeasureData::getErp, erp)
                            .eq(MeasureData::getYn, DataYnEnum.VALID.getCode())
                            .select(MeasureData::getId));
                    List<Long> measureDataIds = measureDataList.stream().map(MeasureData::getId).collect(Collectors.toList());
                    log.info("refreshMeasureDataDept erp:{} 待更新部门数据size为：{}", erp, measureDataIds.size());
                    this.updateMeasureDataInBatches(department, measureData, measureDataIds);
                }
            } catch (Exception e) {
                log.error("refreshMeasureDataDept 异常，erp:{},错误信息:{}", erp, e);
            }
        }
        log.info("refreshMeasureDataDept--执行结束");
        return true;
    }

    public void updateMeasureDataInBatches(String department, MeasureData measureData, List<Long> measureDataIds) {
        if (CollectionUtils.isNotEmpty(measureDataIds)) {
            int fromIndex = 0;
            while (fromIndex < measureDataIds.size()) {
                int toIndex = Math.min(fromIndex + BATCH_SIZE, measureDataIds.size());
                List<Long> batchIds = measureDataIds.subList(fromIndex, toIndex);
                log.info("refreshMeasureDataDept size:{}, fromIndex:{}, toIndex:{}", batchIds.size(), fromIndex, toIndex);
                this.update(new LambdaUpdateWrapper<MeasureData>()
                        .set(MeasureData::getDepartment, department)
                        .set(MeasureData::getDep0, measureData.getDep0())
                        .set(MeasureData::getDep1, measureData.getDep1())
                        .set(MeasureData::getDep2, measureData.getDep2())
                        .set(MeasureData::getDep3, measureData.getDep3())
                        .in(MeasureData::getId, batchIds));
                fromIndex = toIndex;
            }
        }
    }

}

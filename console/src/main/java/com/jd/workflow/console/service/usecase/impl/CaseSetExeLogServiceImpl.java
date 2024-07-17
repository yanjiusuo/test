package com.jd.workflow.console.service.usecase.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.jd.common.util.StringUtils;
import com.jd.matrix.core.utils.CollectionUtils;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.CaseTypeEnum;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.usecase.CaseSetExeLogMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.client.MethodOutDto;
import com.jd.workflow.console.dto.constant.SystemConstants;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordDTO;
import com.jd.workflow.console.dto.usecase.CaseType;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.usecase.*;
import com.jd.workflow.console.entity.usecase.enums.CaseSetExeLogStatusEnum;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.jacoco.JacocoService;
import com.jd.workflow.console.service.param.IParamBuilderRecordService;
import com.jd.workflow.console.service.usecase.CaseSetExeLogDetailService;
import com.jd.workflow.console.service.usecase.CaseSetExeLogService;
import com.jd.workflow.console.service.usecase.CaseSetService;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.server.dto.requirement.QueryRequirementCodeParam;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import org.apache.poi.hssf.record.DVALRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description: 用例集执行记录表 服务实现类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Service
public class CaseSetExeLogServiceImpl extends ServiceImpl<CaseSetExeLogMapper, CaseSetExeLog> implements CaseSetExeLogService {

    @Autowired
    private CaseSetService caseSetService;
    @Autowired
    private UserHelper userHelper;
    @Resource
    private AppInfoServiceImpl appInfoService;
    @Autowired
    private CaseSetExeLogDetailService caseSetExeLogDetailService;
    @Autowired
    private IParamBuilderRecordService paramBuilderRecordService;
    @Autowired
    private JacocoService jacocoService;

    @Override
    public Long createCaseSetExeLog(CreateCaseSetExeLogParam createParam) {

        CaseSet caseSet = checkCreateCaseSetExeLogParam(createParam);
        List<String> caseIdList= obtainCaseIdList(caseSet);
        if (CollectionUtils.isEmpty(caseIdList)) {
            throw new StdException("没有可执行的用例数据");
        }
        CaseSetExeLog caseSetExeLog = new CaseSetExeLog();
        BeanUtils.copyProperties(createParam, caseSetExeLog);
        caseSetExeLog.setCreator(UserSessionLocal.getUser().getUserId());caseSetExeLog.setCaseTotalNo(caseIdList.size());
        caseSetExeLog.setRequirementId(caseSet.getRequirementId());
        save(caseSetExeLog);
        LambdaUpdateWrapper<CaseSet> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(CaseSet::getId, caseSet.getId()).setSql("`exe_count`=`exe_count`+1");
        caseSetService.update(updateWrapper);
        return caseSetExeLog.getId();
    }

    @NotNull
    private CaseSet checkCreateCaseSetExeLogParam(CreateCaseSetExeLogParam createParam) {
        CaseSet caseSet = caseSetService.getById(createParam.getCaseSetId());
        if (Objects.isNull(caseSet)) {
            throw new StdException("此用例集【" + createParam.getCaseSetId() + "】不存在");
        }
        if (!caseSetService.checkAuth(caseSet.getRequirementId(), UserSessionLocal.getUser().getUserId())) {
            throw new BizException("无权执行此用例集");
        }
        Integer caseType= caseSet.getCaseType();
        if(caseType== CaseTypeEnum.caseTypeJsfHttp.getCode() || caseType==CaseTypeEnum.caseTypeJsf.getCode() ){
            //校验机器是否为启动状态
            checkJsfIp(createParam);
        }
//        if(caseType== CaseTypeEnum.caseTypeJsfHttp.getCode() || caseType==CaseTypeEnum.caseTypeHttp.getCode() ) {
//            checkHttpIp(createParam);
//        }

        return caseSet;
    }

    private void checkJsfIp(CreateCaseSetExeLogParam createParam){
        String[] ipArr = createParam.getIp().split(":");
        if (ipArr.length != 2) {
            throw new StdException("ip格式应为[ip:端口]形式");
        }
        boolean enabled = jacocoService.isJacocoEnabled(ipArr[0]);
        if (!enabled) {
            throw new StdException("自测环境IP[" + ipArr[0] + "]未启动");
        }
    }

    private void checkHttpIp(CreateCaseSetExeLogParam createParam){
        HttpDebugDto httpDebugDto = JSON.parseObject(createParam.getHttpEnv(), HttpDebugDto.class);
        String targetAddress = httpDebugDto.getTargetAddress();
        String ip;
        String httpsAddress="https://test-local-debug.jd.com/";
        String httpAddress="http://test-local-debug.jd.com/";
        if (targetAddress.contains(httpsAddress)) {
            ip = targetAddress.replace(httpAddress, "");
        } else if (targetAddress.contains(httpAddress)) {
            ip = targetAddress.replace(httpAddress, "");
        } else {
            throw new StdException("地址格式应为https://test-local-debug.jd.com/[ip:端口]或http://test-local-debug.jd.com/[ip:端口]");
        }
        String[] ipArr = ip.split(":");
        if (ipArr.length != 2) {
            throw new StdException("ip格式应为[ip:端口]形式");
        }
        boolean enabled = jacocoService.isJacocoEnabled(ipArr[0]);
        if (!enabled) {
            throw new StdException("自测环境IP[" + ipArr[0] + "]未启动");
        }
    }

    private List<String>  obtainCaseIdList(CaseSet caseSet) {
        CaseType casejson = JSON.parseObject(caseSet.getSelectedData(), CaseType.class);
        List<String> httpSelectedList = casejson.getHttp().getSelected();
        List<String> jsfSelectedList = casejson.getJsf().getSelected();
        List<String> caseIdList = Stream.concat(httpSelectedList.stream(), jsfSelectedList.stream())
                .collect(Collectors.toList());
        if (Objects.isNull(caseIdList)) {
            caseIdList = new ArrayList<>();
        }
        return caseIdList;
    }

    @Override
    public Page<CaseSetExeLogDTO> pageList(PageCaseSetExeLogParam pageParam) {
        LambdaQueryWrapper<CaseSetExeLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CaseSetExeLog::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(CaseSetExeLog::getCaseSetId, pageParam.getCaseSetId());
        if (pageParam.getQueryType() == 1) {
            lqw.eq(CaseSetExeLog::getCreator, UserSessionLocal.getUser().getUserId());
        }
        lqw.orderByDesc(CaseSetExeLog::getId);

        Page<CaseSetExeLog> page = this.page(new Page<>(pageParam.getCurrent(), pageParam.getPageSize()), lqw);
        Page<CaseSetExeLogDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<CaseSetExeLogDTO> caseSetExeLogDTOS = page.getRecords().stream().map(caseSetExeLog -> {
            CaseSetExeLogDTO caseSetExeLogDTO = new CaseSetExeLogDTO();
            BeanUtils.copyProperties(caseSetExeLog, caseSetExeLogDTO);
            String percentage = calculatePercentage(caseSetExeLogDTO.getCaseTotalNo(), caseSetExeLogDTO.getCaseSuccessNo(), caseSetExeLogDTO.getCaseFailNo());
            caseSetExeLogDTO.setProgressPercentage(percentage);
            UserVo userVo = userHelper.getUserBaseInfoByUserName(caseSetExeLogDTO.getCreator());
            if (userVo != null && userVo.getUserCode() != null) {
                caseSetExeLogDTO.setCreatorName(userVo.getRealName());
            }
            return caseSetExeLogDTO;
        }).collect(Collectors.toList());
        result.setRecords(caseSetExeLogDTOS);
        return result;
    }

    @Override
    public CaseSetExeLogInfo detail(CaseSetExeLogDetailParam detailParam) {
        //设置执行记录信息
        CaseSetExeLogInfo caseSetExeLogInfo = new CaseSetExeLogInfo();
        CaseSetExeLog caseSetExeLog = obtainCaseSetExeLogById(detailParam.getCaseSetExeLogId());
        if (Objects.isNull(caseSetExeLog)) {
            throw new StdException("此执行记录【" + detailParam.getCaseSetExeLogId() + "】不存在。");
        }
        caseSetExeLogInfo.setIp(caseSetExeLog.getIp());
        caseSetExeLogInfo.setJsfAlias(caseSetExeLog.getJsfAlias());
        caseSetExeLogInfo.setBranchName(caseSetExeLog.getBranchName());
        caseSetExeLogInfo.setStatus(caseSetExeLog.getStatus());
        caseSetExeLogInfo.setHttpEnv(caseSetExeLog.getHttpEnv());
        caseSetExeLogInfo.setNewCodeCoverage(caseSetExeLog.getNewCodeCoverage());
        //设置应用信息
        CaseSet caseSet = caseSetService.getById(caseSetExeLog.getCaseSetId());
        if (Objects.isNull(caseSet)) {
            throw new StdException("此用例集【" + caseSetExeLog.getCaseSetId() + "】不存在。");
        }
        AppInfoDTO app = appInfoService.findApp(caseSet.getAppId());
        if (Objects.nonNull(app)) {
            caseSetExeLogInfo.setAppName(app.getAppName());
            caseSetExeLogInfo.setAppCode(app.getAppCode());
        }
        if (StringUtils.isNotBlank(caseSetExeLog.getBucketName())){
            caseSetExeLogInfo.setNewCodeCoverageIndexUrl("http://"+ SystemConstants.OSS_HOST_NAME + caseSetExeLog.getBucketName() + "/index.html");
        }

        //设置结果详情记录
        PageCaseSetExeLogDetailParam pageCaseSetExeLogDetailParam = new PageCaseSetExeLogDetailParam();
        pageCaseSetExeLogDetailParam.setCurrent(detailParam.getCurrent());
        pageCaseSetExeLogDetailParam.setPageSize(detailParam.getPageSize());
        pageCaseSetExeLogDetailParam.setCaseSetExeLogId(detailParam.getCaseSetExeLogId());
        Page<ParamBuilderRecordDTO> page = caseSetExeLogDetailService.pageList(pageCaseSetExeLogDetailParam);
        caseSetExeLogInfo.setPage(page);
        return caseSetExeLogInfo;
    }

    @Override
    public Page<CaseSetExeLog> listCaseSetExeLogs(PageCaseSetExeLogParam pageParam) {
        LambdaQueryWrapper<CaseSetExeLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CaseSetExeLog::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(CaseSetExeLog::getStatus, pageParam.getStatus());
        lqw.orderByDesc(CaseSetExeLog::getId);
        Page<CaseSetExeLog> page = this.page(new Page<>(pageParam.getCurrent(), pageParam.getPageSize()), lqw);
        return page;
    }

    @Override
    public boolean failNoAddOne(Long id) {
        LambdaUpdateWrapper<CaseSetExeLog> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(CaseSetExeLog::getId, id).setSql("`case_fail_no`=`case_fail_no`+1");
        return update(updateWrapper);
    }

    @Override
    public boolean successNoAddOne(Long id) {
        LambdaUpdateWrapper<CaseSetExeLog> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(CaseSetExeLog::getId, id).setSql("`case_success_no`=`case_success_no`+1");
        return update(updateWrapper);
    }

    @Override
    public boolean updateStatus(Long id, CaseSetExeLogStatusEnum preStatus, CaseSetExeLogStatusEnum resultStatus,String remark) {
        LambdaUpdateWrapper<CaseSetExeLog> updateWrapper = new LambdaUpdateWrapper();
        if (Objects.nonNull(preStatus)) {
            updateWrapper.eq(CaseSetExeLog::getStatus, preStatus.getCode());
        }
        updateWrapper.eq(CaseSetExeLog::getId, id);
        CaseSetExeLog caseSetExeLog = new CaseSetExeLog();
        caseSetExeLog.setStatus(resultStatus.getCode());
        if(StringUtils.isNotBlank(remark)){
            caseSetExeLog.setRemark(remark);
        }
        return update(caseSetExeLog, updateWrapper);
    }

    @Override
    public Long reExecute(Long id) {
        CaseSetExeLog caseSetExeLog = getById(id);
        if (Objects.isNull(caseSetExeLog)) {
            throw new StdException("此执行记录【" + caseSetExeLog.getCaseSetId() + "】不存在。");
        }
        CreateCaseSetExeLogParam createParam = new CreateCaseSetExeLogParam();
        createParam.setJsfAlias(caseSetExeLog.getJsfAlias());
        createParam.setIp(caseSetExeLog.getIp());
        createParam.setCaseSetId(caseSetExeLog.getCaseSetId());
        createParam.setHttpEnv(caseSetExeLog.getHttpEnv());
        return createCaseSetExeLog(createParam);
    }

    private CaseSetExeLog obtainCaseSetExeLogById(Long id) {
        LambdaQueryWrapper<CaseSetExeLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CaseSetExeLog::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(CaseSetExeLog::getId, id);
        CaseSetExeLog caseSetExeLog = getOne(lqw);
        if (Objects.isNull(caseSetExeLog)) {
            throw new StdException("此执行记录【" + id + "】不存在。");
        }
        return caseSetExeLog;
    }

    /**
     * 计算百分比
     *
     * @param caseTotalNo
     * @param caseSuccessNo
     * @param caseFailNo
     * @return
     */
    private String calculatePercentage(Integer caseTotalNo, Integer caseSuccessNo, Integer caseFailNo) {
        String defaultValue = "0";
        if (caseTotalNo > 0) {
            BigDecimal numerator = NumberUtils.toBigDecimal(caseSuccessNo).add(NumberUtils.toBigDecimal(caseFailNo));
            BigDecimal decimal = NumberUtils.toBigDecimal(caseTotalNo);
            BigDecimal divide = numerator.multiply(NumberUtils.toBigDecimal(100)).divide(decimal, 0, RoundingMode.HALF_UP);
            defaultValue = divide.toString();
        }
        return defaultValue;
    }

    @Override
    public boolean update2Success(CaseSetExeLog caseSetExeLog) {
        caseSetExeLog.setStatus(CaseSetExeLogStatusEnum.SUCCESS.getCode());
        return updateById(caseSetExeLog);
    }


    public List<String> queryRequirementCodes(QueryRequirementCodeParam queryParam) {
        if(Objects.isNull(queryParam)){
            throw new BizException("入参queryParam不能为null");
        }
        if(StringUtils.isBlank(queryParam.getCodingAddress())){
            throw new BizException("属性CodingAddress不能为空");
        }
        if(StringUtils.isBlank(queryParam.getBranchName())){
            throw new BizException("属性BranchName不能为空");
        }
        List<String> requirementCodes = this.getBaseMapper().queryRequirementCodes(queryParam);
        return requirementCodes;
    }

}


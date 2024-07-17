package com.jd.workflow.console.service.doc.impl;
import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;

import com.jd.fastjson.JSON;
import com.jd.laf.binding.annotation.JsonConverter;
import com.jd.laf.config.spring.annotation.LafValue;
import com.jd.matrix.core.utils.CollectionUtils;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.entity.jagile.AppJagileDeployInfo;
import com.jd.workflow.console.entity.statistics.FlowLineScanStatistics;
import com.jd.workflow.console.service.WebHookServiceImpl;
import com.jd.workflow.console.service.doc.SyncDocService;
import com.jd.workflow.console.service.jagile.AppJagileDeployInfoService;
import com.jd.workflow.console.service.statistics.FlowLineScanStatisticsService;
import com.jd.workflow.console.utils.CodingUtils;
import com.jd.workflow.console.utils.UUIDUtil;
import com.jd.workflow.webhook.Project;
import com.jd.workflow.webhook.WebHookVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description: 同步文档服务
 * @author: zhaojingchun
 * @Date: 2024/7/1
 */
@Service
@Slf4j
public class SyncDocServiceImpl implements SyncDocService {

    /**
     * 行云jdos部署记录表 服务类
     */
    @Autowired
    private AppJagileDeployInfoService appJagileDeployInfoService;
    @Autowired
    private WebHookServiceImpl webHookService;
    @Autowired
    private FlowLineScanStatisticsService statisticsService;

    //https://taishan.jd.com/ducc/web/nswork?nsId=7426&nsName=lht_solution&cId=709979&cName=dataflow&envId=843707&envName=pro&defAppId=12037&dataType=0
    @LafValue("data.flow.sync.doc.dept")
    @JsonConverter
    private List<String> deptList;

    @Override
    public void syncDocDependDeployInfo() {
        if (CollectionUtils.isEmpty(deptList)) return;
        String previousDateStr = DateUtil.obtainPreviousDateStr(1);
        for (String dept : deptList) {
            List<AppJagileDeployInfo> deployInfoList = appJagileDeployInfoService.fetchDeployInfoListAfter(dept, previousDateStr);
            for (AppJagileDeployInfo deployInfo : deployInfoList) {
                initStatistics(deployInfo);
                doJagLine(deployInfo);
            }
        }
    }

    /**
     * 获取不为空的属性，
     *
     * @param source
     * @return
     */
    private static String[] getNotNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> notEmptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (Objects.nonNull(srcValue)) {
                notEmptyNames.add(pd.getName());
            }

        }
        String[] result = new String[notEmptyNames.size()];
        return notEmptyNames.toArray(result);
    }

    /**
     * 初始化统计数据
     * @param deployInfo
     */
    private void initStatistics(AppJagileDeployInfo deployInfo){
        try {
            if(StringUtils.isNotBlank(deployInfo.getGitProject())){
                int quarterNo = DateUtil.getQuarterOfDay(deployInfo.getCreateTime());
                FlowLineScanStatistics flowLineScanStatistics = new FlowLineScanStatistics();

                flowLineScanStatistics.setLineStatus(-1);
                flowLineScanStatistics.setType(2);
                flowLineScanStatistics.setDept(deployInfo.getAppDeptFullname());
                flowLineScanStatistics.setBranch(deployInfo.getBranch());
                flowLineScanStatistics.setQuarterNo(quarterNo);
                String onlyCodingAddress = CodingUtils.getOnlyCodingAddress(deployInfo.getGitProject());
                flowLineScanStatistics.setCodeAddress(onlyCodingAddress);
                FlowLineScanStatistics scanStatisticsDB = statisticsService.getDataByCoding(onlyCodingAddress, quarterNo);
                if(Objects.nonNull(scanStatisticsDB)){
                    BeanUtils.copyProperties(scanStatisticsDB, flowLineScanStatistics, getNotNullPropertyNames(flowLineScanStatistics));
                }
                if(Objects.isNull(flowLineScanStatistics.getLineId())){
                    flowLineScanStatistics.setLineId(0L);
                }
                statisticsService.saveOrUpdate(flowLineScanStatistics);
            }
        } catch (Exception e) {
            log.error("SyncDocServiceImpl.doStatistics Exception ",e);
        }

    }

    private void doJagLine(AppJagileDeployInfo deployInfo) {
        WebHookVo webHookVo = new WebHookVo();
        try {
            webHookVo.setFlowId(UUIDUtil.getUUID());
            //脚本
            webHookVo.setType(1);
            webHookVo.setRef(deployInfo.getBranch());
            Project project = new Project();
            String gitHttpUrl = deployInfo.getGitProject().replace("git@coding.jd.com:", "https://coding.jd.com/");
            project.setGit_http_url(gitHttpUrl);
            webHookVo.setProject(project);
            log.info("SyncDocServiceImpl.doJagLine webHookVo : "+JSON.toJSONString(webHookVo));
            webHookService.jagline(webHookVo);
        } catch (Throwable e) {
            log.error("SyncDocServiceImpl.doJagLine Exception webHookVo ： " + JSON.toJSONString(webHookVo), e);
        }
    }


}

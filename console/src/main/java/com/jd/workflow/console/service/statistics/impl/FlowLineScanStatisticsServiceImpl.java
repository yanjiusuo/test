package com.jd.workflow.console.service.statistics.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.matrix.core.utils.CollectionUtils;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.dao.mapper.statistics.FlowLineScanStatisticsMapper;
import com.jd.workflow.console.entity.doc.SyncJsfDocLog;
import com.jd.workflow.console.entity.statistics.FlowLineScanStatistics;
import com.jd.workflow.console.service.doc.SyncJsfDocLogService;
import com.jd.workflow.console.service.statistics.FlowLineScanStatisticsService;
import com.jd.workflow.console.service.sync.SynJsfInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 统计应用，coding维度流水线扫描数据 服务实现类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-07-15
 */
@Service
public class FlowLineScanStatisticsServiceImpl extends ServiceImpl<FlowLineScanStatisticsMapper, FlowLineScanStatistics> implements FlowLineScanStatisticsService {

    @Autowired
    private SyncJsfDocLogService syncJsfDocLogService;
    @Autowired
    private SynJsfInfoService synJsfInfoService;

    /**
     * 获取当年季度内 coding维度的统计数据
     *
     * @param CodeAddress
     * @param quarterNo
     * @return
     */
    @Override
    public FlowLineScanStatistics getDataByCoding(String CodeAddress, int quarterNo) {
        LambdaQueryWrapper<FlowLineScanStatistics> lqw = new LambdaQueryWrapper();
        lqw.eq(FlowLineScanStatistics::getCodeAddress, CodeAddress)
                .eq(FlowLineScanStatistics::getYn, 1)
                .eq(FlowLineScanStatistics::getType, 2)
                .eq(FlowLineScanStatistics::getQuarterNo, quarterNo)
                .gt(FlowLineScanStatistics::getCreated, DateUtil.getFirstDateOfYear());
        return getOne(lqw);
    }

    public void updateScanData() {
        List<FlowLineScanStatistics> waitingDataList = getWaitingData();
        if (!CollectionUtils.isEmpty(waitingDataList)) {
            for (FlowLineScanStatistics scanStatistics : waitingDataList) {
                handleFlowLineScanStatistics(scanStatistics);
            }
        }
    }

    /**
     * 处理
     * @param scanStatistics
     */
    private void handleFlowLineScanStatistics(FlowLineScanStatistics scanStatistics) {
        try {
            SyncJsfDocLog lastJsfDocLogInfo = syncJsfDocLogService.getLastJsfDocLogInfo(scanStatistics.getCodeAddress());
            if (lastJsfDocLogInfo.isLastStatus()) {
                scanStatistics.setLineId(lastJsfDocLogInfo.getId());
                scanStatistics.setLineStatus(lastJsfDocLogInfo.getStatus());
                scanStatistics.setScanHttpNo(lastJsfDocLogInfo.getHttpNum());
                scanStatistics.setScanJsfNo(lastJsfDocLogInfo.getJsfNum());
                Integer jsfNo = synJsfInfoService.getJsfNoByCodingAddress(scanStatistics.getCodeAddress());
                scanStatistics.setJsfNo(jsfNo);
                updateById(scanStatistics);
            }
        } catch (Exception e) {
            log.error("FlowLineScanStatisticsServiceImpl.handleFlowLineScanStatistics Exception ", e);
        }
    }

    /**
     * 获取待执行数据
     *
     * @return
     */
    private List<FlowLineScanStatistics> getWaitingData() {
        LambdaQueryWrapper<FlowLineScanStatistics> lqw = new LambdaQueryWrapper();
        lqw.eq(FlowLineScanStatistics::getYn, 1)
                .eq(FlowLineScanStatistics::getLineStatus, -1);
        List<FlowLineScanStatistics> retList = list(lqw);
        return retList;
    }
}

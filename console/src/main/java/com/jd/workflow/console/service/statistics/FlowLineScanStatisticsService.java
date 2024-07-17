package com.jd.workflow.console.service.statistics;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.statistics.FlowLineScanStatistics;

/**
 * <p>
 * 统计应用，coding维度流水线扫描数据 服务类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-07-15
 */
public interface FlowLineScanStatisticsService extends IService<FlowLineScanStatistics> {

    FlowLineScanStatistics getDataByCoding(String CodeAddress,int quarterNo);

    /**
     * 更新扫描数据
     */
    void updateScanData();
}

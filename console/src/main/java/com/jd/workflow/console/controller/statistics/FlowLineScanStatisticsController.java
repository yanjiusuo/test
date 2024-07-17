package com.jd.workflow.console.controller.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.dto.requirement.RequirementInfoDto;
import com.jd.workflow.console.entity.statistics.FlowLineScanStatistics;
import com.jd.workflow.console.service.statistics.FlowLineScanStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 统计应用，coding维度流水线扫描数据 前端控制器
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-07-15
 */
@RestController
@RequestMapping("/statistics")
@Slf4j
public class FlowLineScanStatisticsController {
    @Autowired
    private FlowLineScanStatisticsService statisticsService;

    @GetMapping("/del")
    public void del() {
        try {
            LambdaQueryWrapper<FlowLineScanStatistics> lqw = new LambdaQueryWrapper<>();
            lqw.gt(FlowLineScanStatistics::getId, 0L);
            final boolean remove = statisticsService.remove(lqw);
        } catch (Exception e) {
            log.error("FlowLineScanStatisticsController.del", e);
        }
    }

    @GetMapping("/updateScanData")
    public CommonResult<Boolean> updateScanData() {
        try {
             statisticsService.updateScanData();
        } catch (Exception e) {
            log.error("FlowLineScanStatisticsController.del", e);
        }
        return CommonResult.buildSuccessResult(true);
    }

}

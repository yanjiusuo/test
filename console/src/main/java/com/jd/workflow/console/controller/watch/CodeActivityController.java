package com.jd.workflow.console.controller.watch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.entity.watch.CodeActivity;
import com.jd.workflow.console.entity.watch.CodeActivityCheckpoint;
import com.jd.workflow.console.entity.watch.CodeActivitySpan;
import com.jd.workflow.console.entity.watch.CodeActivityStatistic;
import com.jd.workflow.console.entity.watch.dto.CodeActivityDto;
import com.jd.workflow.console.service.watch.CodeActivityCheckpointService;
import com.jd.workflow.console.service.watch.CodeActivityService;
import com.jd.workflow.console.service.watch.CodeActivitySpanService;
import com.jd.workflow.console.service.watch.CodeActivityStatisticService;
import com.jd.workflow.soap.common.util.StdCalendar;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/codeActivity")
public class CodeActivityController {
    @Autowired
    CodeActivityStatisticService codeActivityStatisticService;

    @Autowired
    CodeActivityService codeActivityService;

    @Autowired
    CodeActivityCheckpointService checkpointService;

    @Autowired
    CodeActivitySpanService spanService;

    @RequestMapping(value = "/saveBatch", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<Boolean> saveBatch(
            @RequestBody @Valid
                    List<CodeActivityDto> dtos) throws Exception {
        if(!dtos.isEmpty()){
            String erp = dtos.get(0).getErp();
            if(StringUtils.isEmpty(erp)){
                erp = dtos.get(0).getUserName();
            }
            log.info("codeActivity.saveBatch:erp={},size={}",erp,dtos.size());
        }

        codeActivityService.saveActivities(dtos);
        return CommonResult.buildSuccessResult(true);
    }
    @RequestMapping(value = "/statistic", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<Boolean> statistic(
            ) throws Exception {

        codeActivityStatisticService.statisticDayCodeCostTime(new Date());
        codeActivityStatisticService.computeBuildTime(StringHelper.formatDate(new Date(),"yyyy-MM-dd"));
        return CommonResult.buildSuccessResult(true);
    }
    @RequestMapping(value = "/reStatistic", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<Boolean> reStatistic(
    ) throws Exception {
        {
            LambdaQueryWrapper<CodeActivityStatistic> lqw = new LambdaQueryWrapper<>();
            codeActivityStatisticService.remove(lqw);
        }
        {
            LambdaQueryWrapper<CodeActivityCheckpoint> lqw = new LambdaQueryWrapper<>();
             checkpointService.remove(lqw);
        }
        {
            LambdaQueryWrapper<CodeActivityCheckpoint> lqw = new LambdaQueryWrapper<>();
            checkpointService.remove(lqw);
        }
        {
            LambdaQueryWrapper<CodeActivitySpan> lqw = new LambdaQueryWrapper<>();
            spanService.remove(lqw);
        }
        LambdaQueryWrapper<CodeActivity> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(CodeActivity::getTime);
        lqw.last("limit 1");
        CodeActivity codeActivity = codeActivityService.getOne(lqw);

        computedDayBuildTime(codeActivity.getTime().getTime());
        StdCalendar stdCalendar = StdCalendar.fromTimestamp(new Timestamp(codeActivity.getTime().getTime()));

        while (stdCalendar.getTime().getTime() < System.currentTimeMillis()){
            codeActivityStatisticService.statisticDayCodeCostTime(stdCalendar.getTime());
            stdCalendar.moveDay(1);
        }
        return CommonResult.buildSuccessResult(true);
    }
    private void computedDayBuildTime(Long startTimestamp){
        StdCalendar stdCalendar = StdCalendar.fromTimestamp(new Timestamp(startTimestamp));

        while (stdCalendar.getTime().getTime() < System.currentTimeMillis()){
            String date = stdCalendar.toString("yyyy-MM-dd");
            codeActivityStatisticService.computeBuildTime(date);
            stdCalendar.moveDay(1);
        }



    }
    @RequestMapping(value = "/listCodeActivity", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<IPage<CodeActivity>> queryErpData(
            long current,long size,@RequestParam(required = false) String type
    ) throws Exception {
        return CommonResult.buildSuccessResult(codeActivityService.pageList(current, size, type));
    }

    @RequestMapping(value = "/removeCodeStatistic", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Boolean> removeCodeStatistic(
            Long id
    ) throws Exception {
        codeActivityStatisticService.removeById(id);
        return CommonResult.buildSuccessResult(true);
    }
    @RequestMapping(value = "/queryStatistic", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<CodeActivityStatistic>> queryAllStatisticData(
         @RequestParam(required = false) String type,@RequestParam(required = false) String erp
    ) throws Exception {
        return CommonResult.buildSuccessResult(codeActivityStatisticService.queryAll(type,erp));
    }

    @RequestMapping(value = "/queryCodeActivity", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<CodeActivity>> queryCodeActivity(
            Long current,Long size
    ) throws Exception {
        IPage<CodeActivity> heartbeat = codeActivityService.pageList(current, size, "heartbeat");
        return CommonResult.buildSuccessResult(heartbeat.getRecords());
    }
    @RequestMapping(value = "/compactCodeActivity", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Boolean> compactCodeActivity(
            String erp
    ) throws Exception {
        codeActivityService.compactCodeActivity(erp);
        return CommonResult.buildSuccessResult(true);
    }

    @RequestMapping(value = "/removeErpActivity", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Boolean> removeErpActivity(
            String erp
    ) throws Exception {
        codeActivityService.removeErpActivity(erp);
        return CommonResult.buildSuccessResult(true);
    }
    @RequestMapping(value = "/clearOneMonthCodeData", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Boolean> clearOneMonthCodeData(

    ) throws Exception {
        codeActivityService.clearOneMonthCodeData();
        return CommonResult.buildSuccessResult(true);
    }
}

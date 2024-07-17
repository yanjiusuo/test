package com.jd.workflow.console.service.watch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dao.mapper.watch.CodeActivityStatisticMapper;
import com.jd.workflow.console.entity.watch.CodeActivity;
import com.jd.workflow.console.entity.watch.CodeActivitySpan;
import com.jd.workflow.console.entity.watch.CodeActivityStatistic;
import com.jd.workflow.console.entity.watch.dto.CodeActivitySpanDto;
import com.jd.workflow.console.entity.watch.dto.CodeActivityTypeEnum;
import com.jd.workflow.console.entity.watch.dto.DayBuildActivityDto;
import com.jd.workflow.console.service.watch.constant.CodeActivityConstants;
import com.jd.workflow.soap.common.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class CodeActivityStatisticService extends ServiceImpl<CodeActivityStatisticMapper, CodeActivityStatistic> {
    @Value("${code.maxKeyAllowInterval:900000}")
    private int afterKeyMaxAllowInterval = 15 * 60 * 1000;
    @Autowired
    CodeActivityCheckpointService codeActivityCheckpointService;
    @Autowired
    CodeActivitySpanService codeActivitySpanService;
    @Autowired
    CodeActivityService codeActivityService;
    @Autowired
    TransactionTemplate transactionTemplate;

    public List<CodeActivityStatistic> fetchDayData(CodeActivityTypeEnum type, String day) {
        LambdaQueryWrapper<CodeActivityStatistic> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CodeActivityStatistic::getStatisticDay, day);
        lqw.eq(CodeActivityStatistic::getType, type.name());
        return list(lqw);
    }

    public void computeBuildTime(String dayFormat){
        List<DayBuildActivityDto> dayBuildDtos = codeActivityService.getBaseMapper().queryDayBuildTime(dayFormat);
        LambdaQueryWrapper<CodeActivityStatistic> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CodeActivityStatistic::getType, CodeActivityTypeEnum.build);
        lqw.eq(CodeActivityStatistic::getStatisticDay,dayFormat);
        remove(lqw);
        List<CodeActivityStatistic> statistics = new ArrayList<>();
        for (DayBuildActivityDto dayBuildDto : dayBuildDtos) {
            CodeActivityStatistic statistic = new CodeActivityStatistic();
            statistic.setType(CodeActivityTypeEnum.build.name());
            statistic.setErp(dayBuildDto.getErp());
            statistic.setCostTime(dayBuildDto.getCostTime() == null? 0L :dayBuildDto.getCostTime());
            statistic.setStatisticDay(dayFormat);
            statistics.add(statistic);
        }
        saveBatch(statistics);
    }

    public void statisticDayCodeCostTime(Date day) {
        Long size = 2500L;
        Map<String, List<CodeActivitySpanDto>> erp2Spans = new HashMap<>();

        int current = 1;
        while (true) {
            List<CodeActivity> codeActivities = codeActivityService.fetchDayCodeActivities(day, CodeActivityTypeEnum.heartbeat,current, size);
            current++;
            if (codeActivities == null || codeActivities.isEmpty()) {
                break;
            }
            Collections.sort(codeActivities, Comparator.comparing(CodeActivity::getTime));

            computeDayCodeCostTime(codeActivities, erp2Spans);
        }

        updateHeartPointAndStatistic(erp2Spans, day);
    }

    private void updateHeartPointAndStatistic(Map<String, List<CodeActivitySpanDto>> erp2Spans, Date day) {

        {
            LambdaQueryWrapper<CodeActivityStatistic> lqw = new LambdaQueryWrapper<>();
            lqw.eq(CodeActivityStatistic::getType, CodeActivityTypeEnum.heartbeat.name());
            lqw.eq(CodeActivityStatistic::getStatisticDay, StringHelper.formatDate(day, CodeActivityConstants.DAY_FORMAT));
            remove(lqw);
        }

        {
            LambdaQueryWrapper<CodeActivitySpan> lqw = new LambdaQueryWrapper<>();
            lqw.eq(CodeActivitySpan::getStatisticDay, StringHelper.formatDate(day, CodeActivityConstants.DAY_FORMAT));
            codeActivitySpanService.remove(lqw);
        }

        List<CodeActivityStatistic> statistics = new ArrayList<>();
        List<CodeActivitySpan> spans = new ArrayList<>();
        String dayFormat = StringHelper.formatDate(day, CodeActivityConstants.DAY_FORMAT);
        for (Map.Entry<String, List<CodeActivitySpanDto>> entry : erp2Spans.entrySet()) {

                CodeActivityStatistic newStat = new CodeActivityStatistic();
                newStat.setErp(entry.getKey());
                Optional<Long> optional = entry.getValue().stream().map(item -> item.getCostTime()).reduce((a, b) -> a + b);
                if(optional.isPresent()){
                    newStat.setCostTime(optional.get());
                }else{
                    newStat.setCostTime(0L);
                }
                newStat.setType(CodeActivityTypeEnum.heartbeat.name());
                newStat.setStatisticDay(dayFormat);
                statistics.add(newStat);

                spans.addAll(entry.getValue().stream().map(item->(CodeActivitySpan)item.toSpan(entry.getKey())).collect(Collectors.toList()));

        }

        saveBatch(statistics);
        codeActivitySpanService.saveBatch(spans);

    }

    /**
     *
     * @param activities
     * @param erp2Spans 每天每个人耗费的时间
     */
    private void computeDayCodeCostTime(List<CodeActivity> activities, Map<String, List<CodeActivitySpanDto>> erp2Spans) {
        Map<String, List<CodeActivitySpanDto>> computed = computeErpCostSpans(activities); // 计算每个erp的耗时
        mergeCostTimeMap(erp2Spans, computed);
    }

    private void mergeCostTimeMap(Map<String, List<CodeActivitySpanDto>> exist, Map<String, List<CodeActivitySpanDto>> computed) {
        for (Map.Entry<String, List<CodeActivitySpanDto>> entry : computed.entrySet()) {
            List<CodeActivitySpanDto> existList = exist.computeIfAbsent(entry.getKey(), vs -> new ArrayList<>());
            codeActivitySpanService.mergeSpan(existList, entry.getValue());
            exist.put(entry.getKey(), existList);
        }
    }



    private Map<String, List<CodeActivitySpanDto>> computeErpCostSpans(List<CodeActivity> activities) {
        Map<String, List<CodeActivitySpanDto>> erp2Spans = new HashMap<>();
        Map<String, List<CodeActivity>> erp2Activities = activities.stream().collect(Collectors.groupingBy(new Function<CodeActivity, String>() {
            @Override
            public String apply(CodeActivity codeActivity) {
                String erp = codeActivity.getErp();
                if (StringUtils.isEmpty(erp)) {
                    erp = codeActivity.getUserName();
                }
                if (StringUtils.isEmpty(erp)) {
                    return "";
                }

                return erp;
            }
        }));
        for (Map.Entry<String, List<CodeActivity>> entry : erp2Activities.entrySet()) {
            List<CodeActivity> erpActivities = entry.getValue();
            CodeActivity lastActivity = codeActivityService.fetchLastCodeActivity(erpActivities.get(0));
            List<CodeActivitySpanDto> spans = codeActivitySpanService.computeSpans(erpActivities,lastActivity);
            erp2Spans.put(entry.getKey(), spans);
        }
        return erp2Spans;
    }

     Long computePersonDayCodeCostTime(List<CodeActivity> activities,CodeActivity lastActivity) {
         List<CodeActivitySpanDto> spans = codeActivitySpanService.computeSpans(activities, lastActivity);
         return spans.stream().map(item->item.getCostTime()).reduce((a,b)->a+b).get();

    }



    public List<CodeActivityStatistic> queryAll(String type,String erp){
        LambdaQueryWrapper<CodeActivityStatistic> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StringUtils.isNotBlank(type),CodeActivityStatistic::getType,type);
        lqw.eq(StringUtils.isNotBlank(type),CodeActivityStatistic::getErp,erp);
        lqw.orderByAsc(CodeActivityStatistic::getId);
        return list(lqw);
    }

}

package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.manage.RankScore;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.MathHelper;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScoreManageService {
    public static Integer PERCENT_DOC_INFO = 20;
    public static Integer PERCENT_INPUT = 40;
    public static Integer PERCENT_OUTPUT = 40;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    MethodManageServiceImpl methodManageService;

    @Autowired
    IAppInfoService appInfoService;
    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;


    public double computeMethodScore(MethodManage method){
        double score = 0.0;
        if(StringUtils.isNotEmpty(method.getDocInfo())){
            score+= PERCENT_DOC_INFO;
        }
        List<JsonType> input = new ArrayList<>();
        List<JsonType> output = new ArrayList<>();
        if(method.getContentObject() == null){
            methodManageService.initContentObject(method,0,null);
        }
        if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
            HttpMethodModel methodModel = (HttpMethodModel) method.getContentObject();
            addIfNotEmpty(input,methodModel.getInput().getParams());
            addIfNotEmpty(input,methodModel.getInput().getPath());
            addIfNotEmpty(input,methodModel.getInput().getHeaders());
            addIfNotEmpty(input,methodModel.getInput().getBody());

            addIfNotEmpty(output,methodModel.getOutput().getBody());
            addIfNotEmpty(output,methodModel.getOutput().getHeaders());
        }else if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
            JsfStepMetadata jsfStepMetadata = (JsfStepMetadata) method.getContentObject();
            addIfNotEmpty(input,jsfStepMetadata.getInput());
            if(jsfStepMetadata.getOutput() != null){
                input.add(jsfStepMetadata.getOutput());
            }
        }

        score+=getPercent(input)* PERCENT_INPUT;
        score+=getPercent(output)* PERCENT_OUTPUT;
        return score;
    }

    public double getPercent(List<JsonType> jsonTypes){
        int fieldCount = getFieldCount(jsonTypes);
        if(fieldCount == 0) return 1.0;
        int hasFieldCount = getHasDescFieldCount(jsonTypes,false);
        double v = (double) hasFieldCount / fieldCount;
        if(v >=1.0){
            v = 1.0;
        }
        return v;
    }

    private int getFieldCount(JsonType jsonType){
        int total = 1;
        if(jsonType instanceof SimpleJsonType){
            return total;
        }else if(jsonType instanceof ComplexJsonType){
            if("root".equals(jsonType.getName())) total = 0; // 忽略根节点
            total+=getFieldCount(((ComplexJsonType) jsonType).getChildren());
        }
        return total;
    }
    private int getFieldCount(List<JsonType> jsonTypes){
        if(jsonTypes == null) return 0;
        int total = 0;
        for (JsonType jsonType : jsonTypes) {
            total+=getFieldCount(jsonType);
        }
        return total;
    }
    private int getHasDescFieldCount(JsonType jsonType,boolean parentIsArray){
        int total = 0;
        if(parentIsArray || StringUtils.isNotBlank(jsonType.getDesc())){// 父节点是数组，认为描述已经有了呢
            total+=1;
        }
        if(jsonType instanceof ComplexJsonType){
            total+=getHasDescFieldCount(((ComplexJsonType) jsonType).getChildren(),jsonType instanceof ArrayJsonType);
        }
        return total;
    }
    private int getHasDescFieldCount(List<JsonType> jsonTypes,boolean parentIsArray){
        int total = 0;
        if(jsonTypes == null) return 0;
        for (JsonType jsonType : jsonTypes) {
            total+=getHasDescFieldCount(jsonType,parentIsArray);
        }
        return total;
    }


    private void addIfNotEmpty(Collection<JsonType> target, Collection<? extends JsonType> src){
        if(src== null) return;
        target.addAll(src);
    }
    public void updateMethodsScore(List<MethodManage> methods){
        for (MethodManage method : methods) {
            double score = computeMethodScore(method);
            LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper<>();
            luw.set(MethodManage::getScore,score);
            luw.eq(MethodManage::getId,method.getId());
            methodManageService.update(luw);
        }
    }
    public double computeMethodScore(Long methodId){
        MethodManage method = methodManageService.getById(methodId);
        methodManageService.initContentObject(method);
        InterfaceManage interfaceManage = interfaceManageService.getById(method.getInterfaceId());
        List<MethodManage> newMethods = Collections.singletonList(method);
        methodManageService.initMethodRefAndDelta(newMethods,interfaceManage.getAppId());
        double score = computeMethodScore(method);
        return score;
    }
    public void updateMethodScore(Long methodId){
        MethodManage method = methodManageService.getById(methodId);
        methodManageService.initContentObject(method);
        InterfaceManage interfaceManage = interfaceManageService.getById(method.getInterfaceId());
        List<MethodManage> newMethods = Collections.singletonList(method);
        methodManageService.initMethodRefAndDelta(newMethods,interfaceManage.getAppId());
        updateMethodsScore(newMethods);
    }
    public void updateInterfaceScore(List<InterfaceManage> interfaceManages){
        List<Long> ids = interfaceManages.stream().map(item->item.getId()).collect(Collectors.toList());
        Map<Long,List<InterfaceManage>> id2Interface = interfaceManages.stream().collect(Collectors.groupingBy(InterfaceManage::getId));
        QueryWrapper<MethodManage> qw = new QueryWrapper<>();
        qw.select("sum(score) as score","count(*) as count","interface_id");
        qw.in("interface_id",ids);
        qw.groupBy("interface_id");
        List<Map<String, Object>> methodScores = methodManageService.listMaps(qw);

        for (Map<String, Object> methodScore : methodScores) {
            Long interfaceId = (Long) methodScore.get("interface_id");
            Double score = Variant.valueOf(methodScore.get("score")).toDouble(0.0);
            int count = Variant.valueOf(methodScore.get("count")).toInt();
            double interfaceScore = 0.0;
            if(count != 0){
                interfaceScore = score/count;
            }
            LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
            luw.eq(InterfaceManage::getId, interfaceId);
            luw.set(InterfaceManage::getScore,interfaceScore);
            interfaceManageService.update(luw);
        }
    }

    // 统计未被删除的接口的评分
    public List<RankScore> getMethodRankScores(){
        QueryWrapper<MethodManage> qw = new QueryWrapper<>();
        qw.select("floor(ifnull(score,0)) as score","count(*) as count");
       // qw.isNotNull("score");
        qw.eq("yn",1);
        qw.in("type",1,3);
        qw.inSql("interface_id","select id from interface_manage where yn=1");
        qw.groupBy("floor(ifnull(score,0))");
        List<Map<String, Object>> scoreMapList = methodManageService.listMaps(qw);
        List<RankScore> scores = new ArrayList<>();
        for (Map<String, Object> scoreMap : scoreMapList) {
            RankScore score = new RankScore();
            score.setCount(Variant.valueOf(scoreMap.get("count")).toInt());
            score.setRank(Variant.valueOf(scoreMap.get("score")).toInt() );
            scores.add(score);
        }
        log.info("score.get_interface_rank_data:result={}", JsonUtils.toJSONString(scoreMapList));
        return scores;
    }
    public List<RankScore> getInterfaceRankScores(){
        QueryWrapper<InterfaceManage> qw = new QueryWrapper<>();
        qw.select("floor(ifnull(score,0)) as score","count(*) as count");
        //qw.isNotNull("score");
        qw.eq("yn",1);
        qw.groupBy("floor(ifnull(score,0))");
        List<Map<String, Object>> scoreMapList = interfaceManageService.listMaps(qw);
        List<RankScore> scores = new ArrayList<>();
        for (Map<String, Object> scoreMap : scoreMapList) {
            RankScore score = new RankScore();
            score.setCount(Variant.valueOf(scoreMap.get("count")).toInt());
            score.setRank(Variant.valueOf(scoreMap.get("score")).toInt() );
            scores.add(score);
        }
        log.info("score.get_interface_rank_data:result={}", JsonUtils.toJSONString(scoreMapList));
        return scores;
    }


    public void initAllAppScores(){
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.inSql(AppInfo::getId,"select app_id from interface_manage where yn = 1 and type=1 or type = 3");
        lqw.eq(AppInfo::getYn,1);
        List<AppInfo> appInfos = appInfoService.list(lqw);
        for (AppInfo appInfo : appInfos) {
            scheduleService.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        initAppScore(appInfo);
                    }catch (Exception e){
                        log.error("app.update_scopre_fail:appCode={}",appInfo.getAppCode(),e);
                    }
                }
            });
        }
    }

    public void initAppScore(AppInfo appInfo){
        List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterfaces(appInfo.getId());
        if(appInterfaces.isEmpty()) return;
        int start = 1;
        while (true){
            LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
            lqw.in(MethodManage::getInterfaceId,appInterfaces.stream().map(item->item.getId()).collect(Collectors.toList()));
            lqw.eq(MethodManage::getYn,1);
            lqw.orderByAsc(MethodManage::getId);
            Page<MethodManage> pageMethods = methodManageService.page(new Page<>(start, 300), lqw);
            methodManageService.initMethodRefAndDelta(pageMethods.getRecords(), appInfo.getId());
            if(pageMethods.getRecords().isEmpty()){
                break;
            }
            updateMethodsScore(pageMethods.getRecords());

            start++;
        }
        updateInterfaceScore(appInterfaces);
    }
}

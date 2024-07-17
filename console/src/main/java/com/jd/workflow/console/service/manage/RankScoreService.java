package com.jd.workflow.console.service.manage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.RankTypeEnum;
import com.jd.workflow.console.dao.mapper.manage.RankScoreMapper;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.manage.RankScore;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.impl.ScoreManageService;
import com.jd.workflow.soap.common.util.MathHelper;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RankScoreService extends ServiceImpl<RankScoreMapper, RankScore> {

    private Map<Integer,List<RankScore>> rankScoreMap = new ConcurrentHashMap<>();

    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    IInterfaceManageService interfaceManageService;

    @Autowired
    ScoreManageService scoreManageService;
    @PostConstruct
    public void init(){
        initData();
    }
    // 5分钟重新获取一下分数信息
    @Scheduled(fixedDelay = 1000*60*1L)
    public void scheduleFetchScores(){
        log.info("rank.update_score_rank");
        initData();
    }
    public void initData(){
        List<RankScore> rankScores = list();

        Map<Integer, List<RankScore>> type2Scores = rankScores.stream().collect(Collectors.groupingBy(RankScore::getType));
        for (Map.Entry<Integer, List<RankScore>> entry : type2Scores.entrySet()) {
            Collections.sort(entry.getValue(), new Comparator<RankScore>(){

                @Override
                public int compare(RankScore o1, RankScore o2) {

                    return o1.getRank() - o2.getRank();
                }
            });
        }
        RankScore prev = null;
        for (Map.Entry<Integer, List<RankScore>> entry : type2Scores.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                RankScore score = entry.getValue().get(i);
                if(prev != null){
                    score.setCount(prev.getCount()+score.getCount());
                }
                prev = score;
            }
        }
        rankScoreMap.putAll(type2Scores);
    }

    public double getMethodRank(Long methodId){
        MethodManage method = methodManageService.getById(methodId);

        return rank(method.getScore(),RankTypeEnum.METHOD.getCode());
    }

    public double rank(double score,int type){
        if(score == 0.0) return 0.0;
        int rankLevel = (int) score;
        List<RankScore> scores = rankScoreMap.get(type);
        if(ObjectHelper.isEmpty(scores)) return 0.0;
        int totalScore = scores.get(scores.size() - 1).getCount();
        int count = 0;
        for (int i = 0; i < scores.size(); i++) {
            if(scores.get(i).getRank().equals(rankLevel)){
                double percent = scores.get(i).getCount()/Double.valueOf(totalScore)*100;
                return MathHelper.roundHalfUp(percent,2).doubleValue();

            }
        }
        return 0.0;
    }

    private RankScore findByRank(List<RankScore> scores,int i){
        for (RankScore score : scores) {
            if(score.getRank().equals(i)) return score;
        }
        return null;
    }
    public void updateRankScores(){
        updateRankScoreData(RankTypeEnum.INTERFACE.getCode(),scoreManageService.getInterfaceRankScores());
        updateRankScoreData(RankTypeEnum.METHOD.getCode(),scoreManageService.getMethodRankScores());
    }
    private void updateRankScoreData(Integer type,List<RankScore> dbScores){
        List<RankScore> result = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            RankScore score = findByRank(dbScores, i);
            if(score == null){
                score = new RankScore();
                score.setRank(i);
                score.setCount(0);
            }
            score.setType(type);
            result.add(score);
        }
        List<RankScore> exist = getByType(type);
        merge(result,exist);
    }
    private void merge(List<RankScore> newData,List<RankScore> existData) {
       List<RankScore> added = new ArrayList<>();
       List<RankScore> updated = new ArrayList<>();
        for (RankScore score : newData) {
            RankScore exist = findByRank(existData, score.getRank());
            if(exist != null){
                exist.setCount(score.getCount());
                updated.add(exist);
            }else{
                added.add(score);
            }
        }
        if(!added.isEmpty()){
            saveBatch(added);
        }
        if(!updated.isEmpty()){
            updateBatchById(updated);
        }
    }
    private List<RankScore> getByType(Integer type){
        LambdaQueryWrapper<RankScore> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RankScore::getType,type);
        return list(lqw);
    }

}

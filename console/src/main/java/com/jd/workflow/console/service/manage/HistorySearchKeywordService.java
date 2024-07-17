package com.jd.workflow.console.service.manage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.manage.HistorySearchKeywordMapper;
import com.jd.workflow.console.dao.mapper.manage.RankScoreMapper;
import com.jd.workflow.console.entity.manage.HistorySearchKeyword;
import com.jd.workflow.console.entity.manage.RankScore;
import com.jd.workflow.soap.common.util.StringHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistorySearchKeywordService extends ServiceImpl<HistorySearchKeywordMapper, HistorySearchKeyword> {

    public void saveSearchStr(String search){

        List<String> result = StringHelper.split(search, " ");
        result = result.stream().filter(item->StringHelper.isNotBlank(item)).collect(Collectors.toList());
        for (String s : result) {
            s = s.trim();
            HistorySearchKeyword word = getBySearch(s);
            if(word == null){
                 word = new HistorySearchKeyword();
                word.setYn(1);
                word.setOperator(UserSessionLocal.getUser().getUserId());
                word.setSearch(s);
                save(word);
            }else{
                updateById(word);
            }

        }
    }
    private HistorySearchKeyword getBySearch(String search){
        LambdaQueryWrapper<HistorySearchKeyword> lqw = new LambdaQueryWrapper<>();
        lqw.eq(HistorySearchKeyword::getSearch,search);
        lqw.eq(HistorySearchKeyword::getOperator,UserSessionLocal.getUser().getUserId());
        List<HistorySearchKeyword> result = list(lqw);
        if(result.isEmpty()) return null;
        return result.get(0) ;
    }
    public List<String> getKeyWords(){
        LambdaQueryWrapper<HistorySearchKeyword> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(HistorySearchKeyword::getModified);
        lqw.eq(HistorySearchKeyword::getOperator,UserSessionLocal.getUser().getUserId());
        Page<HistorySearchKeyword> page = page(new Page<>(1, 10), lqw);
        return page.getRecords().stream().map(item->item.getSearch()).collect(Collectors.toList());

    }

}

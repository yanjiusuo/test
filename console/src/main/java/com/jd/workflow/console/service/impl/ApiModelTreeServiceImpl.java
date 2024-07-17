package com.jd.workflow.console.service.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.ApiModelTreeMapper;
import com.jd.workflow.console.entity.model.ApiModelTree;
import com.jd.workflow.console.service.model.IApiModelTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */
@Slf4j
@Service
public class ApiModelTreeServiceImpl extends ServiceImpl<ApiModelTreeMapper, ApiModelTree> implements IApiModelTreeService {
    @Override
    public ApiModelTree getTreeByAppId(Long appId) {
        LambdaQueryWrapper<ApiModelTree> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ApiModelTree::getAppId, appId);
        lqw.eq(ApiModelTree::getYn, 1);
        ApiModelTree apiModelTree = this.getOne(lqw);
        return apiModelTree;

    }
    @Override
    public boolean removeDuplicated(){
        LambdaQueryWrapper<ApiModelTree> lqw = new LambdaQueryWrapper<>();
        lqw.select(ApiModelTree::getAppId,ApiModelTree::getId,ApiModelTree::getYn,ApiModelTree::getCreated,ApiModelTree::getModified);
        lqw.eq(ApiModelTree::getYn,1);
        List<ApiModelTree> trees = list(lqw);
        Map<Long, List<ApiModelTree>> id2Models = trees.stream().collect(Collectors.groupingBy(ApiModelTree::getAppId));
        List<ApiModelTree> removed = new ArrayList<>();
        for (Map.Entry<Long, List<ApiModelTree>> entry : id2Models.entrySet()) {
            if(entry.getValue().size() <=1) continue;
            Collections.sort(entry.getValue(), new Comparator<ApiModelTree>() {
                @Override
                public int compare(ApiModelTree o1, ApiModelTree o2) {
                    return -o1.getModified().compareTo(o2.getModified());
                }
            });
            removed.addAll(entry.getValue().subList(1,entry.getValue().size()));
        }
        LambdaUpdateWrapper<ApiModelTree> luw = new LambdaUpdateWrapper<>();
        luw.set(ApiModelTree::getYn,0);
        List<Long> removeIds = removed.stream().map(item -> item.getId()).collect(Collectors.toList());
        if(removeIds.isEmpty()){
            return false;
        }
        luw.in(ApiModelTree::getId, removeIds);
        log.info("api.remove_ids:ids={}",removeIds);
        update(luw);
        return true;
    }

    public static void main(String[] args) {
        Long time = System.currentTimeMillis();
        Long time1= time+2000;

    }
}

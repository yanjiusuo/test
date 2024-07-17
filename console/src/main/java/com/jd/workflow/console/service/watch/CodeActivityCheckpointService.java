package com.jd.workflow.console.service.watch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.watch.CodeActivityCheckpointMapper;
import com.jd.workflow.console.entity.watch.CodeActivityCheckpoint;
import com.jd.workflow.console.entity.watch.dto.CodeActivityCheckpointTypeEnum;
import org.springframework.stereotype.Service;

@Service
public class CodeActivityCheckpointService extends ServiceImpl<CodeActivityCheckpointMapper,CodeActivityCheckpoint>   {

    public CodeActivityCheckpoint fetchLastCheckpoint(CodeActivityCheckpointTypeEnum type){
        LambdaQueryWrapper<CodeActivityCheckpoint> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(CodeActivityCheckpoint::getId);
        lqw.last("limit 1");
        CodeActivityCheckpoint checkPoint = getOne(lqw);
        return checkPoint;
    }
}

package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.DevCodeInfoMapper;
import com.jd.workflow.console.entity.DevCodeInfo;
import com.jd.workflow.console.service.DevCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CodeServiceImpl extends ServiceImpl<DevCodeInfoMapper, DevCodeInfo> implements DevCodeService {


}
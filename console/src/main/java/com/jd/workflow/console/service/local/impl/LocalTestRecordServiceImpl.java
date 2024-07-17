package com.jd.workflow.console.service.local.impl;

import com.jd.workflow.console.entity.local.LocalTestRecord;
import com.jd.workflow.console.dao.mapper.local.LocalTestRecordMapper;
import com.jd.workflow.console.service.local.ILocalTestRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 本地测试记录流水 服务实现类
 * </p>
 *
 * @author sunchao81
 * @since 2024-07-02
 */
@Service
public class LocalTestRecordServiceImpl extends ServiceImpl<LocalTestRecordMapper, LocalTestRecord> implements ILocalTestRecordService {

}

package com.jd.workflow.console.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.doc.AppInterfaceCount;
import com.jd.workflow.console.dto.doc.InterfaceTypeCount;
import com.jd.workflow.console.dto.manage.CountQueryDto;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * 方法管理接口
 * @date: 2022/5/16 18:18
 * @author wubaizhao1
 */
public interface MethodManageMapper extends BaseMapper<MethodManage> {
    public List<InterfaceTypeCount> queryInterfaceTypeCount(CountQueryDto dto);

    public List<AppInterfaceCount> queryInterfaceMethodCount(CountQueryDto dto);
}

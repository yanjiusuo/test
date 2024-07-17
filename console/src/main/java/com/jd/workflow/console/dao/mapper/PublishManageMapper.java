package com.jd.workflow.console.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.PublishMethodDTO;
import com.jd.workflow.console.dto.PublishMethodQueryDTO;
import com.jd.workflow.console.entity.PublishManage;

import java.util.List;

/**
 * 项目名称：example
 * 类 名 称：PublishManageMapper
 * 类 描 述：发布管理dao
 * 创建时间：2022-06-01 09:54
 * 创 建 人：wangxiaofei8
 */
public interface PublishManageMapper extends BaseMapper<PublishManage> {

    public List<PublishMethodDTO> queryPublishMethodList(PublishMethodQueryDTO dto);

    public Long queryPublishMethodCount(PublishMethodQueryDTO dto);
}

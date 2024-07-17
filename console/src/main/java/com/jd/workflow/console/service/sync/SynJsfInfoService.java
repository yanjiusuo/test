package com.jd.workflow.console.service.sync;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/7/8
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.sync.SynJsfInfo;

import java.util.List;

/**
 * <p>
 * jsf平台同步的接口信息表 服务类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-07-08
 */
public interface SynJsfInfoService extends IService<SynJsfInfo> {

    /**
     *
     * @return
     */
    List<SynJsfInfo> queryList();

    /**
     * 通过Coding地址获取Jsf接口数量
     * @param codingAddress
     * @return
     */
    Integer getJsfNoByCodingAddress(String codingAddress);

}

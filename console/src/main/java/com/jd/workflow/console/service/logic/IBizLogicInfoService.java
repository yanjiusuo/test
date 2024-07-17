package com.jd.workflow.console.service.logic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.logic.BizLogicInfo;

import java.util.List;

/**
 * <p>
 * 组件业务逻辑信息表 从藏经阁同步 服务类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-06-17
 */
public interface IBizLogicInfoService extends IService<BizLogicInfo> {

    /**
     * 保存更新BizLogicInfo
     *
     * @param bizLogicInfo
     * @return
     */
    boolean saveOrUpdateData(BizLogicInfo bizLogicInfo);

    /**
     * 通过接口名获取BizLogicInfo信息
     * @param interfaceName
     * @return
     */
    List<BizLogicInfo> obtainInfoListByInterfaceName(String interfaceName);

    /**
     * 通过接口名获取BizLogicInfo信息
     * @param interfaceName
     * @return
     */
    List<BizLogicInfo> obtainInfoListByInterfaceAndMethod(String interfaceName,String method);
}

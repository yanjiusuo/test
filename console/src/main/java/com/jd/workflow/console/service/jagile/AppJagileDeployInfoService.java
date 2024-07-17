package com.jd.workflow.console.service.jagile;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.jagile.AppJagileDeployInfo;

import java.util.List;

/**
 * <p>
 * 行云jdos部署记录表 服务类
 * </p>
 *
 * @author zhaojingchun
 * @since 2023-07-10
 */
public interface AppJagileDeployInfoService extends IService<AppJagileDeployInfo> {

    /**
     * 获取指定日期后的上线记录，并去重
     * @param deptFullName
     * @param createTime
     * @return
     */
    List<AppJagileDeployInfo> fetchDeployInfoListAfter(String deptFullName, String createTime);

}

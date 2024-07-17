package com.jd.workflow.console.service.jagile.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.jagile.AppJagileDeployInfoMapper;
import com.jd.workflow.console.entity.jagile.AppJagileDeployInfo;
import com.jd.workflow.console.service.jagile.AppJagileDeployInfoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 行云jdos部署记录表 服务实现类
 * </p>
 *
 * @author zhaojingchun
 * @since 2023-07-10
 */
@Service
public class AppJagileDeployInfoServiceImpl extends ServiceImpl<AppJagileDeployInfoMapper, AppJagileDeployInfo> implements AppJagileDeployInfoService {

    /**
     * 从数据库查询指定时间之后的上线记录
     *
     * @return
     */
    public List<AppJagileDeployInfo> queryFromDbDeployInfoAfter(String deptFullName, String createTime) {
        String env = "pro";
        LambdaQueryWrapper<AppJagileDeployInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppJagileDeployInfo::getEnvironment, env);
        lqw.likeRight(AppJagileDeployInfo::getAppDeptFullname, deptFullName);
        lqw.gt(AppJagileDeployInfo::getCreateTime, createTime);
        lqw.ne(AppJagileDeployInfo::getBranch, "");
        List<AppJagileDeployInfo> retData = list(lqw);
        return retData;
    }

    /**
     * 获取指定日期后的上线记录，并去重
     * @param deptFullName
     * @param createTime
     * @return
     */
    public List<AppJagileDeployInfo> fetchDeployInfoListAfter(String deptFullName, String createTime) {
        List<AppJagileDeployInfo> deployInfoList = queryFromDbDeployInfoAfter(deptFullName, createTime);
        List<AppJagileDeployInfo> retDataList = distinctDeployInfoList(deployInfoList);
        return retDataList;
    }


    /**
     * 去重部署记录
     * @param deployInfoList
     * @return
     */
    private List<AppJagileDeployInfo> distinctDeployInfoList(List<AppJagileDeployInfo> deployInfoList) {
        List<AppJagileDeployInfo> retData = deployInfoList.stream()
                .filter(distinctByKey(deploy -> deploy.getGitProject() + "_" + deploy.getBranch()))
                .collect(Collectors.toList());
        return retData;
    }

    /**
     * 去重 Predicate
     * @param keyExtractor
     * @param <T>
     * @return
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


}

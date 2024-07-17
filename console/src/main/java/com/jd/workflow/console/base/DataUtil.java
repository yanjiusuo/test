package com.jd.workflow.console.base;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Sets;
import com.jd.common.util.StringUtils;

import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：DataUtil
 * 类 描 述：数据处理工具类
 * 创建时间：2022-11-16 17:44
 * 创 建 人：wangxiaofei8
 */
public class DataUtil {

    /**
     * 集合元素判断
     * @param list
     * @return
     */
    public static boolean strElementIsInvalid(List<String> list){
        return list==null||list.size()==0||list.size()!= Sets.newHashSet(list).size()||list.stream().anyMatch(o-> StringUtils.isBlank(o));
    }

    /**
     * 集合元素判断非空时判断
     * @param list
     * @return
     */
    public static boolean strElementIsInvalidWhenNotEmpty(List<String> list){
        return CollectionUtils.isNotEmpty(list) && (list.size()!= Sets.newHashSet(list).size()||list.stream().anyMatch(o-> StringUtils.isBlank(o)));
    }
}

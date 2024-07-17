package com.jd.workflow.console.dto.manage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口分页搜索结果
 */
@Data
public class InterfaceOrMethodResult {
    /**
     * 接口搜索结果
     */
    List<SearchResult> result = new ArrayList<>();

    /**
     * 搜索结果
     */
    @Data
     public static class SearchResult{
        /**
         * 类型 {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
         */
        int type;
        /**
         *  总数
         */
         long total;
        /**
         * 接口数据
         */
        Page<InterfaceManage> interfaceData;
        /**
         * 方法数据
         */
         Page<MethodSearchResult> methodData;
     }
}

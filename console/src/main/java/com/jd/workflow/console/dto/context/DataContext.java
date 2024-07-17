package com.jd.workflow.console.dto.context;


import com.jd.workflow.console.dto.ParamDepDto;
import com.jd.workflow.console.dto.ParamStepDto;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-28
 */
@Data
public class DataContext {

    /**
     * 快捷调用 用于储存执行信息
     */
    List<ParamStepDto> paramStepMsgs = Lists.newArrayList();


    /**
     * 用于解析参数依赖
     */
    List<ParamDepDto> paramDepParse = Lists.newArrayList();
}

package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.ExampleScence;
import io.swagger.annotations.Api;

import java.util.List;
import java.util.Map;

@Api(value = "方法示例场景接口")
public interface ExampleScenceService extends IService<ExampleScence> {
    ExampleScence getEntity(String id);

    Boolean remove(Long id);

    Boolean removeHard(Long id);

//    Boolean add(ExampleScence scence);

    Map<Long, Boolean> add(List<ExampleScence> scence);

    Map<Long, Boolean> removeAndadd(List<ExampleScence> scence);



    List<ExampleScence> listByMethodId(Long methodId);
}

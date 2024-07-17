package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.PublicParamsDTO;
import com.jd.workflow.console.entity.InterfaceExtParam;


/**
 * @author shuchang21
 * @since 2022-09-13
 */
public interface InterfaceExtService extends IService<InterfaceExtParam> {

    Long add(PublicParamsDTO publicParamsDTO);

    Long edit(PublicParamsDTO publicParamsDTO);

    Boolean remove(Long id);
    InterfaceExtParam getByInterfaceId(Long id);

    Page<InterfaceExtParam> page(Integer page);

}

package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * 项目名称：parent
 * 类 名 称：PublishMethodQueryDTO
 * 类 描 述：TODO
 * 创建时间：2022-12-28 14:06
 * 创 建 人：wangxiaofei8
 */
@Data
public class PublishMethodQueryDTO extends PublishMethodQueryReqDTO{

    private Long offset=0L;

    private Long limit=10L;

    public PublishMethodQueryDTO() {
    }

    public PublishMethodQueryDTO(PublishMethodQueryReqDTO dto) {
        this.setInterfaceName(dto.getInterfaceName());
        this.setClusterId(dto.getClusterId());
        this.setMethodName(dto.getMethodName());
        this.setCurrentPage(dto.getCurrentPage()==null?1l:dto.getCurrentPage());
        this.setPageSize(dto.getPageSize()==null?10l:dto.getPageSize());
        this.setOffset(((this.getCurrentPage()-1)*this.getPageSize()));
        this.setLimit(this.getPageSize());
    }
}

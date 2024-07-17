package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询结果
 */
@Data
public class CamelLogListDTO {

    /**
     * 总条数
     */
    private Long totalCnt = 0l;

    /**
     * 查询结果
     */
    private List<CamelLogDTO> list ;


    /**
     * 填充数据
     * @param dto
     * @return
     */
    public CamelLogListDTO addCamelLogDto(CamelLogDTO dto){
        if(list==null){
            list = new ArrayList<>();
        }
        list.add(dto);
        return this;
    }


}

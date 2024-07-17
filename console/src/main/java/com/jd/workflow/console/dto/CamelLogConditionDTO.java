package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志查询条件选择
 */
@Data
public class CamelLogConditionDTO {

    /**
     * 总条数
     */
    private Long totalCnt = 0l;

    /**
     * 查询结果
     */
    private List<ConditionElement> list ;




    @Data
    static class ConditionElement{

        private Long id;

        private String name;

        public ConditionElement(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    /**
     * 封装结果
     * @param id
     * @param name
     * @return
     */
    public CamelLogConditionDTO addElement(Long id,String name){
        if(list==null){
            list = new ArrayList<>();
        }
        list.add(new ConditionElement(id,name));
        return this;
    }
}

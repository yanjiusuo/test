package com.jd.workflow.console.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.base.PageParam;

import com.jd.workflow.jsf.enums.JsfRegistrySite;
import lombok.Data;

@Data
public class JsfAliasDTO extends PageParam {
    //主键
    Long id;

    //所属接口ID
    Long interfaceId;

    //别名
    String alias;

    //站点
    JsfRegistrySite site;

    //环境
    String env;
}

package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * coding地址信息 用于原子市场-文档上报原子 执行时 获取 编译参数
 */
@Data
@TableName("dev_code_info")
public class DevCodeInfo extends BaseEntity implements Serializable {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String name;
    private String language;
    private String compileTool;
    private String compileCommand;
    private String compileParam;
    private String branch;
    private String compilePath;
    private String codePath;


    public DevCodeInfo() {
        this.compileTool = "";
        this.compileCommand = "";
        this.compileParam = "";
        this.compilePath = "";
        this.codePath = "";
    }
}

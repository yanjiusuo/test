package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目名称：parent
 * 类 名 称：JobLock
 * 类 描 述：TODO
 * 创建时间：2022-12-16 15:07
 * 创 建 人：wangxiaofei8
 */
@Data
@TableName(value = "job_lock")
public class JobLock implements Serializable {

    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer type;

    private String lockValue;

    private String ip;

    private String creator;

    private Date created;


}

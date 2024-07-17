package com.jd.workflow.console.entity.debug;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.debug.dto.JarLoadStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * jsf接口jar包记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jsf_jar_record")
public class JsfJarRecord extends BaseEntity implements Serializable {
    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 应用id
     */
    private Long appId;

    private String groupId;
    private String artifactId;
    private String version;
    /**
     * 上次更新时间
      */
    private Date lastUpdatedTime;
    /**
     * 是否有效 0 无效 1 有效
     */
    private Integer isValid;
    /**
     * 当前缓存jar包依赖的ip地址
     */
    private String currentCachedServerIp;

    private String jarActualVersion;
    private String ossDownloadUrl;
    /**
     * 0:未加载 1:加载中 2:加载成功 3:加载失败
      */
    private Integer loadStatus;
    private String failReasonDesc;
    private String failReasonDetail;

    public void changeLoadingStatus(Integer loadingStatus){
         this.loadStatus = loadingStatus;
         this.failReasonDesc = null;
            this.failReasonDetail = null;

    }
}

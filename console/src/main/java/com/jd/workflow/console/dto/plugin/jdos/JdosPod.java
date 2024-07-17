package com.jd.workflow.console.dto.plugin.jdos;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liangchengshuo
 * @description
 * @date 2021/9/7
 * <p>
 * 类
 */
@Data
public class JdosPod implements Serializable {

    private static final long serialVersionUID = 1L;

    private String podName;

    private String podIp;

    private String status;

    private String lbStatus;

    private String jsfStatus;

    private String applicabilityStatus;

    private String image;
}

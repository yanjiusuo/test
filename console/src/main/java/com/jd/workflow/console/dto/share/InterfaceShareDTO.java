package com.jd.workflow.console.dto.share;

import com.jd.workflow.console.dto.InterfaceShareTreeDTO;
import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/31 14:37
 * @Description:
 */
@Data
public class InterfaceShareDTO {

    /**
     * 分享名称
     */
    private String shareGroupName;

    /**
     * 是否跨应用0：不跨应用 1：跨应用
     */
    private int acrossApp;

    /**
     * 分享接口树
     */
    private InterfaceShareTreeDTO interfaceShareTreeDTO;

    /**
     * 分享erps
     */
    private String shareErp;

}
